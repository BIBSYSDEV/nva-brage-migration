package no.sikt.nva;

import static java.util.Objects.nonNull;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.AUTHOR;
import static nva.commons.core.attempt.Try.attempt;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.aws.ColoredLogger;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Customer;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.sikt.nva.exceptions.ContentException;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.AffiliationType;
import no.sikt.nva.scrapers.AlreadyImportedHandlesScraper;
import no.sikt.nva.scrapers.ContentScraper;
import no.sikt.nva.scrapers.DublinCoreFactory;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.HandleScraper;
import no.sikt.nva.scrapers.LicenseScraper;
import no.sikt.nva.scrapers.embargo.EmbargoParser;
import no.sikt.nva.scrapers.embargo.OnlineEmbargoChecker;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

@SuppressWarnings("PMD.GodClass")
@JacocoGenerated
public class BrageProcessor implements Runnable {

    private static final ColoredLogger logger = ColoredLogger.create(BrageProcessor.class);
    private static final String HANDLE_DEFAULT_NAME = "handle";
    private static final String BRAGE_DUBLIN_CORE_XML_DEFAULT_NAME = "dublin_core.xml";
    private static final String FS_DUBLIN_CORE_XML_DEFAULT_NAME = "metadata_fs.xml";
    private final static Counter counter = new Counter();
    private final String zipfile;
    private final String destinationDirectory;
    private final HandleScraper handleScraper;
    private final boolean enableOnlineValidation;
    private final boolean shouldLookUpInChannelRegister;
    private final Map<String, List<Embargo>> embargoes;
    private final Map<String, Contributor> contributors;
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final String customer;
    private final AffiliationType affiliationType;
    private List<Record> records;
    private final boolean isUnzipped;
    private final OnlineEmbargoChecker onlineEmbargoChecker;

    @SuppressWarnings({"PMD.AssignmentToNonFinalStatic", "PMD.ExcessiveParameterList"})
    public BrageProcessor(String zipfile, String customer,
                          String destinationDirectory,
                          boolean enableOnlineValidation,
                          boolean shouldLookUpInChannelRegister,
                          Map<String, List<Embargo>> embargoes,
                          Map<String, Contributor> contributors,
                          AffiliationType affiliationType,
                          boolean isUnzipped,
                          OnlineEmbargoChecker onlineEmbargoChecker
                          ) {
        this.customer = customer;
        this.zipfile = zipfile;
        this.enableOnlineValidation = enableOnlineValidation;
        this.shouldLookUpInChannelRegister = shouldLookUpInChannelRegister;
        this.destinationDirectory = destinationDirectory;
        this.handleScraper = new HandleScraper();
        this.embargoes = embargoes;
        this.contributors = contributors;
        this.affiliationType = affiliationType;
        this.isUnzipped = isUnzipped;
        this.onlineEmbargoChecker = onlineEmbargoChecker;
    }

    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    @Override
    public void run() {
        var resourceDirectories = isUnzipped
                                      ? readResources(destinationDirectory)
                                      : UnZipper.extractResourceDirectories(zipfile, destinationDirectory);
        try {
            if (!resourceDirectories.isEmpty()) {
                records = processBundles(resourceDirectories);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<File> readResources(String directory) {
        return Arrays.asList(Objects.requireNonNull(new File(directory).listFiles()));
    }

    public List<Record> getRecords() {
        return records;
    }

    public int getEmbargoCounter() {
        return counter.getEmbargoCounter();
    }

    private boolean isBundle(File entryDirectory) {
        return nonNull(entryDirectory)
               && StringUtils.isNotBlank(entryDirectory.getPath())
               && entryDirectory.isDirectory();
    }

    private Path getHandlePath(File entryDirectory) {
        var bundlePath = entryDirectory.getPath();
        return Path.of(bundlePath, HANDLE_DEFAULT_NAME);
    }

    private File getDublinCoreFile(File entryDirectory, String fileName) {
        return new File(Path.of(entryDirectory.toString(), fileName).toString());
    }

    private Record injectContentBundle(Record record, File entryDirectory, BrageLocation brageLocation,
                                       DublinCore dublinCore) throws ContentException {
        var embargo = DublinCoreScraper.extractEmbargo(dublinCore, customer);
        record.setContentBundle(getContent(entryDirectory, brageLocation, dublinCore, embargo));
        return record;
    }

    private ResourceContent getContent(File entryDirectory, BrageLocation brageLocation, DublinCore dublinCore,
                                       String embargo)
        throws ContentException {
        var license = new LicenseScraper(dublinCore).generateLicense();
        var contentScraper = new ContentScraper(entryDirectory.toPath(), brageLocation, license, embargo, customer);
        return contentScraper.scrapeContent();
    }

    private Record injectResourceContent(File entryDirectory, BrageLocation brageLocation, DublinCore dublinCore,
                                         Record r) {
        try {
            return injectContentBundle(r, entryDirectory, brageLocation, dublinCore);
        } catch (ContentException e) {
            throw new RuntimeException(e);
        }
    }

    private DublinCore parseDublinCore(File entryDirectory) {
        return DublinCoreFactory.createDublinCoreFromXml(
            getDublinCoreFile(entryDirectory, BRAGE_DUBLIN_CORE_XML_DEFAULT_NAME));
    }

    private DublinCore parseFsDublinCore(File entryDirectory) {
        var file = getDublinCoreFile(entryDirectory, FS_DUBLIN_CORE_XML_DEFAULT_NAME);
        return file.exists()
                   ? DublinCoreFactory.createDublinCoreFromXml(file)
                   : emptyDublinCore();
    }

    private static DublinCore emptyDublinCore() {
        return new DublinCore(List.of());
    }

    private Record injectCustomer(Record record) {
        record.setCustomer(new Customer(customer, null));
        return record;
    }

    private List<Record> processBundles(List<File> resourceDirectories) throws IOException {
        return resourceDirectories.stream()
                   .filter(this::isBundle)
                   .map(this::processBundle)
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private Optional<Record> processBundle(File entryDirectory) {
        try {
            var brageLocation = new BrageLocation(Path.of(entryDirectory.getPath()));
            var dublinCore = parseDublinCore(entryDirectory);
            brageLocation.setTitle(DublinCoreScraper.extractMainTitle(dublinCore));
            brageLocation.setHandle(getHandle(entryDirectory, dublinCore, brageLocation));
            String handle = brageLocation.getHandle().toString();
            if (BrageMigrationCommand.alreadyProcessed(handle)) {
                return Optional.empty();
            }
            if (isAlreadyImported(handle)) {
                return Optional.empty();
            }
            return createRecord(entryDirectory,
                                brageLocation,
                                dublinCore);
        } catch (Exception e) {
            var brageLocation = new BrageLocation(Path.of(destinationDirectory, entryDirectory.getName()));
            if (nonNull(brageLocation.getHandle())) {
                logger.error(e + StringUtils.SPACE + brageLocation.getOriginInformation());
            } else {
                logger.error(e + StringUtils.SPACE + brageLocation.getBrageBundlePath() + StringUtils.SPACE + brageLocation.getTitle());
                logger.error(brageLocation.getBrageBundlePath() + StringUtils.SPACE + "this post is most likely "
                             + "bugged and not searchable, or in a unfinished state. Search result for this title is "
                             + "most likely a duplicate post.");
            }
            return Optional.empty();
        }
    }

    private Optional<Record> createRecord(File entryDirectory,
                                          BrageLocation brageLocation,
                                          DublinCore dublinCore) {
        var dublinCoreScraper = new DublinCoreScraper(enableOnlineValidation,
                                                      shouldLookUpInChannelRegister,
                                                      contributors);
        return Optional.of(dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                        brageLocation,
                                                                        customer))
                   .map(this::injectCustomer)
                   .map(r -> injectResourceContent(entryDirectory, brageLocation, dublinCore, r))
                   .map(r -> injectBrageLocation(r, brageLocation))
                   .map(this::injectAffiliationsFromExternalFile)
                   .map(record -> injectValuesFromFsDublinCore(record, dublinCore, parseFsDublinCore(entryDirectory)))
                   .map(r -> EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(r, embargoes, onlineEmbargoChecker));
    }

    public static Record injectValuesFromFsDublinCore(Record record, DublinCore brageDublinCore, DublinCore fsDublinCore) {
        var subjectCode = getSubjectCode(fsDublinCore, brageDublinCore);
        record.setSubjectCode(subjectCode);
        updateDescriptionsIfNeeded(record, subjectCode);
        return record;
    }

    private static String getSubjectCode(DublinCore fsDublinCore, DublinCore brageDublinCore) {
        var subjectCodeFromFs = DublinCoreScraper.extractSubjectCodeFromFsXml(fsDublinCore);
        var subjectCodesFromBrage = DublinCoreScraper.extractSubjectCodesFromDublinCore(brageDublinCore);
        return nonNull(subjectCodeFromFs) ? subjectCodeFromFs : getFirstOrElseNull(subjectCodesFromBrage);
    }

    private static String getFirstOrElseNull(List<String> subjectCodesFromBrage) {
        return attempt(() -> subjectCodesFromBrage.get(0)).orElse(failure -> null);
    }

    private static void updateDescriptionsIfNeeded(Record record, String subjectCode) {
        if (nonNull(subjectCode)) {
            var descriptions = new ArrayList<>(record.getEntityDescription().getDescriptions());
            descriptions.remove(subjectCode);
            record.getEntityDescription().setDescriptions(descriptions);
        }
    }

    private Record injectBrageLocation(Record record, BrageLocation brageLocation) {
        record.setBrageLocation(String.valueOf(brageLocation.getBrageBundlePath()));
        return record;
    }

    private Record injectAffiliationsFromExternalFile(Record record) {
        if (!affiliationType.getAffiliations().isEmpty() && containsType(record)) {
            var matchingAffiliationKeys = affiliationType.getAffiliations().keySet()
                                              .stream()
                                              .filter(record::hasOrigin)
                                              .collect(Collectors.toList());
            var matchingAffiliations = matchingAffiliationKeys.stream()
                                           .map(key -> affiliationType.getAffiliations().get(key))
                                           .collect(Collectors.toSet());
            record.getEntityDescription()
                .getContributors().stream()
                .filter(contributor -> AUTHOR.equals(contributor.getRole()))
                .forEach(contributor -> contributor.setAffiliations(matchingAffiliations));
        }
        return record;
    }

    private boolean containsType(Record record) {
        return affiliationType.getTypes().contains(record.getType().getNva());
    }

    private boolean isAlreadyImported(String handle) {
        var importedHandles = AlreadyImportedHandlesScraper.scrapeHandlesFromSuppliedExternalFile(
            new File("handles.csv"));
        return importedHandles.contains(handle);
    }

    private URI getHandle(File entryDirectory, DublinCore dublinCore, BrageLocation brageLocation) throws HandleException {
        return handleScraper.scrapeHandle(getHandlePath(entryDirectory), dublinCore, brageLocation);
    }
}
package no.sikt.nva;

import static java.util.Objects.nonNull;
import static no.sikt.nva.scrapers.DublinCoreScraper.CRISTIN_POST;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Customer;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.sikt.nva.exceptions.ContentException;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.AffiliationsScraper;
import no.sikt.nva.scrapers.AlreadyImportedHandlesScraper;
import no.sikt.nva.scrapers.ContentScraper;
import no.sikt.nva.scrapers.CustomerMapper;
import no.sikt.nva.scrapers.DublinCoreFactory;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.EmbargoScraper;
import no.sikt.nva.scrapers.HandleScraper;
import no.sikt.nva.scrapers.LicenseScraper;
import no.sikt.nva.scrapers.ResourceOwnerMapper;
import no.sikt.nva.validators.BrageProcessorValidator;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
@JacocoGenerated
public class BrageProcessor implements Runnable {

    public static final String DEFAULT_LICENSE_FILE_NAME = "license_rdf";
    public static final String RECORD_REMOVED_BECAUSE_OF_EMBARGO = "Record was removed from import because of "
                                                                   + "embargo: ";
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    private static final String HANDLE_DEFAULT_NAME = "handle";
    private static final String DUBLIN_CORE_XML_DEFAULT_NAME = "dublin_core.xml";
    private static final String CONTENT_FILE_DEFAULT_NAME = "contents";
    private final static Counter counter = new Counter();
    private final String zipfile;
    private final String destinationDirectory;
    private final HandleScraper handleScraper;
    private final boolean enableOnlineValidation;
    private final boolean shouldLookUpInChannelRegister;
    private final boolean noHandleCheck;
    private final List<Embargo> embargoes;
    private final Map<String, Contributor> contributors;
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final String customer;
    private final String awsEnvironment;
    private List<Record> records;

    @SuppressWarnings({"PMD.AssignmentToNonFinalStatic", "PMD.ExcessiveParameterList"})
    public BrageProcessor(String zipfile,
                          String customer,
                          String destinationDirectory,
                          final Map<String, String> rescueTitleAndHandleMap,
                          boolean enableOnlineValidation,
                          boolean shouldLookUpInChannelRegister,
                          boolean noHandleCheck,
                          String awsEnvironment,
                          List<Embargo> embargoes,
                          Map<String, Contributor> contributors) {
        this.customer = customer;
        this.zipfile = zipfile;
        this.enableOnlineValidation = enableOnlineValidation;
        this.shouldLookUpInChannelRegister = shouldLookUpInChannelRegister;
        this.destinationDirectory = destinationDirectory;
        this.handleScraper = new HandleScraper(rescueTitleAndHandleMap);
        this.noHandleCheck = noHandleCheck;
        this.awsEnvironment = awsEnvironment;
        this.embargoes = embargoes;
        this.contributors = contributors;
    }

    public Path getContentFilePath(File entryDirectory) {
        var bundlePath = entryDirectory.getPath();
        return Path.of(bundlePath, CONTENT_FILE_DEFAULT_NAME);
    }

    public boolean hasEmbargoEndDateWhichIsNotExpired(DublinCore dublinCore) {
        if (getEmbargoEndDate(dublinCore).isPresent()) {
            var today = LocalDate.now();
            try {
                return new LocalDate(getEmbargoEndDate(dublinCore)).isAfter(today);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    @Override
    public void run() {
        List<File> resourceDirectories = UnZipper.extractResourceDirectories(zipfile, destinationDirectory);
        try {
            records = processBundles(resourceDirectories);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Record> getRecords() {
        return records;
    }

    public int getEmbargoCounter() {
        return counter.getEmbargoCounter();
    }

    private boolean isBundle(File entryDirectory) {
        return entryDirectory.isDirectory();
    }

    private Path getHandlePath(File entryDirectory) {
        var bundlePath = entryDirectory.getPath();
        return Path.of(bundlePath, HANDLE_DEFAULT_NAME);
    }

    private File getDublinCoreFile(File entryDirectory) {
        return new File(entryDirectory, DUBLIN_CORE_XML_DEFAULT_NAME);
    }

    private void logErrorsIfNotEmpty(BrageLocation brageLocation, List<ErrorDetails> errors, Record record) {
        if (!errors.isEmpty()) {
            if (isCristinPost(record)) {
                logger.error(CRISTIN_POST + errors + StringUtils.SPACE + brageLocation.getOriginInformation());
            } else {
                logger.error(errors + StringUtils.SPACE + brageLocation.getOriginInformation());
            }
        }
    }

    private static boolean isCristinPost(Record record) {
        return nonNull(record.getCristinId()) && !record.getCristinId().isEmpty();
    }

    private boolean hasEmbargo(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .anyMatch(DcValue::isEmbargo);
    }

    private Optional<Boolean> getEmbargoEndDate(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .map(DcValue::isEmbargoEndDate)
                   .findFirst();
    }

    private String getEmbargoDate(DublinCore dublinCore) {
        return String.valueOf(dublinCore.getDcValues()
                                  .stream()
                                  .filter(DcValue::isEmbargo)
                                  .findFirst()
                                  .map(DcValue::scrapeValueAndSetToScraped));
    }

    private void logEmbargoMessage(BrageLocation brageLocation, DublinCore dublinCore) {
        logger.error(RECORD_REMOVED_BECAUSE_OF_EMBARGO
                     + getEmbargoDate(dublinCore)
                     + StringUtils.SPACE
                     + brageLocation.getOriginInformation());
    }

    private boolean containsEmbargo(DublinCore dublinCore) {
        return hasEmbargo(dublinCore) || hasEmbargoEndDateWhichIsNotExpired(dublinCore);
    }

    private Record injectContentBundle(Record record, File entryDirectory, BrageLocation brageLocation,
                                       LicenseScraper licenseScraper, DublinCore dublinCore)
        throws ContentException {
        record.setContentBundle(getContent(entryDirectory, brageLocation, licenseScraper,
                                           dublinCore));
        return record;
    }

    private ResourceContent getContent(File entryDirectory, BrageLocation brageLocation,
                                       LicenseScraper licenseScraper, DublinCore dublinCore)
        throws ContentException {
        var license = licenseScraper.extractLicense(entryDirectory, dublinCore);
        var contentScraper = new ContentScraper(getContentFilePath(entryDirectory), brageLocation, license);
        return contentScraper.scrapeContent();
    }

    private Record injectErrorsFromBrageProcessorValidator(File entryDirectory, DublinCore dublinCore,
                                                           Record record, BrageLocation brageLocation) {
        var errorsFromBrageProcessorValidator = BrageProcessorValidator.getBrageProcessorErrors(entryDirectory,
                                                                                                dublinCore);
        record.getErrors().addAll(errorsFromBrageProcessorValidator);
        logErrorsIfNotEmpty(brageLocation, errorsFromBrageProcessorValidator, record);
        return record;
    }

    private Record injectResourceContent(LicenseScraper licenseScraper, File entryDirectory,
                                         BrageLocation brageLocation,
                                         DublinCore dublinCore, Record r) {
        try {
            return injectContentBundle(r, entryDirectory, brageLocation,
                                       licenseScraper, dublinCore);
        } catch (ContentException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Record> logAndReturnOptionalEmpty(BrageLocation brageLocation, DublinCore dublinCore) {
        logEmbargoMessage(brageLocation, dublinCore);
        counter.countRecordWithEmbargo();
        return Optional.empty();
    }

    private DublinCore parseDublinCore(File entryDirectory) {
        return DublinCoreFactory.createDublinCoreFromXml(getDublinCoreFile(entryDirectory));
    }

    private Record injectCustomer(Record record) {
        record.setCustomer(new Customer(customer, new CustomerMapper().getCustomerUri(customer, awsEnvironment)));
        return record;
    }

    private Record injectResourceOwner(Record record) {
        record.setResourceOwner(new ResourceOwnerMapper().getResourceOwner(customer, awsEnvironment));
        return record;
    }

    private List<Record> processBundles(List<File> resourceDirectories) throws IOException {
        LicenseScraper licenseScraper = new LicenseScraper(DEFAULT_LICENSE_FILE_NAME);
        return resourceDirectories.stream()
                   .filter(file -> isBundle(file))
                   .map(bundleDirectory -> processBundle(licenseScraper, bundleDirectory))
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private Optional<Record> processBundle(LicenseScraper licenseScraper, File entryDirectory) {
        try {
            var brageLocation = new BrageLocation(Path.of(destinationDirectory, entryDirectory.getName()));
            var dublinCore = parseDublinCore(entryDirectory);
            brageLocation.setTitle(DublinCoreScraper.extractMainTitle(dublinCore));
            brageLocation.setHandle(getHandle(entryDirectory, dublinCore));
            if (isAlreadyImported(brageLocation.getHandle().toString())) {
                return Optional.empty();
            }
            if (containsEmbargo(dublinCore)) {
                return logAndReturnOptionalEmpty(brageLocation, dublinCore);
            }
            return createRecord(licenseScraper, entryDirectory, brageLocation, dublinCore);
        } catch (Exception e) {
            var brageLocation = new BrageLocation(Path.of(destinationDirectory, entryDirectory.getName()));
            logger.error(e.getMessage() + StringUtils.SPACE + brageLocation.getOriginInformation());
            return Optional.empty();
        }
    }

    private Optional<Record> createRecord(LicenseScraper licenseScraper, File entryDirectory,
                                          BrageLocation brageLocation,
                                          DublinCore dublinCore) {
        var dublinCoreScraper = new DublinCoreScraper(enableOnlineValidation,
                                                      shouldLookUpInChannelRegister,
                                                      contributors);
        return Optional.of(dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation))
                   .map(record -> injectCustomer(record))
                   .map(record -> injectResourceOwner(record))
                   .map(r -> injectResourceContent(licenseScraper, entryDirectory, brageLocation, dublinCore, r))
                   .map(r -> injectBrageLocation(r, brageLocation))
                   .map(this::injectAffiliationsFromExternalFile)
                   .map(r -> injectErrorsFromBrageProcessorValidator(entryDirectory, dublinCore, r, brageLocation))
                   .map(r -> EmbargoScraper.checkForEmbargoFromSuppliedEmbargoFile(r, embargoes));
    }

    private Record injectBrageLocation(Record record, BrageLocation brageLocation) {
        record.setBrageLocation(String.valueOf(brageLocation.getBrageBundlePath()));
        return record;
    }

    private Record injectAffiliationsFromExternalFile(Record record) {
        var affiliations = AffiliationsScraper.getAffiliations(new File("affiliations.txt"));
        if (!affiliations.isEmpty()) {
            var matchingAffiliationKeys = affiliations.keySet().stream()
                                              .filter(record::hasOrigin)
                                              .collect(Collectors.toList());
            var matchingAffiliations = matchingAffiliationKeys.stream()
                                           .map(affiliations::get)
                                           .collect(Collectors.toList());
            record.getEntityDescription()
                .getContributors()
                .forEach(contributor -> contributor.setAffiliations(matchingAffiliations));
        }
        return record;
    }

    private boolean isAlreadyImported(String handle) {
        var importedHandles = AlreadyImportedHandlesScraper.scrapeHandlesFromSuppliedExternalFile(
            new File("handles.csv"));
        return importedHandles.contains(handle);
    }

    private URI getHandle(File entryDirectory, DublinCore dublinCore) throws HandleException {
        return noHandleCheck
                   ? getHandleAndIgnoreErrors(entryDirectory, dublinCore)
                   : handleScraper.scrapeHandle(getHandlePath(entryDirectory), dublinCore);
    }

    private URI getHandleAndIgnoreErrors(File entryDirectory, DublinCore dublinCore) {
        try {
            return handleScraper.scrapeHandle(getHandlePath(entryDirectory), dublinCore);
        } catch (HandleException e) {
            //ignored
            return null;
        }
    }
}
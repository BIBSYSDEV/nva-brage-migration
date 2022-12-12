package no.sikt.nva;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.record.Customer;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.sikt.nva.exceptions.ContentException;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.ContentScraper;
import no.sikt.nva.scrapers.CustomerMapper;
import no.sikt.nva.scrapers.DublinCoreFactory;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.HandleScraper;
import no.sikt.nva.scrapers.LicenseScraper;
import no.sikt.nva.scrapers.ResourceOwnerMapper;
import no.sikt.nva.validators.BrageProcessorValidator;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JacocoGenerated
public class BrageProcessor implements Runnable {

    public static final String DEFAULT_LICENSE_FILE_NAME = "license_rdf";
    public static final String RECORD_REMOVED_BECAUSE_OF_EMBARGO = "Record was removed from import because of "
                                                                   + "embargo: ";
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    private static final String HANDLE_DEFAULT_NAME = "handle";
    private static final String DUBLIN_CORE_XML_DEFAULT_NAME = "dublin_core.xml";
    private static final String CONTENT_FILE_DEFAULT_NAME = "contents";
    private final String zipfile;
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final String customer;
    private final String destinationDirectory;
    private final HandleScraper handleScraper;
    private final boolean enableOnlineValidation;
    private final boolean noHandleCheck;
    private final List<Embargo> embargoes;
    private List<Record> records;

    public BrageProcessor(String zipfile,
                          String customer,
                          String destinationDirectory,
                          final Map<String, String> rescueTitleAndHandleMap,
                          boolean enableOnlineValidation,
                          boolean noHandleCheck,
                          List<Embargo> embargoes) {
        this.customer = customer;
        this.zipfile = zipfile;
        this.enableOnlineValidation = enableOnlineValidation;
        this.destinationDirectory = destinationDirectory;
        this.handleScraper = new HandleScraper(rescueTitleAndHandleMap);
        this.noHandleCheck = noHandleCheck;
        this.embargoes = embargoes;
    }

    public static Path getContentFilePath(File entryDirectory) {
        var bundlePath = entryDirectory.getPath();
        return Path.of(bundlePath, CONTENT_FILE_DEFAULT_NAME);
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

    private static boolean isBundle(File entryDirectory) {
        return entryDirectory.isDirectory();
    }

    private static Path getHandlePath(File entryDirectory) {
        var bundlePath = entryDirectory.getPath();
        return Path.of(bundlePath, HANDLE_DEFAULT_NAME);
    }

    private static File getDublinCoreFile(File entryDirectory) {
        return new File(entryDirectory, DUBLIN_CORE_XML_DEFAULT_NAME);
    }

    private static void logWarningsIfNotEmpty(BrageLocation brageLocation, List<WarningDetails> warnings) {
        if (!warnings.isEmpty()) {
            logger.warn(warnings + StringUtils.SPACE + brageLocation.getOriginInformation());
        }
    }

    private static boolean hasEmbargo(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .anyMatch(DcValue::isEmbargo);
    }

    private static String getEmbargoDate(DublinCore dublinCore) {
        return String.valueOf(dublinCore.getDcValues()
                                  .stream()
                                  .filter(DcValue::isEmbargo)
                                  .findFirst()
                                  .map(DcValue::scrapeValueAndSetToScraped));
    }

    private static void logEmbargoMessage(BrageLocation brageLocation, DublinCore dublinCore) {
        logger.error(RECORD_REMOVED_BECAUSE_OF_EMBARGO
                     + getEmbargoDate(dublinCore)
                     + StringUtils.SPACE
                     + brageLocation.getOriginInformation());
    }

    private List<Record> processBundles(List<File> resourceDirectories) throws IOException {
        LicenseScraper licenseScraper = new LicenseScraper(DEFAULT_LICENSE_FILE_NAME);
        return resourceDirectories.stream()
                   .filter(BrageProcessor::isBundle)
                   .map(bundleDirectory -> processBundle(licenseScraper, bundleDirectory))
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private Optional<Record> processBundle(LicenseScraper licenseScraper, File entryDirectory) {
        var brageLocation = new BrageLocation(Path.of(destinationDirectory, entryDirectory.getName()));
        try {
            var dublinCore = DublinCoreFactory.createDublinCoreFromXml(getDublinCoreFile(entryDirectory));
            if (hasEmbargo(dublinCore)) {
                logEmbargoMessage(brageLocation, dublinCore);
                return Optional.empty();
            }
            brageLocation.setTitle(DublinCoreScraper.extractMainTitle(dublinCore));
            brageLocation.setHandle(getHandle(entryDirectory, dublinCore));
            var dublinCoreScraper = new DublinCoreScraper(enableOnlineValidation);
            var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
            record.setCustomer(generateCustomer());
            record.setResourceOwner(ResourceOwnerMapper.getResourceOwner(customer));
            record.setContentBundle(getContent(entryDirectory, brageLocation, licenseScraper, dublinCore));
            record.setBrageLocation(String.valueOf(brageLocation.getBrageBundlePath()));
            var warnings = BrageProcessorValidator.getBrageProcessorWarnings(entryDirectory, dublinCore);
            record.getWarnings().addAll(warnings);
            EmbargoScraper.checkForEmbargoFromSuppliedEmbargoFile(record, embargoes);
            logWarningsIfNotEmpty(brageLocation, warnings);
            return Optional.of(record);
        } catch (Exception e) {
            logger.error(e.getMessage() + StringUtils.SPACE + brageLocation.getOriginInformation());
            return Optional.empty();
        }
    }

    @NotNull
    private Customer generateCustomer() {
        return new Customer(customer, CustomerMapper.getCustomerUri(customer));
    }

    private ResourceContent getContent(File entryDirectory, BrageLocation brageLocation,
                                       LicenseScraper licenseScraper, DublinCore dublinCore)
        throws ContentException {
        var license = licenseScraper.extractLicense(entryDirectory, dublinCore);
        var contentScraper = new ContentScraper(getContentFilePath(entryDirectory), brageLocation, license);
        return contentScraper.scrapeContent();
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
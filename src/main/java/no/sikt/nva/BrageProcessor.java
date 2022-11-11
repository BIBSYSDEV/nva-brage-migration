package no.sikt.nva;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.ContentException;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.content.ResourceContent;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.record.Record;
import no.sikt.nva.scrapers.ContentScraper;
import no.sikt.nva.scrapers.DublinCoreFactory;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.HandleScraper;
import no.sikt.nva.scrapers.LicenseScraper;
import no.sikt.nva.validators.BrageProcessorValidator;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JacocoGenerated
public class BrageProcessor implements Runnable {

    public static final String DEFAULT_LICENSE_FILE_NAME = "license_rdf";
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    private static final String HANDLE_DEFAULT_NAME = "handle";
    private static final String DUBLIN_CORE_XML_DEFAULT_NAME = "dublin_core.xml";
    private static final String CONTENT_FILE_DEFAULT_NAME = "contents";

    private final String zipfile;
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final URI customerId;
    private final String destinationDirectory;
    private final HandleScraper handleScraper;
    private final boolean enableOnlineValidation;
    private final boolean noHandleCheck;
    private List<Record> records;

    public BrageProcessor(String zipfile,
                          URI customerId,
                          String destinationDirectory,
                          final Map<String, String> rescueTitleAndHandleMap,
                          boolean enableOnlineValidation,
                          boolean noHandleCheck) {
        this.customerId = customerId;
        this.zipfile = zipfile;
        this.enableOnlineValidation = enableOnlineValidation;
        this.destinationDirectory = destinationDirectory;
        this.handleScraper = new HandleScraper(rescueTitleAndHandleMap);
        this.noHandleCheck = noHandleCheck;
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
        records = processBundles(resourceDirectories);

        System.out.println(records);
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

    private List<Record> processBundles(List<File> resourceDirectories) {
        LicenseScraper licenseScraper = new LicenseScraper(DEFAULT_LICENSE_FILE_NAME);
        return resourceDirectories
                   .stream()
                   .filter(BrageProcessor::isBundle)
                   .map(bundleDirectory -> processBundle(
                       licenseScraper,
                       bundleDirectory))
                   .flatMap(Optional::stream)
                   .collect(
                       Collectors.toList());
    }

    private Optional<Record> processBundle(LicenseScraper licenseScraper,
                                           File entryDirectory) {
        var brageLocation = new BrageLocation(Path.of(destinationDirectory, entryDirectory.getName()));
        try {
            var dublinCore = DublinCoreFactory.createDublinCoreFromXml(getDublinCoreFile(entryDirectory));
            brageLocation.setTitle(DublinCoreScraper.extractMainTitle(dublinCore));
            brageLocation.setHandle(getHandle(entryDirectory, dublinCore));
            var dublinCoreScraper = new DublinCoreScraper(enableOnlineValidation);
            var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
            record.setCustomerId(customerId);
            record.setContentBundle(getContent(entryDirectory, brageLocation, licenseScraper));
            record.setBrageLocation(brageLocation.getOriginInformation());
            logWarningsIfNotEmpty(brageLocation, BrageProcessorValidator.getBrageProcessorWarnings(entryDirectory));
            return Optional.of(record);
        } catch (Exception e) {
            logger.error(e.getMessage() + StringUtils.SPACE + brageLocation.getOriginInformation());
            return Optional.empty();
        }
    }

    private ResourceContent getContent(File entryDirectory, BrageLocation brageLocation, LicenseScraper licenseScraper)
        throws ContentException {
        var license = licenseScraper.extractOrCreateLicense(entryDirectory);
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
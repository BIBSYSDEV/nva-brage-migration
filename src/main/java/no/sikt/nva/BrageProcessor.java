package no.sikt.nva;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.record.Record;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JacocoGenerated
public class BrageProcessor implements Runnable {

    public static final String DEFAULT_LICENSE_FILE_NAME = "license_rdf";
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    private static final String HANDLE_DEFAULT_NAME = "handle";
    private static final String DUBLIN_CORE_XML_DEFAULT_NAME = "dublin_core.xml";

    private final String zipfile;
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final String customerId;
    private final String destinationDirectory;
    private final HandleScraper handleScraper;
    private List<Record> records;

    public BrageProcessor(String zipfile, String customerId, String destinationDirectory,
                          final Map<String, String> rescueTitleAndHandleMap) {
        this.customerId = customerId;
        this.zipfile = zipfile;
        this.destinationDirectory = destinationDirectory;
        this.handleScraper = new HandleScraper(rescueTitleAndHandleMap);
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
            brageLocation.setTitle(DublinCoreParser.extractTitle(dublinCore));
            brageLocation.setHandle(
                handleScraper.scrapeHandle(getHandlePath(entryDirectory),
                                           dublinCore));
            var record = DublinCoreParser.validateAndParseDublinCore(dublinCore, brageLocation);
            record.setLicense(
                licenseScraper.extractOrCreateLicense(entryDirectory, brageLocation.getOriginInformation()));
            return Optional.of(record);
        } catch (Exception e) {
            logger.error(e.getMessage() + brageLocation.getOriginInformation());
            return Optional.empty();
        }
    }
}
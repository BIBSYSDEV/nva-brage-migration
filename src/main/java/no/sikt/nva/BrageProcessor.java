package no.sikt.nva;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private static final String DEBUG_FILE_PROCESSING_LOG_MESSAGE = "Processeing file %s part of bundle %s";
    private final String zipfile;
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final String customerId;
    private final String destinationDirectory;
    private List<Record> records;

    public BrageProcessor(String zipfile, String customerId, String destinationDirectory) {
        this.customerId = customerId;
        this.zipfile = zipfile;
        this.destinationDirectory = destinationDirectory;
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

    private static void doStuffsForEachFile(File file, Record record) {
        //TODO: do more useful stuff here:
        //TODO: skip already processed files here.
        logger.debug(String.format(DEBUG_FILE_PROCESSING_LOG_MESSAGE, file.getName(), record.getId()));
    }

    private List<Record> processBundles(List<File> resourceDirectories) {
        DublinCoreParser dublinCoreParser = new DublinCoreParser();
        LicenseScraper licenseScraper = new LicenseScraper(DEFAULT_LICENSE_FILE_NAME);
        return resourceDirectories
                   .stream()
                   .filter(BrageProcessor::isBundle)
                   .map(bundleDirectory -> processBundle(dublinCoreParser,
                                                         licenseScraper,
                                                         bundleDirectory))
                   .flatMap(Optional::stream)
                   .collect(
                       Collectors.toList());
    }

    private Optional<Record> processBundle(DublinCoreParser dublinCoreParser, LicenseScraper licenseScraper,
                                                  File entryDirectory) {
        var record = new Record();
        record.setOrigin(Path.of(destinationDirectory, entryDirectory.getName()));
        try {
            var bundlePath = entryDirectory.getPath();
            var handlePath = Path.of(bundlePath, HANDLE_DEFAULT_NAME);
            var dublinCoreFile = new File(entryDirectory, DUBLIN_CORE_XML_DEFAULT_NAME);
            record.setId(HandleScraper.extractHandleFromBundle(handlePath, dublinCoreFile));
            dublinCoreParser.parseDublinCoreToRecord(dublinCoreFile, record);
            record.setLicense(licenseScraper.extractOrCreateLicense(entryDirectory, record.getOriginInformation()));
            Arrays.stream(Objects.requireNonNull(entryDirectory.listFiles()))
                .forEach(file -> doStuffsForEachFile(file, record));
        } catch (Exception e) {
            logger.error(e.getMessage(), record.getOriginInformation());
            return Optional.empty();
        }
        return Optional.of(record);
    }
}
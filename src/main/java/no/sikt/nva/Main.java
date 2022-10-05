package no.sikt.nva;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import no.sikt.nva.model.record.Record;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.ShortClassName")
@JacocoGenerated
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String HANDLE_DEFAULT_NAME = "handle";
    private static final String DUBLIN_CORE_XML_DEFAULT_NAME = "dublin_core.xml";
    private static final String DEBUG_FILE_PROCESSING_LOG_MESSAGE = "Processeing file %s part of bundle %s";

    public static void main(String[] args) {
        var pathToZip = "inputWithCristinId.zip"; //Todo: read this from CLI input
        var pathToDestinationDirectory = "destinationDirectory"; //TODO: read from CLI input
        var resourceDirectories = UnZipper.extractResourceDirectories(pathToZip, pathToDestinationDirectory);
        var records = processBundles(resourceDirectories);
        System.out.println(records);
    }

    private static List<Record> processBundles(List<File> resourceDirectories) {
        DublinCoreParser dublinCoreParser = new DublinCoreParser();
        return resourceDirectories
                   .stream()
                   .filter(Main::isBundle)
                   .map(bundleDirectory -> processBundle(dublinCoreParser,
                                                         bundleDirectory))
                   .collect(
                       Collectors.toList());
    }

    private static boolean isBundle(File entryDirectory) {
        return entryDirectory.isDirectory();
    }

    private static Record processBundle(DublinCoreParser dublinCoreParser, File entryDirectory) {
        var record = new Record();
        try {
            var bundlePath = entryDirectory.getPath();
            var handlePath = Path.of(bundlePath, HANDLE_DEFAULT_NAME);
            var dublinCoreFile = new File(entryDirectory, DUBLIN_CORE_XML_DEFAULT_NAME);
            record.setId(HandleScraper.extractHandleFromBundle(handlePath, dublinCoreFile));
            dublinCoreParser.parseDublinCoreToRecord(dublinCoreFile, record);
            Arrays.stream(Objects.requireNonNull(entryDirectory.listFiles()))
                .forEach(file -> doStuffsForEachFile(file, record));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return record;
    }

    private static void doStuffsForEachFile(File file, Record record) {
        //TODO: do more useful stuff here:
        //TODO: skip already processed files here.
        logger.debug(String.format(DEBUG_FILE_PROCESSING_LOG_MESSAGE, file.getName(), record.getId()));
    }
}
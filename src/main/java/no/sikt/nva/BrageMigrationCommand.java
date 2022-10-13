package no.sikt.nva;

import com.opencsv.CSVReaderHeaderAware;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@SuppressWarnings({"PMD.DoNotUseThreads"})
@JacocoGenerated
@Command(
    name = "Brage migration",
    description = "Tool for migrating Brage bundles"
)
public class BrageMigrationCommand implements Callable<Integer> {

    public static final String PATH_DELIMITER = "/";
    public static final String OUTPUT_JSON_FILENAME = "records.json";
    private static final Logger logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
    private static final String RESCUE_HANDLE_MAPPING_NVE = "src/main/resources/nve_handles_for_datasets.csv";
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;
    @Option(names = {"-c", "--customer"}, required = true, description = "customer id in NVA")
    private String customer;

    @Option(names = {"-z",
        "--zip-files"}, arity = "1..*", description = "input zipfiles containing brage bundles")
    private String[] zipFiles;

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            var brageProcessors = createBrageProcessorThread(zipFiles, customer);
            var brageProcessorThreads = brageProcessors.stream().map(Thread::new).collect(Collectors.toList());
            startProcessors(brageProcessorThreads);
            waitForAllProcesses(brageProcessorThreads);
            writeRecordsToFiles(brageProcessors);
            return NORMAL_EXIT_CODE;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ERROR_EXIT_CODE;
        }
    }

    private void writeRecordsToFiles(List<BrageProcessor> brageProcessors) {
        brageProcessors.forEach(this::writeRecordToFile);
    }

    private void writeRecordToFile(BrageProcessor brageProcessor) {
        var outputFileName = brageProcessor.getDestinationDirectory() + PATH_DELIMITER + OUTPUT_JSON_FILENAME;
        RecordsWriter.writeRecordsToFile(outputFileName, brageProcessor.getRecords());
    }

    private void waitForAllProcesses(List<Thread> brageProcessors) {
        brageProcessors.forEach(brageProcessor -> {
            try {
                brageProcessor.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Map<String, String> readNveTitleAndHandlesPatch() {

        try (BufferedReader bufferedReader =
                 Files.newBufferedReader(Paths.get(RESCUE_HANDLE_MAPPING_NVE),
                                         StandardCharsets.UTF_8)) {
            return new CSVReaderHeaderAware(bufferedReader).readMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startProcessors(List<Thread> brageProcessors) {
        brageProcessors.forEach(Thread::start);
    }

    private List<BrageProcessor> createBrageProcessorThread(String[] zipFiles, String customer) {
        var brageProcessorFactory = new BrageProcessorFactory(readNveTitleAndHandlesPatch());
        return
            Arrays.stream(zipFiles)
                .map(zipfile -> brageProcessorFactory.createBrageProcessor(zipfile, customer))
                .collect(Collectors.toList());
    }
}

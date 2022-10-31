package no.sikt.nva;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import no.sikt.nva.scrapers.HandleTitleMapReader;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
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
    public static final String FAILURE_IN_BRAGE_MIGRATION_COMMAND = "Failure in BrageMigration command";
    private static final Logger logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;

    private static final String NVE_DEV_CUSTOMER_ID =
        "https://api.dev.nva.aws.unit.no/customer/b4497570-2903-49a2-9c2a-d6ab8b0eacc2";
    @Option(names = {"-c", "--customer"},
        defaultValue = NVE_DEV_CUSTOMER_ID,
        description = "customer id in NVA")
    private String customer;

    @Option(names = {"-o", "--online-validator"}, description = "enable online validator, disabled if not present")
    private boolean enableOnlineValidation;

    @Option(names = {"-z", "--zip-files"}, arity = "1..*", description = "input zipfiles containing brage bundles")
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
            var customerUri = UriWrapper.fromUri(customer).getUri();
            var brageProcessors = createBrageProcessorThread(zipFiles, customerUri, enableOnlineValidation);
            var brageProcessorThreads = brageProcessors.stream().map(Thread::new).collect(Collectors.toList());
            startProcessors(brageProcessorThreads);
            waitForAllProcesses(brageProcessorThreads);
            writeRecordsToFiles(brageProcessors);
            return NORMAL_EXIT_CODE;
        } catch (Exception e) {
            logger.error(FAILURE_IN_BRAGE_MIGRATION_COMMAND, e);
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

    private void startProcessors(List<Thread> brageProcessors) {
        brageProcessors.forEach(Thread::start);
    }

    private List<BrageProcessor> createBrageProcessorThread(String[] zipFiles, URI customer,
                                                            boolean enableOnlineValidation) {
        var handleTitleMapReader = new HandleTitleMapReader();
        var brageProcessorFactory = new BrageProcessorFactory(handleTitleMapReader.readNveTitleAndHandlesPatch());
        return
            Arrays.stream(zipFiles)
                .map(zipfile -> brageProcessorFactory.createBrageProcessor(zipfile, customer, enableOnlineValidation))
                .collect(Collectors.toList());
    }
}

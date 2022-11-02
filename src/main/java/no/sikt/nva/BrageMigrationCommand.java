package no.sikt.nva;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.HandleTitleMapReader;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
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

    public static final String FOLLOWING_FIELDS_ARE_IGNORED = "The following fields are ignored: \n";
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

    @Option(names = {"-z", "--zip-files"}, description = "input zipfiles containing brage bundles, if none specified "
                                                         + "all zipfiles will be read based on samlingsfil.txt")
    private String[] zipFiles;

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    @Option(names = {"-no-handle-erros"}, description = "turn off handle errors. Invalid and missing handles does not"
                                                        + " get checked")
    private boolean noHandleCheck;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            printIgnoredDcValuesFieldsInInfoLog();
            var customerUri = UriWrapper.fromUri(customer).getUri();
            if (Objects.isNull(zipFiles)) {
                this.zipFiles = readZipfilesFromSamlingsfilTxt();
            }
            var brageProcessors = createBrageProcessorThread(zipFiles, customerUri, enableOnlineValidation,
                                                             noHandleCheck);
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

    private static String[] readZipfilesFromSamlingsfilTxt() {
        var zipfiles = new ArrayList<String>();
        File samlingsfil = new File("samlingsfil.txt");
        try (var scanner = new Scanner(samlingsfil)) {
            while(scanner.hasNextLine()) {
                var fileNamePartial = scanner.nextLine();
                if (StringUtils.isNotEmpty(fileNamePartial)) {
                    zipfiles.add(fileNamePartial + ".zip") ;
                }

            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  zipfiles.toArray(new String[0]);

    }

    private void printIgnoredDcValuesFieldsInInfoLog() {
        logger.info(FOLLOWING_FIELDS_ARE_IGNORED + DublinCoreScraper.getIgnoredFieldNames());
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
                                                            boolean enableOnlineValidation, boolean noHandleCheck) {
        var handleTitleMapReader = new HandleTitleMapReader();
        var brageProcessorFactory = new BrageProcessorFactory(handleTitleMapReader.readNveTitleAndHandlesPatch());
        return
            Arrays.stream(zipFiles)
                .map(zipfile -> brageProcessorFactory.createBrageProcessor(zipfile, customer, enableOnlineValidation,
                                                                           noHandleCheck))
                .collect(Collectors.toList());
    }
}

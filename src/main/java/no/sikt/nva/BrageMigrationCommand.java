package no.sikt.nva;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.awsconnection.WriteFileTos3;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.logutils.LogSetup;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.HandleTitleMapReader;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import software.amazon.awssdk.services.s3.S3Client;

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
    public static final String INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY =
        "Both specified zipfiles and starting directory cannot be set at "
        + "the same time";
    public static final String RECORDS_WITHOUT_ERRORS = "Records without errors: ";
    public static final String SLASH = "/";
    private static final String DEFAULT_EMBARGO_FILE_NAME = "FileEmbargo.txt";
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;
    private static final String NVE_DEV_CUSTOMER_ID =
        "https://api.dev.nva.aws.unit.no/customer/b4497570-2903-49a2-9c2a-d6ab8b0eacc2";
    private static final String COLLECTION_FILENAME = "samlingsfil.txt";
    private static final String ZIP_FILE_ENDING = ".zip";
    private static final Logger logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
    private final S3Client s3Client;
    @Option(names = {"-c", "--customer"},
        defaultValue = NVE_DEV_CUSTOMER_ID,
        description = "customer id in NVA")
    private String customer;
    @Option(names = {"-ov", "--online-validator"}, description = "enable online validator, disabled if not present")
    private boolean enableOnlineValidation;
    @Parameters(description = "input zipfiles containing brage bundles, if none specified "
                              + "all zipfiles will be read based on samlingsfil.txt")
    private String[] zipFiles;
    @Option(names = {"-D", "--directory"}, description = "Directory to search for samlingsfil.txt and FileEmbargo.txt. "
                                                         + "This option cannot be set at the same time as specified "
                                                         + "zipfiles")
    private String startingDirectory;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;
    @Option(names = {"-O", "--output-directory"}, description = "result outputdirectory.")
    private String userSpecifiedOutputDirectory;
    @Option(names = {"-a", "--do-not-write-to-aws"}, description = "If this flag is set, result will not "
                                                                   + "be pushed "
                                                                   + "to S3")
    private boolean shouldNotWriteToAws;
    @Option(names = {"-no-handle-erros"}, description = "turn off handle errors. Invalid and missing handles does not"
                                                        + " get checked")
    private boolean noHandleCheck;

    public BrageMigrationCommand() {
        this.s3Client = S3Driver.defaultS3Client().build();
    }

    public BrageMigrationCommand(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            checkForIllegalArguments();
            var inputDirectory = StringUtils.isNotEmpty(startingDirectory)
                                     ? startingDirectory + "/"
                                     : StringUtils.EMPTY_STRING;
            var outputDirectory = StringUtils.isNotEmpty(userSpecifiedOutputDirectory)
                                      ? userSpecifiedOutputDirectory + "/"
                                      : inputDirectory;
            var logOutPutDirectory = getLogOutputDirectory(inputDirectory, outputDirectory);
            /* IMPORTANT: DO NOT USE LOGGER BEFORE THIS METHOD HAS RUN: */
            LogSetup.setupLogging(logOutPutDirectory);
            List<Embargo> embargoes;
            if (isNull(zipFiles)) {
                this.zipFiles = readZipFileNamesFromCollectionFile(inputDirectory);
                embargoes = getEmbargoes(inputDirectory);
            } else {
                embargoes = getEmbargoes(Arrays.stream(zipFiles));
            }
            checkIfZipFilesInCollectionFileArePresent(inputDirectory);
            var customerUri = UriWrapper.fromUri(customer).getUri();

            printIgnoredDcValuesFieldsInInfoLog();
            var brageProcessors = getBrageProcessorThread(customerUri, outputDirectory, embargoes);
            var brageProcessorThreads = brageProcessors.stream().map(Thread::new).collect(Collectors.toList());
            startProcessors(brageProcessorThreads);
            waitForAllProcesses(brageProcessorThreads);
            writeRecordsToFiles(brageProcessors);
            logRecordCounter(brageProcessors);
            if (!shouldNotWriteToAws) {
                writeFileToS3();
            }
            return NORMAL_EXIT_CODE;
        } catch (Exception e) {
            var logger = LoggerFactory.getLogger(BrageProcessor.class);
            logger.error(FAILURE_IN_BRAGE_MIGRATION_COMMAND, e);
            return ERROR_EXIT_CODE;
        }
    }

    private static String getLogOutputDirectory(String inputDirectory, String outputDirectory) {
        if (inputDirectory.equals(outputDirectory)) {
            return outputDirectory;
        }
        return outputDirectory + inputDirectory;
    }

    private static String[] readZipFileNamesFromCollectionFile(String inputDirectory) {
        var zipfiles = new ArrayList<String>();
        var filenameWithPath = inputDirectory + COLLECTION_FILENAME;
        File collectionsInformationFile = new File(filenameWithPath);
        try (var scanner = new Scanner(collectionsInformationFile)) {
            while (scanner.hasNextLine()) {
                var fileNamePartial = scanner.nextLine();
                if (StringUtils.isNotEmpty(fileNamePartial)) {
                    zipfiles.add(inputDirectory + fileNamePartial + ZIP_FILE_ENDING);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return zipfiles.toArray(new String[0]);
    }

    private static void compareFileNamesWithActualFiles(String inputDirectory,
                                                        List<String> fileNamesFromCollectionFile) {
        var actualZipFiles = Arrays.stream(Objects.requireNonNull(new File(inputDirectory).listFiles()))
                                 .map(File::getName)
                                 .filter(filename -> filename.contains(".zip"))
                                 .collect(Collectors.toList());
        if (!actualZipFiles.containsAll(fileNamesFromCollectionFile)) {
            fileNamesFromCollectionFile.removeAll(actualZipFiles);
            logger.info("Following collections are missing: " + fileNamesFromCollectionFile);
        }
    }

    private void checkIfZipFilesInCollectionFileArePresent(String inputDirectory) {
        var fileNamesFromCollectionFile = Arrays.asList(zipFiles);
        var s = new File(inputDirectory).getPath();
        var files = new File(inputDirectory).listFiles();
        if (nonNull(files)) {
            compareFileNamesWithActualFiles(inputDirectory, fileNamesFromCollectionFile);
        }
    }

    private List<BrageProcessor> getBrageProcessorThread(URI customerUri, String outputDirectory,
                                                         List<Embargo> embargoes) {
        return createBrageProcessorThread(zipFiles,
                                          customerUri,
                                          enableOnlineValidation,
                                          noHandleCheck,
                                          outputDirectory, embargoes);
    }

    private List<Embargo> getEmbargoes(String directory) {
        try {
            var embargoFile = new File(directory + DEFAULT_EMBARGO_FILE_NAME);
            return EmbargoScraper.getEmbargoList(embargoFile);
        } catch (IOException e) {
            //TODO: do something with this exception
            return List.of();
        }
    }

    private List<Embargo> getEmbargoes(Stream<String> zipfiles) {
        return zipfiles.map(this::getDirectory)
                   .map(this::getEmbargoes)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }

    private String getDirectory(String zipfile) {
        var zipfileName = Path.of(zipfile).getFileName().toString();
        return zipfile.substring(0, zipfile.indexOf(zipfileName));
    }

    private void writeFileToS3() {
        var awsFileWriter = new WriteFileTos3(s3Client);
        var testFile = new File("samlingsfil.txt");
        awsFileWriter.writeFileToS3(testFile, "wohoo/pushet fil");
    }

    private void logRecordCounter(List<BrageProcessor> brageProcessors) {
        var counterWithoutErrors = 0;
        var totalCounter = 0;
        for (BrageProcessor brageProcessor : brageProcessors) {
            if (nonNull(brageProcessor.getRecords())) {
                for (Record record : brageProcessor.getRecords()) {
                    if (record.getErrors().isEmpty()) {
                        counterWithoutErrors++;
                    }
                    totalCounter++;
                }
            }
        }
        var logger = LoggerFactory.getLogger(BrageProcessor.class);
        logger.info(RECORDS_WITHOUT_ERRORS + counterWithoutErrors + SLASH + totalCounter);
    }

    private void checkForIllegalArguments() {
        if (nonNull(zipFiles) && zipFiles.length > 0 && StringUtils.isNotEmpty(startingDirectory)) {
            throw new IllegalArgumentException(INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY);
        }
    }

    private void printIgnoredDcValuesFieldsInInfoLog() {
        var logger = LoggerFactory.getLogger(BrageProcessor.class);
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
                                                            boolean enableOnlineValidation, boolean noHandleCheck,
                                                            String outputDirectory, List<Embargo> embargoes) {
        var handleTitleMapReader = new HandleTitleMapReader();
        var brageProcessorFactory = new BrageProcessorFactory(handleTitleMapReader.readNveTitleAndHandlesPatch(),
                                                              embargoes);
        return
            Arrays.stream(zipFiles)
                .map(zipfile -> brageProcessorFactory.createBrageProcessor(zipfile, customer, enableOnlineValidation,
                                                                           noHandleCheck, outputDirectory))
                .collect(Collectors.toList());
    }
}

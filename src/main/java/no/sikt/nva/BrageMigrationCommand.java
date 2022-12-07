package no.sikt.nva;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import java.io.File;
import java.io.IOException;
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
import no.sikt.nva.brage.migration.aws.S3Storage;
import no.sikt.nva.brage.migration.aws.S3StorageImpl;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.logutils.LogSetup;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.HandleTitleMapReader;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import software.amazon.awssdk.services.s3.S3Client;

@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.GodClass"})
@JacocoGenerated
@Command(
    name = "Brage migration",
    description = "Tool for migrating Brage bundles"
)
public class BrageMigrationCommand implements Callable<Integer> {

    private AwsEnvironment awsEnvironment;

    public static final String PATH_DELIMITER = "/";
    public static final String OUTPUT_JSON_FILENAME = "records.json";
    public static final String FAILURE_IN_BRAGE_MIGRATION_COMMAND = "Failure in BrageMigration command";
    public static final String FOLLOWING_FIELDS_ARE_IGNORED = "The following fields are ignored: \n";
    public static final String INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY =
        "Both specified zipfiles and starting directory cannot be set at "
        + "the same time";
    public static final String RECORDS_WITHOUT_ERRORS = "Records without errors: ";
    public static final String SLASH = "/";
    public static final String DUPLICATE_MESSAGE = "Record was removed from import because of duplicate: ";
    private static final String DEFAULT_EMBARGO_FILE_NAME = "FileEmbargo.txt";
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;
    private static final String NVE_DEV_CUSTOMER_ID =
        "https://api.dev.nva.aws.unit.no/customer/b4497570-2903-49a2-9c2a-d6ab8b0eacc2";
    private static final String COLLECTION_FILENAME = "samlingsfil.txt";
    private static final String ZIP_FILE_ENDING = ".zip";
    private final S3Client s3Client;

    @Spec
    private CommandSpec spec;

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
    private boolean shouldWriteToAws;

    @Option(names = {"-b", "--write-processed-import-to-aws"}, description = "If this flag is set, processed result"
                                                                             + "will be pushed to S3")
    private boolean writeProcessedImportToAws;
    @Option(names = {"-no-handle-erros"}, description = "turn off handle errors. Invalid and missing handles does not"
                                                        + " get checked")
    private boolean noHandleCheck;
    private RecordStorage recordStorage;

    public BrageMigrationCommand() {
        this.s3Client = S3Driver.defaultS3Client().build();
    }

    public BrageMigrationCommand(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Option(names = {"-j", "--aws-bucket"}, description = "Name of AWS bucket to push result in  'experimental', "
                                                          + "'sandbox', and 'develop' are valid",
        defaultValue = "experimental")
    public void setAwsEnvironment(String value) {
        this.awsEnvironment = AwsEnvironment.fromValue(value);
        if (isNull(awsEnvironment)) {
            throw new ParameterException(spec.commandLine(), String.format("Invalid value '%s' for option "
                                                                           + "'--aws-bucket'", value));
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            this.recordStorage = new RecordStorage();
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
            if (writeProcessedImportToAws) {
                pushExistingResourcesToNva(readZipFileNamesFromCollectionFile(inputDirectory));
            } else {
                List<Embargo> embargoes;
                if (isNull(zipFiles)) {
                    this.zipFiles = readZipFileNamesFromCollectionFile(inputDirectory);
                    embargoes = getEmbargoes(inputDirectory);
                } else {
                    embargoes = getEmbargoes(Arrays.stream(zipFiles));
                }
                printIgnoredDcValuesFieldsInInfoLog();
                var brageProcessors = getBrageProcessorThread(customer, outputDirectory, embargoes);
                var brageProcessorThreads = brageProcessors.stream().map(Thread::new).collect(Collectors.toList());
                startProcessors(brageProcessorThreads);
                waitForAllProcesses(brageProcessorThreads);
                writeRecordsToFiles(brageProcessors);

                if (shouldWriteToAws) {
                    pushToNva(brageProcessors);
                    storeLogsToNva();
                }
                var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
                logger.info("Records written to file: " + RecordsWriter.getCounter());
                logRecordCounter(brageProcessors);
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

    @SuppressWarnings("PMD.UseVarargs")
    private void pushExistingResourcesToNva(String[] collections) {
        S3Storage storage = new S3StorageImpl(s3Client, "CUSTOMER", awsEnvironment.getValue());
        storage.storeProcessedCollections(collections);
    }

    private void pushToNva(List<BrageProcessor> brageProcessors) {
        brageProcessors.stream()
            .map(BrageProcessor::getRecords)
            .filter(Objects::nonNull)
            .forEach(list -> list.forEach(this::storeFileToNVA));
    }

    private List<BrageProcessor> getBrageProcessorThread(String customer, String outputDirectory,
                                                         List<Embargo> embargoes) {
        return createBrageProcessorThread(zipFiles,
                                          customer,
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
        var zipFileName = Path.of(zipfile).getFileName().toString();
        return zipfile.substring(0, zipfile.indexOf(zipFileName));
    }

    private void storeFileToNVA(Record record) {
        S3Storage storage = new S3StorageImpl(s3Client, getCustomerShortName(), awsEnvironment.getValue());
        storage.storeRecord(record);
    }

    private String getCustomerShortName() {
        if (NVE_DEV_CUSTOMER_ID.equals(customer)) {
            return "NVE";
        } else {
            return "CUSTOMER";
        }
    }

    private void storeLogsToNva() {
        S3Storage storage = new S3StorageImpl(s3Client, getCustomerShortName(), awsEnvironment.getValue());
        storage.storeLogs();
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
        var records = removeIdenticalRecords(brageProcessor.getRecords());
        RecordsWriter.writeRecordsToFile(outputFileName, records);
    }

    private List<Record> removeIdenticalRecords(List<Record> records) {
        if (nonNull(records) && !records.isEmpty()) {
            var duplicates = findDuplicates(records);
            records.removeAll(duplicates);
        }
        return records;
    }

    private List<Record> findDuplicates(List<Record> records) {
        List<Record> recordsToRemove = new ArrayList<>();
        for (Record record : records) {
            var alreadyRegisteredHandles = recordStorage.getRecords().stream()
                                               .map(Record::getId)
                                               .collect(Collectors.toList());
            if (alreadyRegisteredHandles.contains(record.getId())) {
                var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
                logger.info(DUPLICATE_MESSAGE
                            + record.getId() + " " + record.getBrageLocation()
                            + "  =>  EXISTING RESOURCE IS: "
                            + recordStorage.getRecordLocationStringById(record.getId()));
                recordsToRemove.add(record);
            } else {
                recordStorage.getRecords().add(record);
            }
        }
        return recordsToRemove;
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

    private List<BrageProcessor> createBrageProcessorThread(String[] zipFiles, String customer,
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

    public enum AwsEnvironment {
        EXPERIMENTAL("experimental"),
        SANDBOX("sandbox"),
        DEVELOP("develop");

        private final String value;

        AwsEnvironment(String type) {
            this.value = type;
        }

        public static AwsEnvironment fromValue(String v) {
            for (AwsEnvironment c : AwsEnvironment.values()) {
                if (c.getValue().equalsIgnoreCase(v)) {
                    return c;
                }
            }
            return null;
        }

        public String getValue() {
            return value;
        }
    }
}

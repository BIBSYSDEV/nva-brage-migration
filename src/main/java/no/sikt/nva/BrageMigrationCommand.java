package no.sikt.nva;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.aws.S3Storage;
import no.sikt.nva.brage.migration.aws.S3StorageImpl;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.scrapers.AffiliationType;
import no.sikt.nva.scrapers.AffiliationsScraper;
import no.sikt.nva.scrapers.ContributorScraper;
import no.sikt.nva.scrapers.CustomerMapper;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.EmbargoParser;
import no.sikt.nva.scrapers.EmbargoScraper;
import no.sikt.nva.scrapers.HandleTitleMapReader;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import software.amazon.awssdk.services.s3.S3Client;

@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.GodClass", "PMD.TooManyFields", "PMD.ExcessiveParameterList"})
@JacocoGenerated
@Command(name = "Brage migration", description = "Tool for migrating Brage bundles")
public class BrageMigrationCommand implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
    public static final String PATH_DELIMITER = "/";
    public static final String OUTPUT_JSON_FILENAME = "records.json";
    public static final String FAILURE_IN_BRAGE_MIGRATION_COMMAND = "Failure in BrageMigration command";
    public static final String FOLLOWING_FIELDS_ARE_IGNORED = "The following fields will not be migrated:  \n";
    public static final String INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY =
        "Both specified zipfiles and starting directory cannot be set at "
        + "the same time";
    public static final String RECORDS_WITHOUT_ERRORS = "Records without errors: ";
    public static final String SLASH = "/";
    public static final String EMBARGO_COUNTER_MESSAGE = "Records removed from import because of embargo: ";
    public static final String RECORDS_WRITER_MESSAGE = "Records written to file: ";
    public static final String DEFAULT_CONTRIBUTORS_FILE_NAME = "contributors.txt";
    public static final String COULD_NOT_EXTRACT_CONTRIBUTORS = "Could not extract contributors";
    public static final String DEFAULT_LOCATION = "/brageexports/";
    public static final String OUTPUT = "output";
    public static final String FAILURE_IN_BRAGE_PROCESS = "FAILURE IN BRAGE PROCESS";
    private static final String DEFAULT_EMBARGO_FILE_NAME = "FileEmbargo.txt";
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;
    private static final String COLLECTION_FILENAME = "samlingsfil.txt";
    private static final String ZIP_FILE_ENDING = ".zip";
    private static final List<String> handles = Collections.synchronizedList(new ArrayList<>());
    public static final String CUSTOMER_ARGUMENT_SHORT = "-c";
    public static final String CUSTOMER_SYSTEM_PROPERTY = "customer";
    public static final String CUSTOMER_ARGUMENT_LONG = "--customer";
    public static final String OUTPUT_DIR_ARGUMENT_SHORT = "-O";
    public static final String OUTPUT_DIR_ARGUMENT_LONG = "--output-directory";
    public static final String OUTPUT_DIR_SYSTEM_PROPERTY = "outputDir";
    public static final String AFFILIATIONS_FILE = "affiliations.txt";
    public static final String CUSTOMER_ID_DOES_NOT_EXIST_FOR_THIS_ENVIRONMENT = "Customer id does not exist for this environment: ";
    private final S3Client s3Client;
    private AwsEnvironment awsEnvironment;
    @Spec
    private CommandSpec spec;
    @Option(names = {CUSTOMER_ARGUMENT_SHORT,
        CUSTOMER_ARGUMENT_LONG}, description = "customer id in NVA", required = true)
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
    @Option(names = {OUTPUT_DIR_ARGUMENT_SHORT,
        OUTPUT_DIR_ARGUMENT_LONG}, description = "result outputdirectory.")
    private String userSpecifiedOutputDirectory;
    @Option(names = {"-a", "--should-write-to-aws"}, description = "If this flag is set, result will "
                                                                   + "be pushed "
                                                                   + "to S3")
    private boolean shouldWriteToAws;
    @Option(names = {"-r", "--should-look-up-in-channel-register"}, description = "If this flag is set, will look "
                                                                                  + "up in channel register")
    private boolean shouldLookUpInChannelRegister;
    @Option(names = {"-b", "--write-processed-import-to-aws"}, description = "If this flag is set, processed result"
                                                                             + "will be pushed to S3")
    private boolean writeProcessedImportToAws;
    @Option(names = {"-no-handle-erros"}, description = "turn off handle errors. Invalid and missing handles does not"
                                                        + " get checked")
    private boolean noHandleCheck;
    @Option(names = {"-u"}, description = "Run import of unzipped collections")
    private boolean isUnzipped;
    private RecordStorage recordStorage;

    public BrageMigrationCommand() {
        this.s3Client = S3Driver.defaultS3Client().build();
    }

    public BrageMigrationCommand(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public static void main(String[] args) {
        setSystemPropertiesForLogFiles(args);
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
    }

    private static void setSystemPropertiesForLogFiles(String... args) {
        var arguments = Arrays.stream(args).collect(Collectors.toList());

        System.setProperty(CUSTOMER_SYSTEM_PROPERTY,
                           getArgument(arguments, CUSTOMER_ARGUMENT_SHORT, CUSTOMER_ARGUMENT_LONG).orElseThrow());

        var outputDir = getArgument(arguments,
                                    OUTPUT_DIR_ARGUMENT_SHORT,
                                    OUTPUT_DIR_ARGUMENT_LONG).orElse("");

        System.setProperty(OUTPUT_DIR_SYSTEM_PROPERTY, outputDir.isEmpty() ? "" : addTrailingSlash(outputDir));
    }

    private static String addTrailingSlash(String input) {
        return input.endsWith("/") ? input : input + "/";
    }

    private static Optional<String> getArgument(List<String> arguments, String argumentShort, String argumentLong) {
        var valueArgument = arguments.indexOf(argumentShort);
        if (valueArgument == -1) {
            valueArgument = arguments.indexOf(argumentLong);
        }
        if (valueArgument == -1) {
            return Optional.empty();
        }
        return Optional.of(arguments.get(valueArgument + 1));
    }

    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public static synchronized boolean alreadyProcessed(String handle) {
        if (handles.contains(handle)) {
            return true;
        }
        handles.add(handle);
        return false;
    }

    @Option(names = {"-j", "--aws-bucket"}, description = "Name of AWS bucket to push result in  'experimental', "
                                                          + "'sandbox', and 'develop' are valid", defaultValue = "experimental")
    public void setAwsEnvironment(String value) {
        this.awsEnvironment = AwsEnvironment.fromValue(value);
        if (isNull(awsEnvironment)) {
            throw new ParameterException(spec.commandLine(),
                                         String.format("Invalid value '%s' for option " + "'--aws-bucket'", value));
        }
    }

    @Override
    public Integer call() {
        try {
            this.recordStorage = new RecordStorage();
            checkForIllegalArguments();
            checkThatCustomerIsValid();
            var inputDirectory = generateInputDirectory();
            var outputDirectory = generateOutputDirectory();
            if (writeProcessedImportToAws) {
                pushExistingResourcesToNva(readZipFileNamesFromCollectionFile(inputDirectory));
            } else {
                Map<String, List<Embargo>> embargoes;
                if (isNull(zipFiles)) {
                    this.zipFiles = readZipFileNamesFromCollectionFile(inputDirectory);
                    embargoes = getEmbargoes(inputDirectory);
                } else {
                    embargoes = getEmbargoes(Arrays.stream(zipFiles));
                }
                var contributors = getContributors(inputDirectory);
                var affiliations = AffiliationsScraper.getAffiliations(new File(inputDirectory + AFFILIATIONS_FILE));
                printIgnoredDcValuesFieldsInInfoLog();
                var brageProcessors = getBrageProcessorThread(customer, outputDirectory, embargoes, contributors,
                                                              affiliations,
                                                              isUnzipped);
                //                Synchronized run:
                brageProcessors.forEach(this::runAndIgnoreException);
                //                Parellallization run:
                //                var brageProcessorThreads = brageProcessors.stream().map(Thread::new).collect
                //                (Collectors.toList());
                //                startProcessors(brageProcessorThreads);
                //                waitForAllProcesses(brageProcessorThreads);
                EmbargoParser.logNonEmbargosDetected(embargoes);
                writeRecordsToFiles(brageProcessors);
                if (shouldWriteToAws) {
                    pushToNva(brageProcessors);
                    storeLogsToNva();
                }
                log(brageProcessors);
            }
            return NORMAL_EXIT_CODE;
        } catch (Exception e) {
            var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
            logger.error(FAILURE_IN_BRAGE_MIGRATION_COMMAND, e);
            return ERROR_EXIT_CODE;
        }
    }

    private void checkThatCustomerIsValid() {
        if (AwsEnvironment.TEST.equals(awsEnvironment)
            || AwsEnvironment.PROD.equals(awsEnvironment)) {
            var customerUri = new CustomerMapper().getCustomerUri(customer, awsEnvironment.getValue());
            if (isNull(customerUri)) {
                logger.error(CUSTOMER_ID_DOES_NOT_EXIST_FOR_THIS_ENVIRONMENT + customer);
                throw new IllegalArgumentException(CUSTOMER_ID_DOES_NOT_EXIST_FOR_THIS_ENVIRONMENT + customer);
            }
        }
    }

    private static Integer getEmbargoCounter(List<BrageProcessor> brageProcessors) {
        return brageProcessors.stream().map(BrageProcessor::getEmbargoCounter).reduce(0, Integer::sum);
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

    private static Map<String, Contributor> extractContributorsFromFile(File contributorsFile) {
        if (contributorsFile.exists()) {
            return ContributorScraper.getContributors(contributorsFile);
        } else {
            return Map.of();
        }
    }

    private void runAndIgnoreException(BrageProcessor brageProcessor) {
        try {
            brageProcessor.run();
        } catch (Exception e) {
            var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
            logger.error(brageProcessor.getDestinationDirectory());
            logger.error(FAILURE_IN_BRAGE_PROCESS, e);
        }
    }

    private String generateInputDirectory() {
        if (StringUtils.isBlank(startingDirectory) && StringUtils.isBlank(customer)) {
            return StringUtils.EMPTY_STRING;
        }
        if (StringUtils.isBlank(startingDirectory)) {
            return DEFAULT_LOCATION + customer + PATH_DELIMITER;
        }
        if (StringUtils.isNotBlank(startingDirectory)) {
            return startingDirectory + "/";
        } else {
            return StringUtils.EMPTY_STRING;
        }
    }

    private String generateOutputDirectory() {
        if (StringUtils.isBlank(userSpecifiedOutputDirectory) && StringUtils.isBlank(customer)) {
            return userSpecifiedOutputDirectory;
        }
        if (StringUtils.isBlank(userSpecifiedOutputDirectory)) {
            return DEFAULT_LOCATION + OUTPUT + customer + PATH_DELIMITER;
        }
        if (StringUtils.isBlank(userSpecifiedOutputDirectory)) {
            return StringUtils.EMPTY_STRING;
        } else {
            return userSpecifiedOutputDirectory;
        }
    }

    private void log(List<BrageProcessor> brageProcessors) {
        var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
        logger.info(RECORDS_WRITER_MESSAGE + RecordsWriter.getCounter());
        logger.info(EMBARGO_COUNTER_MESSAGE + getEmbargoCounter(brageProcessors));
        logRecordCounter(brageProcessors);
    }

    @SuppressWarnings("PMD.UseVarargs")
    private void pushExistingResourcesToNva(String[] collections) {
        S3Storage storage = new S3StorageImpl(s3Client, userSpecifiedOutputDirectory,
                                              customer, awsEnvironment.getValue());
        storage.storeProcessedCollections(collections);
    }

    private void pushToNva(List<BrageProcessor> brageProcessors) {
        var recordList = brageProcessors.stream()
                             .map(BrageProcessor::getRecords)
                             .filter(Objects::nonNull)
                             .flatMap(List::stream)
                             .collect(Collectors.toList());
        var counter = 0;
        for (Record record : recordList) {
            storeFileToNVA(record);
            counter++;
        }
        var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
        logger.info("Records pushed to AWS: " + counter);
    }

    private List<BrageProcessor> getBrageProcessorThread(String customer, String outputDirectory,
                                                         Map<String, List<Embargo>> embargoes,
                                                         Map<String, Contributor> contributors,
                                                         AffiliationType affiliations, boolean isUnzipped) {
        return createBrageProcessorThread(zipFiles, customer, enableOnlineValidation, shouldLookUpInChannelRegister,
                                          noHandleCheck, outputDirectory, embargoes, contributors, affiliations,
                                          isUnzipped);
    }

    private Map<String, List<Embargo>> getEmbargoes(String directory) {
        var embargoFile = new File(directory + DEFAULT_EMBARGO_FILE_NAME);
        return EmbargoScraper.getEmbargoList(embargoFile);
    }

    private Map<String, Contributor> getContributors(String directory) {
        try {
            var contributorsFile = new File(directory + DEFAULT_CONTRIBUTORS_FILE_NAME);
            return extractContributorsFromFile(contributorsFile);
        } catch (Exception e) {
            var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
            logger.info(COULD_NOT_EXTRACT_CONTRIBUTORS + e);
            return Map.of();
        }
    }

    private Map<String, List<Embargo>> getEmbargoes(Stream<String> zipfiles) {
        return zipfiles.map(this::getDirectory)
                   .map(this::getEmbargoes)
                   .flatMap(map -> map.entrySet().stream())
                   .collect(
                       Collectors.toMap(
                           Map.Entry::getKey,
                           Map.Entry::getValue,
                           this::mergeValues, // merge function to handle possible duplicates
                           HashMap::new));
    }

    private List<Embargo> mergeValues(List<Embargo> v1, List<Embargo> v2) {
        var values = new HashSet<Embargo>();
        values.addAll(v1);
        values.addAll(v2);
        return new ArrayList<>(values);
    }

    private String getDirectory(String zipfile) {
        var zipFileName = Path.of(zipfile).getFileName().toString();
        return zipfile.substring(0, zipfile.indexOf(zipFileName));
    }

    private void storeFileToNVA(Record record) {
        S3Storage storage = new S3StorageImpl(s3Client, userSpecifiedOutputDirectory,
                                              customer, awsEnvironment.getValue());
        storage.storeRecord(record);
    }

    private void storeLogsToNva() {
        S3Storage storage = new S3StorageImpl(s3Client, userSpecifiedOutputDirectory + "/",
                                              customer, awsEnvironment.getValue());
        storage.storeLogs(customer);
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
        var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
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
        var outputFileName = brageProcessor.getDestinationDirectory()
                                 .replaceAll(StringUtils.SPACE, StringUtils.EMPTY_STRING)
                             + PATH_DELIMITER
                             + OUTPUT_JSON_FILENAME;
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
            var alreadyRegisteredHandles = recordStorage.getRecords()
                                               .stream()
                                               .map(Record::getId)
                                               .collect(Collectors.toList());
            if (alreadyRegisteredHandles.contains(record.getId())) {
                recordsToRemove.add(record);
            } else {
                recordStorage.getRecords().add(record);
            }
        }
        return recordsToRemove;
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void waitForAllProcesses(List<Thread> brageProcessors) {
        brageProcessors.forEach(brageProcessor -> {
            try {
                brageProcessor.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void startProcessors(List<Thread> brageProcessors) {
        brageProcessors.forEach(Thread::start);
    }

    private List<BrageProcessor> createBrageProcessorThread(String[] zipFiles, String customer,
                                                            boolean enableOnlineValidation,
                                                            boolean shouldLookUpInChannelRegister,
                                                            boolean noHandleCheck, String outputDirectory,
                                                            Map<String, List<Embargo>> embargoes,
                                                            Map<String, Contributor> contributors,
                                                            AffiliationType affiliations, boolean isUnzipped) {
        var handleTitleMapReader = new HandleTitleMapReader();
        var brageProcessorFactory = new BrageProcessorFactory(handleTitleMapReader.readNveTitleAndHandlesPatch(),
                                                              embargoes, contributors, affiliations);
        return Arrays.stream(zipFiles)
                   .filter(StringUtils::isNotBlank)
                   .map(zipfile -> brageProcessorFactory.createBrageProcessor(zipfile, customer, enableOnlineValidation,
                                                                              shouldLookUpInChannelRegister,
                                                                              noHandleCheck, awsEnvironment.getValue(),
                                                                              outputDirectory, isUnzipped))
                   .collect(Collectors.toList());
    }

    public enum AwsEnvironment {
        EXPERIMENTAL("experimental"), SANDBOX("sandbox"), DEVELOP("dev"), TEST("test"), PROD("prod");

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

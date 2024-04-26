package no.sikt.nva;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import no.sikt.nva.brage.migration.aws.S3StorageValidator;
import no.sikt.nva.brage.migration.aws.StorageValidatorException;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.scrapers.AffiliationType;
import no.sikt.nva.scrapers.AffiliationsScraper;
import no.sikt.nva.scrapers.ContributorScraper;
import no.sikt.nva.scrapers.CustomerMapper;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.embargo.EmbargoParser;
import no.sikt.nva.scrapers.embargo.EmbargoScraper;
import no.sikt.nva.scrapers.embargo.OnlineEmbargoChecker;
import no.sikt.nva.scrapers.embargo.OnlineEmbargoCheckerImpl;
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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.GodClass", "PMD.TooManyFields", "PMD.ExcessiveParameterList",
    "PMD.UnusedPrivateMethod"})
@JacocoGenerated
@Command(name = "Brage migration", description = "Tool for migrating Brage bundles")
public class BrageMigrationCommand implements Callable<Integer> {

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
    public static final String CUSTOMER_ARGUMENT_SHORT = "-c";
    public static final String CUSTOMER_SYSTEM_PROPERTY = "customer";
    public static final String CUSTOMER_ARGUMENT_LONG = "--customer";
    public static final String OUTPUT_DIR_ARGUMENT_SHORT = "-O";
    public static final String OUTPUT_DIR_ARGUMENT_LONG = "--output-directory";
    public static final String OUTPUT_DIR_SYSTEM_PROPERTY = "outputDir";
    public static final String AFFILIATIONS_FILE = "affiliations.txt";
    public static final String CUSTOMER_ID_DOES_NOT_EXIST_FOR_THIS_ENVIRONMENT = "Customer id does not exist for this"
                                                                                 + " environment: ";
    public static final String WRONG_ACCOUNT_MESSAGE = "You are connected to wrong aws account: ";
    private static final String PATTERN_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
                                                           .ofPattern(PATTERN_FORMAT)
                                                           .withZone(ZoneId.systemDefault());
    private static final String DEFAULT_EMBARGO_FILE_NAME = "FileEmbargo.txt";
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;
    private static final String COLLECTION_FILENAME = "samlingsfil.txt";
    private static final String ZIP_FILE_ENDING = ".zip";
    private static final List<String> handles = Collections.synchronizedList(new ArrayList<>());
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
    @Option(names = {"-u"}, description = "Run import of unzipped collections")
    private boolean isUnzipped;

    @Option(names = {"-s", "--samlingsfil"}, description = "Samlingsfilnavn")
    private String collectionFileName;

    private RecordStorage recordStorage;

    private final OnlineEmbargoChecker onlineEmbargoChecker;

    public BrageMigrationCommand() {
        this(S3Driver.defaultS3Client().build(),
             new OnlineEmbargoCheckerImpl());
    }

    public BrageMigrationCommand(S3Client s3Client,
                                 OnlineEmbargoChecker onlineEmbargoChecker) {
        this.s3Client = s3Client;
        this.onlineEmbargoChecker = onlineEmbargoChecker;
    }

    public static void main(String[] args) {
        setSystemPropertiesForLogFiles(args);
        var environment = getArgument(Arrays.asList(args), "-j", "--aws-bucket");
        var pushToAwsOnly = getPushToAwsOnlyArgument(args);
        var proceedAndPushToAws = getProceedAndPushToAwsArgument(args);
        if (pushToAwsOnly.isPresent() || proceedAndPushToAws.isPresent()) {
            validateAwsEnvironment(environment.orElse(null));
        }
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
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
            logStartingPoint();
            this.recordStorage = new RecordStorage();
            var collectionFileName = nonNull(this.collectionFileName) ? this.collectionFileName : COLLECTION_FILENAME;
            checkForIllegalArguments();
            checkThatCustomerIsValid();
// Temporarily commenting out the following section. It's currently causing issues with running the same archive in multiple parts.
//            if (writeProcessedImportToAws || shouldWriteToAws){
//                checkThatArchiveHasNotBeenPushedToAwsPreviously();
//            }
            var inputDirectory = generateInputDirectory();
            var outputDirectory = generateOutputDirectory();
            if (writeProcessedImportToAws) {
                pushExistingResourcesToNva(readZipFileNamesFromCollectionFile(inputDirectory, collectionFileName));
            } else {
                Map<String, List<Embargo>> embargoes;
                if (isNull(zipFiles)) {
                    this.zipFiles = readZipFileNamesFromCollectionFile(inputDirectory, collectionFileName);
                    embargoes = getEmbargoes(inputDirectory);
                } else {
                    embargoes = getEmbargoes(Arrays.stream(zipFiles));
                }
                var contributors = getContributors(inputDirectory);
                var affiliations = AffiliationsScraper.getAffiliations(new File(inputDirectory + AFFILIATIONS_FILE));
                printIgnoredDcValuesFieldsInInfoLog();
                onlineEmbargoChecker.calculateCustomerAddress(customer);
                onlineEmbargoChecker.setOutputDirectory(outputDirectory);
                var brageProcessors = getBrageProcessorThread(customer, outputDirectory, embargoes, contributors,
                                                              affiliations,
                                                              isUnzipped,
                                                              onlineEmbargoChecker);

                brageProcessors.forEach(this::runAndIgnoreException);
                EmbargoParser.logNonEmbargosDetected(embargoes);
                writeRecordsToFiles(brageProcessors);
                if (shouldWriteToAws) {
                    pushToNva(brageProcessors);
                    storeLogsToNva();
                    storeInputFilesToNva();
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

    private void checkThatArchiveHasNotBeenPushedToAwsPreviously() throws StorageValidatorException {
        new S3StorageValidator(s3Client, customer, awsEnvironment.value ).validateStorage();
    }

    private static void validateAwsEnvironment(String environment) {
        var accountId = getAccountId();
        var awsEnvironment = AwsEnvironment.fromValue(environment);
        if (doesNotBelongToCurrentAwsEnvironment(accountId, awsEnvironment)) {
            throw new IllegalArgumentException(
                WRONG_ACCOUNT_MESSAGE + AwsEnvironment.getEnvironmentById(accountId).getValue());
        }
    }

    private static boolean doesNotBelongToCurrentAwsEnvironment(String accountId, AwsEnvironment awsEnvironment) {
        return nonNull(accountId)
               && nonNull(awsEnvironment.getAccountId())
               && !accountId.equals(awsEnvironment.getAccountId());
    }

    private static String getAccountId() {
        return AWSSecurityTokenServiceClientBuilder
                   .standard()
                   .withRegion(Region.EU_WEST_1.id())
                   .build()
                   .getCallerIdentity(new GetCallerIdentityRequest())
                   .getAccount();
    }

    private static Optional<String> getProceedAndPushToAwsArgument(String... args) {
        return getArgument(Arrays.asList(args), "-a", "--should-write-to-aws");
    }

    private static Optional<String> getPushToAwsOnlyArgument(String... args) {
        return getArgument(Arrays.asList(args), "-b", "--write-processed-import-to-aws");
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

    private static void logStartingPoint() {
        //we need this because the users forgets to delete logs, and they don't want timestamp in logs.
        var startLogMessage = "\n\n================= Starting new import "
                              + FORMATTER.format(Instant.now())
                              + " ==================\n\n";
        var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
        logger.info(startLogMessage);
        logger.warn(startLogMessage);
        logger.error(startLogMessage);
    }

    private static Integer getEmbargoCounter(List<BrageProcessor> brageProcessors) {
        return brageProcessors.stream().map(BrageProcessor::getEmbargoCounter).reduce(0, Integer::sum);
    }

    private static String[] readZipFileNamesFromCollectionFile(String inputDirectory, String collectionFileName) {
        var zipfiles = new ArrayList<String>();
        var filenameWithPath = inputDirectory + collectionFileName;
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

    private void checkThatCustomerIsValid() {
        if (AwsEnvironment.TEST.equals(awsEnvironment)
            || AwsEnvironment.PROD.equals(awsEnvironment)) {
            var customerUri = new CustomerMapper().getCustomerUri(customer, awsEnvironment.getValue());
            if (isNull(customerUri)) {
                var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
                logger.error(CUSTOMER_ID_DOES_NOT_EXIST_FOR_THIS_ENVIRONMENT + customer);
                throw new IllegalArgumentException(CUSTOMER_ID_DOES_NOT_EXIST_FOR_THIS_ENVIRONMENT + customer);
            }
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
                                                         AffiliationType affiliations, boolean isUnzipped,
                                                         OnlineEmbargoChecker onlineEmbargoChecker) {
        return createBrageProcessorThread(zipFiles, customer, enableOnlineValidation, shouldLookUpInChannelRegister,
                                          outputDirectory, embargoes, contributors, affiliations,
                                          isUnzipped, onlineEmbargoChecker);
    }

    private Map<String, List<Embargo>> getEmbargoes(String directory) {
        var embargoFile = new File(directory + DEFAULT_EMBARGO_FILE_NAME);

        if (!embargoFile.exists()) {
            var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
            logger.error("Embargo File does not exist: " + embargoFile.getAbsolutePath());
            throw new RuntimeException("Embargo file does not exists");
        }
        return EmbargoScraper.getEmbargoes(embargoFile);
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

    private void storeInputFilesToNva() {
        S3Storage storage = new S3StorageImpl(s3Client, userSpecifiedOutputDirectory + "/",
                                              customer, awsEnvironment.getValue());
        storage.storeInputFile(startingDirectory, DEFAULT_EMBARGO_FILE_NAME);
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
            if (!duplicates.isEmpty()) {
                var logger = LoggerFactory.getLogger(BrageMigrationCommand.class);
                logger.error("Removing duplicates: {}",
                             duplicates.stream()
                                 .map(Record::getId)
                                 .map(URI::toString)
                                 .collect(Collectors.joining(System.lineSeparator())));
            }
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

    private List<BrageProcessor> createBrageProcessorThread(String[] zipFiles, String customer,
                                                            boolean enableOnlineValidation,
                                                            boolean shouldLookUpInChannelRegister,
                                                            String outputDirectory,
                                                            Map<String, List<Embargo>> embargoes,
                                                            Map<String, Contributor> contributors,
                                                            AffiliationType affiliations,
                                                            boolean isUnzipped,
                                                            OnlineEmbargoChecker onlineEmbargoChecker) {
        var brageProcessorFactory = new BrageProcessorFactory(embargoes, contributors, affiliations);
        return Arrays.stream(zipFiles)
                   .filter(StringUtils::isNotBlank)
                   .map(zipfile -> brageProcessorFactory.createBrageProcessor(zipfile,
                                                                              customer,
                                                                              enableOnlineValidation,
                                                                              shouldLookUpInChannelRegister,
                                                                              awsEnvironment.getValue(),
                                                                              outputDirectory,
                                                                              isUnzipped,
                                                                              onlineEmbargoChecker))
                   .collect(Collectors.toList());
    }

    public enum AwsEnvironment {
        EXPERIMENTAL("experimental"), SANDBOX("sandbox"), DEVELOP("dev"), TEST("test"), PROD("prod");
        private static final Map<AwsEnvironment, String> ID_MAP = Map.of(SANDBOX, "750639270376",
                                                                         DEVELOP, "884807050265",
                                                                         TEST, "812481234721",
                                                                         PROD, "755923822223");
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

        public static AwsEnvironment getEnvironmentById(String id) {
            return ID_MAP.entrySet()
                       .stream()
                       .filter(entry -> id.equals(entry.getValue()))
                       .map(Map.Entry::getKey).findFirst().orElseThrow();
        }

        public String getValue() {
            return value;
        }

        public String getAccountId() {
            return ID_MAP.get(this);
        }
    }
}

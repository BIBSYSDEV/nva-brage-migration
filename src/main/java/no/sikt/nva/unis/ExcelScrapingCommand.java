package no.sikt.nva.unis;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static nva.commons.core.StringUtils.isBlank;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import no.sikt.nva.BrageMigrationCommand.AwsEnvironment;
import no.sikt.nva.brage.migration.aws.S3Storage;
import no.sikt.nva.brage.migration.aws.S3StorageImpl;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@JacocoGenerated
@Command(name = "Excel scraper", description = "Tool for scraping an excel file of records")
public class ExcelScrapingCommand implements Callable<Integer> {

    public static final String WRONG_ACCOUNT_MESSAGE = "You are connected to wrong aws account: ";
    public static final String INVALID_EXCEL_FILE_MESSAGE = "Invalid excel file: ";
    public static final String FAILURE_IN_EXCEL_SCRAPER_COMMAND = "Failure in ExcelScraping command";
    public static final String CUSTOMER_SYSTEM_PROPERTY = "customer";
    public static final String OUTPUT_DIR_SYSTEM_PROPERTY = "outputDir";
    public static final String EXCEL_FILE_ARGUMENT_SHORT = "-e";
    public static final String EXCEL_FILE_ARGUMENT_LONG = "--excel-file";
    public static final String ENVIRONMENT_ARGUMENT_SHORT = "-j";
    public static final String ENVIRONMENT_ARGUMENT_LONG = "--aws-bucket";
    public static final String UNIS = "unis";
    public static final String EMPTY_STRING = "";
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;
    private final S3Client s3Client;
    private AwsEnvironment awsEnvironment;
    private URI excelFile;
    private URI excelFileFolder;

    @Spec
    private CommandSpec spec;

    public ExcelScrapingCommand() {
        this.s3Client = S3Driver.defaultS3Client().build();
    }

    public static void main(String[] args) throws URISyntaxException {
        validateArguments(args);
        setSystemPropertiesForLogFiles(args);
        int exitCode = new CommandLine(new ExcelScrapingCommand()).execute(args);
        System.exit(exitCode);
    }

    @Option(names = {EXCEL_FILE_ARGUMENT_SHORT, EXCEL_FILE_ARGUMENT_LONG},
        description = "The excel file with metadata. Associated files must be in the same directory")
    public void setFileLocations(String value) throws URISyntaxException {
        excelFile = new URI(value);
        excelFileFolder = getFolderFromUri(excelFile);
    }

    @Option(names = {ENVIRONMENT_ARGUMENT_SHORT, ENVIRONMENT_ARGUMENT_LONG},
        description = "Name of AWS bucket to push result in  'experimental', 'sandbox', and 'develop' are valid",
        defaultValue = "experimental")
    public void setAwsEnvironment(String value) {
        this.awsEnvironment = AwsEnvironment.fromValue(value);
        if (isNull(awsEnvironment)) {
            throw new ParameterException(spec.commandLine(),
                                         String.format("Invalid value '%s' for option '--aws-bucket'", value));
        }
    }

    @Override
    public Integer call() throws Exception {
        try {
            var records = ExcelScraper.toRecords(excelFile.toString());
            for (Record record : records) {
                storeFileToNVA(record);
            }

            var logger = LoggerFactory.getLogger(ExcelScrapingCommand.class);
            logger.info("Records pushed to AWS: " + records.size());

            return NORMAL_EXIT_CODE;
        } catch (Exception exc) {
            var logger = LoggerFactory.getLogger(ExcelScrapingCommand.class);
            logger.error(FAILURE_IN_EXCEL_SCRAPER_COMMAND, exc);
            return ERROR_EXIT_CODE;
        }
    }

    private static void setSystemPropertiesForLogFiles(String... args) throws URISyntaxException {
        var arguments = Arrays.stream(args).collect(Collectors.toList());

        System.setProperty(CUSTOMER_SYSTEM_PROPERTY, UNIS);

        var outputDir = getArgument(arguments, EXCEL_FILE_ARGUMENT_SHORT, EXCEL_FILE_ARGUMENT_LONG).orElse("");
        var folder = getFolderFromUri(new URI(outputDir));
        System.setProperty(OUTPUT_DIR_SYSTEM_PROPERTY, outputDir.isEmpty() ? "" : folder.toString());
    }

    private static Optional<String> getArgument(List<String> arguments, String argShort, String argLong) {
        var indexOfShort = arguments.indexOf(argShort);
        if (indexOfShort != -1) {
            return Optional.of(arguments.get(indexOfShort + 1));
        }

        var indexOfLong = arguments.indexOf(argLong);
        if (indexOfLong != -1) {
            return Optional.of(arguments.get(indexOfLong + 1));
        }

        return Optional.empty();
    }

    private static void validateArguments(String... args) {
        var environment = getArgument(Arrays.asList(args), ENVIRONMENT_ARGUMENT_SHORT, ENVIRONMENT_ARGUMENT_LONG);
        validateAwsEnvironment(environment.orElse(null));

        var excelFile = getArgument(Arrays.asList(args), EXCEL_FILE_ARGUMENT_SHORT, EXCEL_FILE_ARGUMENT_LONG);
        validateExcelFile(excelFile.orElse(null));
    }

    private static void validateAwsEnvironment(String environment) {
        var accountId = getAccountId();
        var awsEnvironment = AwsEnvironment.fromValue(environment);
        if (doesNotBelongToCurrentAwsEnvironment(accountId, awsEnvironment)) {
            throw new IllegalArgumentException(
                INVALID_EXCEL_FILE_MESSAGE + AwsEnvironment.getEnvironmentById(accountId).getValue());
        }
    }

    private static void validateExcelFile(String excelFile) {
        if (isBlank(excelFile)) {
            throw new IllegalArgumentException(INVALID_EXCEL_FILE_MESSAGE + excelFile);
        }
    }

    private static String getAccountId() {
        return AWSSecurityTokenServiceClientBuilder
                   .standard()
                   .withRegion(Region.EU_WEST_1.id())
                   .build()
                   .getCallerIdentity(new GetCallerIdentityRequest())
                   .getAccount();
    }

    private static boolean doesNotBelongToCurrentAwsEnvironment(String accountId, AwsEnvironment awsEnvironment) {
        return nonNull(accountId)
               && nonNull(awsEnvironment.getAccountId())
               && !accountId.equals(awsEnvironment.getAccountId());
    }

    private static URI getFolderFromUri(URI file) {
        return file.resolve(".");
    }

    private void storeFileToNVA(Record record) {
        S3Storage storage = new S3StorageImpl(s3Client, excelFileFolder.toString(), UNIS, awsEnvironment.getValue());
        storage.storeRecord(record);
    }
}

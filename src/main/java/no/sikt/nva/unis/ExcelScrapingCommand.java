package no.sikt.nva.unis;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
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
    public static final String FAILURE_IN_EXCEL_SCRAPER_COMMAND = "Failure in ExcelScraping command";
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;
    public static final String UNIS = "unis";

    private final S3Client s3Client;
    private AwsEnvironment awsEnvironment;
    private URI excelFile;
    private URI excelFileFolder;

    @Spec
    private CommandSpec spec;

    @Option(names = {"-e", "--excel-file"}, description = "The excel file with metadata. Associated "
                                                          + "files must be in the same directory")
    public void setFileLocations(String value) throws URISyntaxException {
        excelFile = new URI(value);
        excelFileFolder = excelFile.resolve(".");
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

    public ExcelScrapingCommand() {
        this.s3Client = S3Driver.defaultS3Client().build();
    }

    public static void main(String[] args) {
        var environment = getEnvironmentArgument(Arrays.asList(args));
        validateAwsEnvironment(environment.orElse(null));
        int exitCode = new CommandLine(new ExcelScrapingCommand()).execute(args);
        System.exit(exitCode);
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

    private static Optional<String> getEnvironmentArgument(List<String> arguments) {
        var indexOfShort = arguments.indexOf("-j");
        if (indexOfShort != -1) {
            return Optional.of(arguments.get(indexOfShort + 1));
        }

        var indexOfLong = arguments.indexOf("--aws-bucket");
        if (indexOfLong != -1) {
            return Optional.of(arguments.get(indexOfLong + 1));
        }

        return Optional.empty();
    }

    private static void validateAwsEnvironment(String environment) {
        var accountId = getAccountId();
        var awsEnvironment = AwsEnvironment.fromValue(environment);
        if (doesNotBelongToCurrentAwsEnvironment(accountId, awsEnvironment)) {
            throw new IllegalArgumentException(
                WRONG_ACCOUNT_MESSAGE + AwsEnvironment.getEnvironmentById(accountId).getValue());
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

    private void storeFileToNVA(Record record) {
        S3Storage storage = new S3StorageImpl(s3Client, excelFileFolder.toString(), UNIS, awsEnvironment.getValue());
        storage.storeRecord(record);
    }
}

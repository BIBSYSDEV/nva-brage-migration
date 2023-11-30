package no.sikt.nva;

import static no.sikt.nva.BrageMigrationCommand.INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY;
import static no.sikt.nva.RecordsWriter.WRITING_TO_JSON_FILE_HAS_FAILED;
import static no.sikt.nva.ResourceNameConstants.EMBARGO_TEST_DIRECTORY;
import static no.sikt.nva.ResourceNameConstants.EMPTY_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI;
import static no.sikt.nva.ResourceNameConstants.INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INPUT_WITH_LICENSE_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK;
import static no.sikt.nva.scrapers.HandleScraper.COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import java.util.ArrayList;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class BrageMigrationCommandTest {

    public static final String EXPECTED_EMBARGO_LOGG_MESSAGE = "FOLLOWING COLLECTION CONTAINS 1 EMBARGO";

    public static final String BUNDLE_WITH_FORWARD_SLASHES_ZIP = "bundleWithForwardSlashes.zip";
    public static final String PUSH_TO_AWS = "-a";
    private static final int NORMAL_EXIT_CODE = 0;
    private static List<String> arguments;

    @BeforeEach
    void beforeEach() {
        arguments = new ArrayList<>(List.of("-c", "someCustomer", "-O",
                                            "someOutputPath"));
    }

    @Test
    void shouldThrowExceptionWhenBothInputDirectoryAndZipfileIsSpecified() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        arguments.addAll(List.of("someZipfile.zip", "-D", "some/directory/path"));
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        assertThat(status, not(equalTo(NORMAL_EXIT_CODE)));
        assertThat(appender.getMessages(), containsString(INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY));
    }

    @Test
    void shouldProcessZipFileWithLicenseCorrectly() {
        arguments.add(TEST_RESOURCE_PATH + INPUT_WITH_LICENSE_ZIP_FILE_NAME);
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessFileWithoutHandleInDublinCoreFile() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        arguments.add(TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME);
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        assertThat(appender.getMessages(),
                   containsString(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV));
    }

    @Test
    void shouldProcessFileWithoutHandleInHandleFile() {
        arguments.add(TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME);
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessFileWithOnlineValidator() {
        arguments.addAll(List.of(TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME, "-ov"));
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldNotLoggDoiOfflineErrorWhenBundleContainsInvalidDoiWithValidDoiStructure() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        arguments.add(TEST_RESOURCE_PATH + INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI);
        new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        assertThat(appender.getMessages(),
                   not(containsString(String.valueOf(INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK))));
    }

    @Test
    void shouldLoggZipThatIsEmpty() {
        var appender = LogUtils.getTestingAppenderForRootLogger();

        arguments.add(TEST_RESOURCE_PATH + EMPTY_ZIP_FILE_NAME);
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        var messages = appender.getMessages();
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
        assertThat(messages,
                   containsString(Warning.EMPTY_COLLECTION.toString()));
        assertThat(messages,
                   not(containsString(WRITING_TO_JSON_FILE_HAS_FAILED)));
    }

    @Test
    void shouldBePossibleToSpecifySubDirectoryWith() {
        arguments.addAll(List.of("-D", TEST_RESOURCE_PATH));
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
    }

    @Test
    void shouldCreateRecordWithEmbargo() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        arguments.addAll(List.of("-D", EMBARGO_TEST_DIRECTORY));
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
        assertThat(appender.getMessages(), containsString(EXPECTED_EMBARGO_LOGG_MESSAGE));
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
    }

    @Test
    void shouldHandleContentFilesWithForwardSlashesInTheirNames() {
        arguments.add(TEST_RESOURCE_PATH + BUNDLE_WITH_FORWARD_SLASHES_ZIP);
        arguments.add(PUSH_TO_AWS);
        new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(
            arguments.toArray(String[]::new));
    }
}

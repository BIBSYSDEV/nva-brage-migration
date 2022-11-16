package no.sikt.nva;

import static no.sikt.nva.BrageMigrationCommand.INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY;
import static no.sikt.nva.ResourceNameConstants.EMPTY_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI;
import static no.sikt.nva.ResourceNameConstants.INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INPUT_WITH_LICENSE_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.UnZipper.HANDLE_FORMAT;
import static no.sikt.nva.UnZipper.UNZIPPING_FAILED_FOR_COLLECTION_WITH_HANDLE;
import static no.sikt.nva.scrapers.HandleScraper.COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import no.sikt.nva.model.ErrorDetails.Error;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.StringUtils;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class BrageMigrationCommandTest {

    private static final int NORMAL_EXIT_CODE = 0;

    @Test
    void shouldRunWhenZipFileOptionIsNotSet() {
        var arguments = new String[]{};
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
    }

    @Test
    void shouldThrowExceptionWhenBothInputDirectoryAndZipfileIsSpecified() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"someZipfile.zip", "-D", "some/directory/path"};
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(status, not(equalTo(NORMAL_EXIT_CODE)));
        assertThat(appender.getMessages(), containsString(INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY));
    }

    @Test
    void shouldProcessZipFileWithLicenseCorrectly() {
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WITH_LICENSE_ZIP_FILE_NAME};
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessFileWithoutHandleInDublinCoreFile() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME};
        new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(appender.getMessages(),
                   containsString(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV));
    }

    @Test
    void shouldProcessFileWithoutHandleInHandleFile() {
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME};
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessFileWithOnlineValidator() {
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME, "-ov"};
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldNotLoggDoiOfflineErrorWhenBundleContainsInvalidDoiWithValidDoiStructure() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI};
        new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(appender.getMessages(), not(containsString(String.valueOf(Error.INVALID_DOI_OFFLINE_CHECK))));
    }

    @Test
    void shouldLoggInvalidDoiErrorOnlineWhenDoiUriIsInvalid() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI,
            "-ov"};
        new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(appender.getMessages(), containsString(String.valueOf(Error.INVALID_DOI_ONLINE_CHECK)));
    }

    @Test
    void shouldLoggZipThatIsEmpty() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{TEST_RESOURCE_PATH + EMPTY_ZIP_FILE_NAME};
        var expectedCollectionHandle =
            String.format(HANDLE_FORMAT,
                          EMPTY_ZIP_FILE_NAME.replace(".zip", StringUtils.EMPTY_STRING));
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
        assertThat(appender.getMessages(),
                   containsString(String.format(UNZIPPING_FAILED_FOR_COLLECTION_WITH_HANDLE, expectedCollectionHandle
                   )));
    }

    @Test
    void shouldBePossibleToSpecifySubDirectoryWith() {
        var arguments = new String[]{"-D", TEST_RESOURCE_PATH};
        int status = new CommandLine(new BrageMigrationCommand(new FakeS3Client())).execute(arguments);
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
    }
}

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
import com.github.stefanbirkner.systemlambda.SystemLambda;
import no.sikt.nva.model.ErrorDetails.Error;
import nva.commons.core.StringUtils;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class BrageMigrationCommandTest {

    private static final int NORMAL_EXIT_CODE = 0;

    @Test
    void shouldRunWhenZipFileOptionIsNotSet() throws Exception {
        var arguments = new String[]{};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
    }

    @Test
    void shouldThrowExceptionWhenBothInputDirectoryAndZipfileIsSpecified() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"someZipfile.zip", "-D", "some/directory/path"};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, not(equalTo(NORMAL_EXIT_CODE)));
        assertThat(appender.getMessages(), containsString(INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY));
    }

    @Test
    void shouldProcessZipFileWithLicenseCorrectly() throws Exception {
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WITH_LICENSE_ZIP_FILE_NAME};
        var exit = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exit, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessFileWithoutHandleInDublinCoreFile() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(),
                   containsString(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV));
    }

    @Test
    void shouldProcessFileWithoutHandleInHandleFile() throws Exception {
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME};
        var exitCode = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exitCode, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessFileWithOnlineValidator() throws Exception {
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME, "-ov"};
        var exitCode = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exitCode, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldNotLoggDoiOfflineErrorWhenBundleContainsInvalidDoiWithValidDoiStructure() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), not(containsString(String.valueOf(Error.INVALID_DOI_OFFLINE_CHECK))));
    }

    @Test
    void shouldLoggInvalidDoiErrorOnlineWhenDoiUriIsInvalid() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{TEST_RESOURCE_PATH + INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI,
            "-ov"};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), containsString(String.valueOf(Error.INVALID_DOI_ONLINE_CHECK)));
    }

    @Test
    void shouldLoggZipThatIsEmpty() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{TEST_RESOURCE_PATH + EMPTY_ZIP_FILE_NAME};
        var expectedCollectionHandle = String.format(HANDLE_FORMAT, EMPTY_ZIP_FILE_NAME.replace(".zip",
                                                                                                StringUtils.EMPTY_STRING));
        var exitCode = SystemLambda.catchSystemExit(
            () -> BrageMigrationCommand.main(arguments));
        assertThat(exitCode, equalTo(NORMAL_EXIT_CODE));
        assertThat(appender.getMessages(),
                   containsString(String.format(UNZIPPING_FAILED_FOR_COLLECTION_WITH_HANDLE, expectedCollectionHandle
                   )));
    }

    @Test
    void shouldBePossibleToSpecifySubDirectoryWith() throws Exception {
        var arguments = new String[]{"-D", TEST_RESOURCE_PATH};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
    }
}

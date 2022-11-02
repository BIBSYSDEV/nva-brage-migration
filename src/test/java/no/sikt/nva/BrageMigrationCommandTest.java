package no.sikt.nva;

import static no.sikt.nva.ResourceNameConstants.INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INPUT_WITH_CRISTIN_ID_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI;
import static no.sikt.nva.ResourceNameConstants.INPUT_WITH_LICENSE_ZIP_FILE_NAME;
import static no.sikt.nva.model.ErrorDetails.Error.CRISTIN_ID_PRESENT;
import static no.sikt.nva.scrapers.HandleScraper.COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import no.sikt.nva.model.ErrorDetails.Error;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class BrageMigrationCommandTest {

    private static final int NORMAL_EXIT_CODE = 0;

    @Test
    void shouldReadSamlingsfilTxtWhenZipFileOptionIsNotSet() throws Exception {
        var arguments = new String[]{};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, equalTo(NORMAL_EXIT_CODE));
    }

    @Test
    void shouldProcessZipFileWithLicenseCorrectlyAndWithoutAnyLogMessages() throws Exception {
        var arguments = new String[]{"-z", "inputWithLicense.zip"};
        var exitCode = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exitCode, is(equalTo(0)));
    }

    @Test
    void shouldProcessZipFileWithLicenseCorrectly() throws Exception {
        var arguments = new String[]{"-z", INPUT_WITH_LICENSE_ZIP_FILE_NAME};
        var exit = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exit, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessAndLogFileWithCristinId() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-z", INPUT_WITH_CRISTIN_ID_FILE_NAME};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), containsString(String.valueOf(CRISTIN_ID_PRESENT)));
    }

    @Test
    void shouldProcessFileWithoutHandleInDublinCoreFile() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-z", INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(),
                   containsString(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV));
    }

    @Test
    void shouldProcessFileWithoutHandleInHandleFile() throws Exception {
        var arguments = new String[]{"-z", INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME};
        var exitCode = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exitCode, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessFileWithOnlineValidator() throws Exception {
        var arguments = new String[]{"-z", INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME, "-o"};
        var exitCode = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exitCode, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldNotLoggDoiOfflineErrorWhenBundleContainsInvalidDoiWithValidDoiStructure() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-z", INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), not(containsString(String.valueOf(Error.INVALID_DOI_OFFLINE_CHECK))));
    }

    @Test
    void shouldLoggInvalidDoiErrorOnlineWhenDoiUriIsInvalid() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-z", INPUT_WHERE_DOI_HAS_VALID_STRUCTURE_BUT_HAS_INVALID_URI, "-o"};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), containsString(String.valueOf(Error.INVALID_DOI_ONLINE_CHECK)));
    }
}

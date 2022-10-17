package no.sikt.nva;

import static no.sikt.nva.DublinCoreValidator.Error.CRISTIN_ID_PRESENT;
import static no.sikt.nva.DublinCoreValidator.Error.INVALID_ISBN;
import static no.sikt.nva.DublinCoreValidator.Error.INVALID_ISSN;
import static no.sikt.nva.HandleScraper.COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV;
import static no.sikt.nva.ResourceNameConstants.INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INPUT_WITH_LICENSE_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INVALID_FILE_INPUT_ZIP_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.TEST_INPUT_ZIP_FILE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class BrageMigrationCommandTest {

    public static final String NO_LICENSE_LOGG_MESSAGE = "No license in bundle found, default license used";
    private static final int NORMAL_EXIT_CODE = 0;

    @Test
    void shouldExitWhenCustomerOptionIsNotSet() throws Exception {
        var arguments = new String[]{"-z", "zipfile1.zip"};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, not(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldExitWhenZipFileOptionIsNotSet() throws Exception {
        var arguments = new String[]{"-c", "nve"};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, not(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessZipFileWithoutLicenseCorrectly() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", TEST_INPUT_ZIP_FILE_NAME};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), containsString(NO_LICENSE_LOGG_MESSAGE));
    }

    @Test
    void shouldProcessZipFileWithLicenseCorrectly() throws Exception {
        var arguments = new String[]{"-c", "nve", "-z", INPUT_WITH_LICENSE_ZIP_FILE_NAME};
        var exit = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exit, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessAndLogFileWithCristinId() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", INVALID_FILE_INPUT_ZIP_FILE_NAME};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), containsString(String.valueOf(CRISTIN_ID_PRESENT)));
    }

    @Test
    void shouldProcessAndLogFileWithInvalidIssn() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", INVALID_FILE_INPUT_ZIP_FILE_NAME};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), containsString(String.valueOf(INVALID_ISSN)));
    }

    @Test
    void shouldProcessAndLogFileWithInvalidIsbn() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", INVALID_FILE_INPUT_ZIP_FILE_NAME};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        var m = appender.getMessages();
        assertThat(appender.getMessages(), containsString(String.valueOf(INVALID_ISBN)));
    }

    @Test
    void shouldProcessFileWithoutHandleInDublinCoreFile() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(),
                   containsString(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV));
    }

    @Test
    void shouldProcessFileWithoutHandleInHandleFile() throws Exception {
        var arguments = new String[]{"-c", "nve", "-z", INPUT_WITHOUT_HANDLE_ZIP_FILE_NAME};
        var exitCode = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exitCode, is(equalTo(NORMAL_EXIT_CODE)));
    }
}

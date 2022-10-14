package no.sikt.nva;

import static no.sikt.nva.DublinCoreValidator.Problem.CRISTIN_ID_PRESENT;
import static no.sikt.nva.HandleScraper.COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV;
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
        var arguments = new String[]{"-c", "nve", "-z", "testinput.zip"};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), containsString(NO_LICENSE_LOGG_MESSAGE));
    }

    @Test
    void shouldProcessZipFileWithLicenseCorrectly() throws Exception {
        var arguments = new String[]{"-c", "nve", "-z", "inputWithLicense.zip"};
        var exit = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exit, is(equalTo(NORMAL_EXIT_CODE)));
    }

    @Test
    void shouldProcessAndLogFileWithCristinId() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", "inputWithCristinId.zip"};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(), containsString(String.valueOf(CRISTIN_ID_PRESENT)));
    }

    @Test
    void shouldProcessFileWithoutHandleInDublinCoreFile() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", "inputWithoutHandle.zip"};
        SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(appender.getMessages(),
                   containsString(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV));
    }

    @Test
    void shouldProcessFileWithoutHandleInHandleFile() throws Exception {
        var arguments = new String[]{"-c", "nve", "-z", "inputWithoutHandle.zip"};
        var exitCode = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(exitCode, is(equalTo(NORMAL_EXIT_CODE)));
    }
}

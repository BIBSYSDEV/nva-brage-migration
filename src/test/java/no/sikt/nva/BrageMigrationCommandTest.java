package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class BrageMigrationCommandTest {

    public static final String RESOURCE_WITH_CRISTIN_IDENTIFIER_LOGG_MESSAGE = "Following resource has Cristin "
                                                                               + "identifier:";
    public static final String NO_LICENSE_LOGG_MESSAGE = "No license in bundle found, default license used";
    public static final String NO_HANDLE_IN_DUBLIN_CORE_LOGG_MESSAGE = "No handle present in dublin_core.xml";
    public static final String NO_HANDLE_FILE_LOG_MESSAGE = "Could not read handle file";
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
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
        assertThat(appender.getMessages(), containsString(NO_LICENSE_LOGG_MESSAGE));
    }

    @Test
    void shouldProcessZipFileWithLicenseCorrectlyAndWithoutAnyLogMessages() throws Exception {
        var arguments = new String[]{"-c", "nve", "-z", "inputWithLicense.zip"};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
        assertThat(appender.getMessages(), is(emptyString()));
    }

    @Test
    void shouldProcessAndLogFileWithCristinId() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", "inputWithCristinId.zip"};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
        assertThat(appender.getMessages(), containsString(RESOURCE_WITH_CRISTIN_IDENTIFIER_LOGG_MESSAGE));
    }

    @Test
    void shouldProcessFileWithoutHandleInDublinCoreFile() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", "inputWithoutHandle.zip"};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
        assertThat(appender.getMessages(), containsString(NO_HANDLE_IN_DUBLIN_CORE_LOGG_MESSAGE));
    }

    @Test
    void shouldProcessFileWithoutHandleInHandleFile() throws Exception {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var arguments = new String[]{"-c", "nve", "-z", "inputWithoutHandle.zip"};
        int status = SystemLambda.catchSystemExit(() -> BrageMigrationCommand.main(arguments));
        assertThat(status, is(equalTo(NORMAL_EXIT_CODE)));
        assertThat(appender.getMessages(), containsString(NO_HANDLE_FILE_LOG_MESSAGE));
    }
}

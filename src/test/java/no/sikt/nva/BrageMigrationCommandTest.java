package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.jupiter.api.Test;

public class BrageMigrationCommandTest {

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
}

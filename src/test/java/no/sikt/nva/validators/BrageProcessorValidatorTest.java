package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.LicenseScraperTest.PATH_TO_FILES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import java.io.File;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.WarningDetails.Warning;
import org.junit.jupiter.api.Test;

public class BrageProcessorValidatorTest {

    @Test
    void shouldLogWhenInvalidCCLicense() {
        var actualWarnings = BrageProcessorValidator.getBrageProcessorWarnings(new File(PATH_TO_FILES));
        assertThat(actualWarnings, contains(new WarningDetails(Warning.INVALID_CC_LICENSE)));
    }
}

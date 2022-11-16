package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.LicenseScraperTest.PATH_TO_FILES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import java.io.File;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DublinCore;
import org.junit.jupiter.api.Test;

public class BrageProcessorValidatorTest {

    @Test
    void shouldLogWhenInvalidCCLicense() {
        var actualWarnings = BrageProcessorValidator.getBrageProcessorWarnings(new File(PATH_TO_FILES), new DublinCore());
        assertThat(actualWarnings, contains(new WarningDetails(Warning.INVALID_CC_LICENSE)));
    }
}

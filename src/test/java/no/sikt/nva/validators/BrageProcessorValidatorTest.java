package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.LicenseScraperTest.PATH_TO_FILES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import java.io.File;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.ErrorDetails.Error;
import no.sikt.nva.model.dublincore.DublinCore;
import org.junit.jupiter.api.Test;

public class BrageProcessorValidatorTest {

    @Test
    void shouldLogWhenInvalidCCLicense() {
        var actualErrors = BrageProcessorValidator.getBrageProcessorErrors(new File(PATH_TO_FILES),
                                                                           new DublinCore(List.of()));
        assertThat(actualErrors, contains(new ErrorDetails(Error.INVALID_CC_LICENSE)));
    }
}

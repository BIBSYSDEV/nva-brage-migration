package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.net.URI;
import no.sikt.nva.exceptions.LicenseExtractingException;
import org.junit.jupiter.api.Test;

public class LicenseScraperTest {

    public static final String INVALID_LICENSE_PATH = "src/test/resources/invalid_license_rdf";
    public static final String VALID_LICENSE_PATH = "src/test/resources/valid_license_rdf";
    public static final String EXPECTED_LICENSE = "http://creativecommons.org/licenses/by/4.0/deed.no";

    @Test
    void shouldReadLicense() throws LicenseExtractingException {
        var expectedLicense = URI.create(EXPECTED_LICENSE);
        LicenseScraper licenseScraper = new LicenseScraper();

        URI statements = licenseScraper.extractLicenseUri(new File(VALID_LICENSE_PATH));

        assertThat(statements, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldThrowExceptionIfReadingFileFails() {
        LicenseScraper licenseScraper = new LicenseScraper();
        assertThrows(LicenseExtractingException.class,
                     () -> licenseScraper.extractLicenseUri(new File(INVALID_LICENSE_PATH)));
    }
}





package no.sikt.nva.scrapers;

import static no.sikt.nva.scrapers.LicenseScraper.DEFAULT_LICENSE;
import static no.sikt.nva.ResourceNameConstants.INVALID_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.VALID_LICENSE_RDF_FILE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.File;
import no.sikt.nva.exceptions.LicenseExtractingException;
import no.sikt.nva.scrapers.LicenseScraper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LicenseScraperTest {

    public static final String EXPECTED_LICENSE_FROM_VALID_LICENSE_FILE = "http://creativecommons.org/licenses/by/4"
                                                                          + ".0/deed.no";
    public static final String PATH_TO_FILES = "src/test/resources";
    public static final String FILE_DOES_NOT_EXISTS = "does_not_exists";

    @Test
    void shouldReadLicenseWhenCustomLicenseFileIsValid() throws LicenseExtractingException {
        LicenseScraper licenseScraper = new LicenseScraper(VALID_LICENSE_RDF_FILE_NAME);
        var statements = licenseScraper.extractOrCreateLicense(new File(PATH_TO_FILES));

        assertThat(statements, is(equalTo(EXPECTED_LICENSE_FROM_VALID_LICENSE_FILE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {FILE_DOES_NOT_EXISTS, INVALID_LICENSE_RDF_FILE_NAME})
    void shouldReturnDefaultLicenseAndWriteToLogsWhenLicenseFileCannotBeRead(String filename) {
        LicenseScraper licenseScraper = new LicenseScraper(filename);
        var statements = licenseScraper.extractOrCreateLicense(new File(PATH_TO_FILES));
        assertThat(statements, is(equalTo(DEFAULT_LICENSE)));
    }
}





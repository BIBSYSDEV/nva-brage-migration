package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.EMPTY_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INVALID_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.FIRST_VALID_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.SECOND_VALID_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.scrapers.LicenseMapper.NvaLicenseIdentifier.DEFAULT_LICENSE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.File;
import java.util.List;
import no.sikt.nva.exceptions.LicenseExtractingException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.record.License;
import no.sikt.nva.model.record.NvaLicense;
import no.sikt.nva.scrapers.LicenseMapper.NvaLicenseIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LicenseScraperTest {

    public static final String PATH_TO_FILES = "src/test/resources";
    public static final String FILE_DOES_NOT_EXISTS = "does_not_exists";
    public static final String CC_LICENSE = "http://creativecommons.org/licenses/by/4.0/deed.no";

    @Test
    void shouldReadLicenseWhenCustomLicenseFileIsValid() throws LicenseExtractingException {
        LicenseScraper licenseScraper = new LicenseScraper(FIRST_VALID_LICENSE_RDF_FILE_NAME);
        var expectedLicense = new License(CC_LICENSE, new NvaLicense(NvaLicenseIdentifier.CC_BY));
        var actualLicense = licenseScraper.extractOrCreateLicense(new File(PATH_TO_FILES), new DublinCore());
        assertThat(actualLicense, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldReadLicenseWhenCustomLicenseFileIsInvalidButLicenseIsPresentInDublinCore()
        throws LicenseExtractingException {
        LicenseScraper licenseScraper = new LicenseScraper(SECOND_VALID_LICENSE_RDF_FILE_NAME);
        var expectedLicense = new License(CC_LICENSE, new NvaLicense(NvaLicenseIdentifier.CC_BY));
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var licenseDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, CC_LICENSE);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, licenseDcValue));
        var actualLicense = licenseScraper.extractOrCreateLicense(new File(PATH_TO_FILES), dublinCore);
        assertThat(actualLicense, is(equalTo(expectedLicense)));
    }

    @ParameterizedTest
    @ValueSource(strings = {FILE_DOES_NOT_EXISTS, INVALID_LICENSE_RDF_FILE_NAME})
    void shouldReturnDefaultLicenseAndWriteToLogsWhenLicenseFileCannotBeRead(String filename) {
        LicenseScraper licenseScraper = new LicenseScraper(filename);
        var expectedLicense = new License(null, new NvaLicense(DEFAULT_LICENSE));
        var actualLicense = licenseScraper.extractOrCreateLicense(new File(PATH_TO_FILES), new DublinCore());
        assertThat(actualLicense, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldDetectInvalidLicense() {
        LicenseScraper licenseScraper = new LicenseScraper(EMPTY_LICENSE_RDF_FILE_NAME);
        var license = licenseScraper.extractOrCreateLicense(new File(PATH_TO_FILES), new DublinCore());
        var expectedResult = false;
        assertThat(LicenseScraper.isValidCCLicense(license), is(equalTo(expectedResult)));
    }
}





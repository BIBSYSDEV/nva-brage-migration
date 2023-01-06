package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.EMPTY_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.FIRST_VALID_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.INVALID_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.SECOND_VALID_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.THIRD_VALID_LICENSE_RDF_FILE_NAME;
import static no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier.DEFAULT_LICENSE;
import static no.sikt.nva.scrapers.LicenseScraper.NORWEGIAN_BOKMAAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.File;
import java.util.List;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicense;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier;
import no.sikt.nva.exceptions.LicenseExtractingException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LicenseScraperTest {

    public static final String PATH_TO_FILES = "src/test/resources";
    public static final String FILE_DOES_NOT_EXISTS = "does_not_exists";
    public static final String CC_LICENSE = "http://creativecommons.org/licenses/by/4.0/deed.no";

    public static final String CC_BY_NC_ND = "http://creativecommons.org/licenses/by-nc-nd/4.0/deed.no";

    @Test
    void shouldReadLicenseWhenCustomLicenseFileIsValid() throws LicenseExtractingException {
        LicenseScraper licenseScraper = new LicenseScraper(FIRST_VALID_LICENSE_RDF_FILE_NAME);
        var expectedLicense =
            new License(CC_LICENSE,
                        new NvaLicense(NvaLicenseIdentifier.CC_BY,
                                       Map.of(NORWEGIAN_BOKMAAL,
                                              NvaLicenseIdentifier.CC_BY.getValue())));
        var actualLicense = licenseScraper.extractLicense(new File(PATH_TO_FILES), new DublinCore(List.of()));
        assertThat(actualLicense, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldPriotizeDublinCoreOverLicenseRdf() throws LicenseExtractingException {

        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var licenseDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, CC_BY_NC_ND);
        var dublinCoreWithCcByNcNdLicense = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue,
                                                                                                   licenseDcValue));
        var licenseFileWithCcByLicense = FIRST_VALID_LICENSE_RDF_FILE_NAME;
        LicenseScraper licenseScraper = new LicenseScraper(licenseFileWithCcByLicense);
        var expectedLicense =
            new License(CC_BY_NC_ND,
                        new NvaLicense(NvaLicenseIdentifier.CC_BY_NC_ND,
                                       Map.of(NORWEGIAN_BOKMAAL,
                                              NvaLicenseIdentifier.CC_BY_NC_ND.getValue())));
        var actualLicense = licenseScraper.extractLicense(new File(PATH_TO_FILES), dublinCoreWithCcByNcNdLicense);
        assertThat(actualLicense, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldReadLicenseWhenCustomLicenseFileIsInvalidButLicenseIsPresentInDublinCore()
        throws LicenseExtractingException {
        LicenseScraper licenseScraper = new LicenseScraper(SECOND_VALID_LICENSE_RDF_FILE_NAME);
        var expectedLicense =
            new License(CC_LICENSE,
                        new NvaLicense(NvaLicenseIdentifier.CC_BY,
                                       Map.of(NORWEGIAN_BOKMAAL,
                                              NvaLicenseIdentifier.CC_BY.getValue())));
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var licenseDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, CC_LICENSE);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, licenseDcValue));
        var actualLicense = licenseScraper.extractLicense(new File(PATH_TO_FILES), dublinCore);
        assertThat(actualLicense, is(equalTo(expectedLicense)));
    }

    @ParameterizedTest
    @ValueSource(strings = {FILE_DOES_NOT_EXISTS, INVALID_LICENSE_RDF_FILE_NAME})
    void shouldReturnDefaultLicenseAndWriteToLogsWhenLicenseFileCannotBeRead(String filename) {
        LicenseScraper licenseScraper = new LicenseScraper(filename);
        var expectedLicense =
            new License(null,
                        new NvaLicense(DEFAULT_LICENSE,
                                       Map.of(NORWEGIAN_BOKMAAL, DEFAULT_LICENSE.getValue())));
        var actualLicense =
            licenseScraper.extractLicense(new File(PATH_TO_FILES), new DublinCore(List.of()));
        assertThat(actualLicense, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldDetectInvalidLicense() {
        LicenseScraper licenseScraper = new LicenseScraper(EMPTY_LICENSE_RDF_FILE_NAME);
        var license = licenseScraper.extractLicense(new File(PATH_TO_FILES), new DublinCore(List.of()));
        var expectedResult = false;
        assertThat(LicenseScraper.isValidCCLicense(license), is(equalTo(expectedResult)));
    }

    @Test
    void shouldExtractLicenseFromOtherXmlFormat() {
        LicenseScraper licenseScraper = new LicenseScraper(SECOND_VALID_LICENSE_RDF_FILE_NAME);
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var actualLicense = licenseScraper.extractLicense(new File(PATH_TO_FILES), dublinCore);
        assertThat(actualLicense.getBrageLicense(), is(equalTo("http://creativecommons.org/licenses/by/4.0/deed.no")));
    }

    @Test
    void shouldExtractLicenseFromThirdXmlFormat() {
        LicenseScraper licenseScraper = new LicenseScraper(THIRD_VALID_LICENSE_RDF_FILE_NAME);
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var actualLicense = licenseScraper.extractLicense(new File(PATH_TO_FILES), dublinCore);
        assertThat(actualLicense.getBrageLicense(), is(equalTo("http://creativecommons.org/publicdomain/zero/1.0/")));
    }
}





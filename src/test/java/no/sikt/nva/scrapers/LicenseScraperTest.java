package no.sikt.nva.scrapers;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicense;
import no.sikt.nva.exceptions.LicenseExtractingException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class LicenseScraperTest {

    public static final URI EXPECTED_LICENSE = URI.create("https://creativecommons.org/licenses/by/4.0");

    public static Stream<String> licenseWithSpacingProvider() {
        return Stream.of("https://creativecommons.org/licenses/by/4.0/deed.no",
                         "https://creativecommons.org/licenses/by/4.0/", "https://creativecommons.org/licenses/by/4.0",
                         "https://creativecommons.org/licenses/by/4.0/no/",
                         "https://\u200Bcreativecommons.\u200Borg/\u200Blicenses/\u200Bby/\u200B4.\u200B0/no",
                         "https://creativecommons.org/licenses/by/\n4.0/ ");
    }

    public static Stream<String> licenseWithDifferentVersionProvider() {
        return Stream.of("https://creativecommons.org/license/1.0",
                         "https://creativecommons.org/licenses/by/2.5",
                         "https://creativecommons.org/licenses/by/2.0",
                         "https://creativecommons.org/licenses/by/3.0",
                         "https://creativecommons.org/licenses/by/4.0");
    }

    @ParameterizedTest
    @MethodSource("licenseWithSpacingProvider")
    void shouldPrioritizeDublinCoreOverLicenseRdf(String license) throws LicenseExtractingException {

        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var licenseDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, license);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, licenseDcValue));
        var licenseScraper = new LicenseScraper(dublinCore);
        var expectedLicense = new License(license, new NvaLicense(EXPECTED_LICENSE));
        var actual = licenseScraper.generateLicense();
        assertThat(actual, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldCreateDefaultLicenseWhenBrageLicenseIsNotPresent() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var licenseScraper = new LicenseScraper(dublinCore);
        var expectedLicense = new License(null, new NvaLicense(LicenseScraper.DEFAULT_LICENSE));
        assertThat(licenseScraper.generateLicense(), is(equalTo(expectedLicense)));
    }

    @Test
    void shouldDetectInvalidLicense() {
        var licenseScraper = new LicenseScraper(
            new DublinCore(List.of(new DcValue(Element.RIGHTS, Qualifier.URI, "SomeLicense"))));
        var license = licenseScraper.generateLicense();
        var expectedResult = false;
        assertThat(licenseScraper.isValidLicense(license), is(equalTo(expectedResult)));
    }

    @ParameterizedTest
    @MethodSource("licenseWithDifferentVersionProvider")
    void shouldSupportValidBrageLicenseVersions(String value) {
        var typeDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var licenseScraper = new LicenseScraper(dublinCore);
        var license = licenseScraper.generateLicense();

        assertThat(license.getNvaLicense().getLicense(), is(equalTo(URI.create(value))));
    }

    @Test
    void shouldReplaceHttpWithHttps() {
        var value = "http://creativecommons.org/licenses/by/2.5";
        var typeDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var licenseScraper = new LicenseScraper(dublinCore);
        var license = licenseScraper.generateLicense();

        var expectedLicense = URI.create("https://creativecommons.org/licenses/by/2.5");
        assertThat(license.getNvaLicense().getLicense(), is(equalTo(expectedLicense)));
    }

    @Test
    void shouldReplaceLicencesWithLicenses() {
        var value = "https://creativecommons.org/licences/by/2.5";
        var typeDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var licenseScraper = new LicenseScraper(dublinCore);
        var license = licenseScraper.generateLicense();

        var expectedLicense = URI.create("https://creativecommons.org/licenses/by/2.5");
        assertThat(license.getNvaLicense().getLicense(), is(equalTo(expectedLicense)));
    }

    @Test
    void shouldRemoveWWWFromLicenseUrl() {
        var value = "https://www.creativecommons.org/licenses/by/2.5";
        var typeDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var licenseScraper = new LicenseScraper(dublinCore);
        var license = licenseScraper.generateLicense();

        var expectedLicense = URI.create("https://creativecommons.org/licenses/by/2.5");
        assertThat(license.getNvaLicense().getLicense(), is(equalTo(expectedLicense)));
    }

    @Test
    void shouldRemoveLanguageCodeFromUrl() {
        var value = "https://creativecommons.org/licenses/by/2.5/uk";
        var typeDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var licenseScraper = new LicenseScraper(dublinCore);
        var license = licenseScraper.generateLicense();

        var expectedLicense = URI.create("https://creativecommons.org/licenses/by/2.5");
        assertThat(license.getNvaLicense().getLicense(), is(equalTo(expectedLicense)));
    }

    @Test
    void shouldReturnDefaultLicenseWhenUnknownLicense() {
        var value = randomString();
        var typeDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var licenseScraper = new LicenseScraper(dublinCore);
        var license = licenseScraper.generateLicense();

        var expectedLicense = URI.create("https://rightsstatements.org/vocab/InC/1.0/");
        assertThat(license.getNvaLicense().getLicense(), is(equalTo(expectedLicense)));
    }
}





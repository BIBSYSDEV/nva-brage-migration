package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier;
import org.junit.jupiter.api.Test;

public class LicenseMapperTest {

    @Test
    void shouldMapBrageLicenseToNvaLicenseCorrectly() {
        var expected = NvaLicenseIdentifier.CC_BY_NC_SA;
        var actual = LicenseMapper.mapLicenseToNva("https://creativecommons.org/licenses/by-nc-sa/4.0/deed.no");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void shouldMapBrageZeroLicenseToNvaLicenseCorrectly() {
        var expected = NvaLicenseIdentifier.CC_ZERO;
        var actual = LicenseMapper.mapLicenseToNva("https://creativecommons.org/publicdomain/zero/4.0/");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void shouldMapLicenseWithZeroWidthWhitespaces() {
        var expected = NvaLicenseIdentifier.CC_BY;
        var actual = LicenseMapper.mapLicenseToNva("http://\u200Bcreativecommons"
                                                   + ".\u200Borg/\u200Blicenses/\u200Bby/\u200B4.\u200B0");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void shouldReturnNullForLicenseWithUnsupportedVersion() {
        var actual = LicenseMapper.mapLicenseToNva("https://creativecommons.org/publicdomain/zero/1.0/");
        assertThat(actual, is(equalTo(null)));
    }
}

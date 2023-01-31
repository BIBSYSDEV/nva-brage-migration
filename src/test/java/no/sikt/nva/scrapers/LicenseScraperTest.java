package no.sikt.nva.scrapers;

import static no.sikt.nva.scrapers.LicenseScraper.NORWEGIAN_BOKMAAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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

public class LicenseScraperTest {

    public static final String CC_BY_NC_ND = "http://creativecommons.org/licenses/by-nc-nd/4.0/deed.no";

    @Test
    void shouldPrioritizeDublinCoreOverLicenseRdf() throws LicenseExtractingException {

        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var licenseDcValue = new DcValue(Element.RIGHTS, Qualifier.URI, CC_BY_NC_ND);
        var dublinCoreWithCcByNcNdLicense = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue,
                                                                                                   licenseDcValue));
        LicenseScraper licenseScraper = new LicenseScraper();
        var expectedLicense =
            new License(CC_BY_NC_ND,
                        new NvaLicense(NvaLicenseIdentifier.CC_BY_NC_ND,
                                       Map.of(NORWEGIAN_BOKMAAL,
                                              NvaLicenseIdentifier.CC_BY_NC_ND.getValue())));
        var actualLicense = licenseScraper.extractLicense(dublinCoreWithCcByNcNdLicense);
        assertThat(actualLicense, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldDetectInvalidLicense() {
        LicenseScraper licenseScraper = new LicenseScraper();
        var license = licenseScraper.extractLicense(new DublinCore(List.of()));
        var expectedResult = false;
        assertThat(LicenseScraper.isValidCCLicense(license), is(equalTo(expectedResult)));
    }
}





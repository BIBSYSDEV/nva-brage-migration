package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicense;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;

public class LicenseScraper {

    public static final String CC_BASE_URL = "creativecommons.org";
    public static final String NORWEGIAN_BOKMAAL = "nb";
    private final DublinCore dublinCore;

    public LicenseScraper(DublinCore dublinCore) {
        this.dublinCore = dublinCore;
    }

    public boolean isValidCCLicense(License license) {
        if (isNull(license)) {
            return false;
        }
        if (isNull(license.getBrageLicense())) {
            return false;
        }
        return license.getBrageLicense().contains(CC_BASE_URL);
    }

    public License generateLicense() {
        return constructLicense(dublinCore);
    }

    private License constructLicense(NvaLicenseIdentifier nvaLicenseIdentifier) {
        var nvaLicenseLabels = getLicenseLabels(nvaLicenseIdentifier);
        return new License(extractLicense(this.dublinCore), new NvaLicense(nvaLicenseIdentifier, nvaLicenseLabels));
    }

    private License constructLicense(DublinCore dublinCore) {
        return Optional.ofNullable(extractLicense(dublinCore))
                   .map(LicenseMapper::mapLicenseToNva)
                   .map(this::constructLicense)
                   .filter(this::isValidCCLicense)
                   .orElse(null);
    }

    public String extractLicense(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isLicense)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private Map<String, String> getLicenseLabels(NvaLicenseIdentifier nvaLicenseIdentifier) {
        return Map.of(NORWEGIAN_BOKMAAL, nvaLicenseIdentifier.getValue());
    }
}

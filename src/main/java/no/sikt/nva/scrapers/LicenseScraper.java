package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicense;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.StringUtils;

public class LicenseScraper {

    public static final String CC_BASE_URL = "creativecommons.org";
    public static final String NORWEGIAN_BOKMAAL = "nb";

    public LicenseScraper() {
    }

    public static boolean isValidCCLicense(License license) {
        if (isNull(license)) {
            return false;
        }
        if (isNull(license.getBrageLicense())) {
            return false;
        }
        return license.getBrageLicense().contains(CC_BASE_URL);
    }

    public License extractLicense(DublinCore dublinCore) {
        return extractLicenseFromDublinCore(dublinCore);
    }

    private static License constructLicense(NvaLicenseIdentifier nvaLicenseIdentifier, String brageLicense) {
        var nvaLicenseLabels = getLicenseLabels(nvaLicenseIdentifier);
        return new License(brageLicense, new NvaLicense(nvaLicenseIdentifier, nvaLicenseLabels));
    }

    private static License extractLicenseFromDublinCore(DublinCore dublinCore) {
        var licenseStringFromDublinCore = dublinCore.getDcValues()
                                              .stream()
                                              .filter(DcValue::isLicense)
                                              .findAny()
                                              .map(DcValue::scrapeValueAndSetToScraped)
                                              .orElse(StringUtils.EMPTY_STRING);
        var licenseFromDublinCore = Optional.ofNullable(LicenseMapper.mapLicenseToNva(licenseStringFromDublinCore))
                                        .map(nvaLicenseIdentifier -> constructLicense(nvaLicenseIdentifier,
                                                                                      licenseStringFromDublinCore))
                                        .orElse(null);
        if (isValidCCLicense(licenseFromDublinCore)) {
            return licenseFromDublinCore;
        } else {
            return null;
        }
    }

    private static Map<String, String> getLicenseLabels(NvaLicenseIdentifier nvaLicenseIdentifier) {
        return Map.of(NORWEGIAN_BOKMAAL, nvaLicenseIdentifier.getValue());
    }
}

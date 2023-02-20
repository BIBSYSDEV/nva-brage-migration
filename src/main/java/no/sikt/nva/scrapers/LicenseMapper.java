package no.sikt.nva.scrapers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier;

public class LicenseMapper {

    private static final String CREATIVE_COMMONS_HOST_NAME = "creativecommons.org";
    private static final String SUPPORTED_CC_LICENSE_VERSION = "4.0";
    private static final Map<BrageLicense, NvaLicenseIdentifier> LICENSE_MAP = Map.of(BrageLicense.CC_BY,
                                                                                      NvaLicenseIdentifier.CC_BY,
                                                                                      BrageLicense.CC_BY_NC,
                                                                                      NvaLicenseIdentifier.CC_BY_NC,
                                                                                      BrageLicense.CC_BY_ND,
                                                                                      NvaLicenseIdentifier.CC_BY_ND,
                                                                                      BrageLicense.CC_BY_SA,
                                                                                      NvaLicenseIdentifier.CC_BY_SA,
                                                                                      BrageLicense.CC_BY_NC_ND,
                                                                                      NvaLicenseIdentifier.CC_BY_NC_ND,
                                                                                      BrageLicense.CC_BY_NC_SA,
                                                                                      NvaLicenseIdentifier.CC_BY_NC_SA,
                                                                                      BrageLicense.CC_ZERO,
                                                                                      NvaLicenseIdentifier.CC_ZERO);

    public static NvaLicenseIdentifier mapLicenseToNva(String licenseUri) {
        return Optional.ofNullable(licenseUri)
                   .map(LicenseMapper::trim)
                   .map(LicenseMapper::getLicenseName)
                   .map(LICENSE_MAP::get)
                   .orElse(null);
    }

    public static boolean isSingleton(List<BrageLicense> versions) {
        return versions.size() == 1;
    }

    private static String trim(String licenseUri) {
        return licenseUri.replaceAll("[\\p{Cf}]", "");
    }

    private static BrageLicense getLicenseName(String licenseUri) {
        return hasCreativeCommonsHost(licenseUri)
                   ? parseLicenseUri(licenseUri)
                   : null;
    }

    private static boolean hasCreativeCommonsHost(String licenseUri) {
        return licenseUri.contains(CREATIVE_COMMONS_HOST_NAME);
    }

    private static BrageLicense parseLicenseUri(String licenseUri) {
        var pathContainingLicenseType = licenseUri.split(CREATIVE_COMMONS_HOST_NAME)[1];
        if (pathContainingLicenseType.contains(SUPPORTED_CC_LICENSE_VERSION)) {
            var brageLicenseSingleton = extractLicenseValue(pathContainingLicenseType);
            return isSingleton(brageLicenseSingleton)
                       ? brageLicenseSingleton.get(0)
                       : null;
        }
        return null;
    }

    private static List<BrageLicense> extractLicenseValue(String pathContainingLicenseType) {
        return Arrays.stream(pathContainingLicenseType.split("/"))
                   .map(BrageLicense::fromValue)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }
}

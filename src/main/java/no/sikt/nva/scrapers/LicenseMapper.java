package no.sikt.nva.scrapers;

import java.net.URI;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier;
import no.sikt.nva.exceptions.DublinCoreException;
import org.apache.commons.validator.routines.UrlValidator;

public class LicenseMapper {

    private static final String CREATIVE_COMMONS_HOST_NAME = "creativecommons.org";
    private static final String NOT_MATCHING_LICENSE_MESSAGE =
        "No configuration to map license, Rights Reserved license used for license: ";
    private static final Map<BrageLicense, NvaLicenseIdentifier> LICENSE_MAP =
        Map.of(BrageLicense.CC_BY, NvaLicenseIdentifier.CC_BY,
               BrageLicense.CC_BY_NC, NvaLicenseIdentifier.CC_BY_NC,
               BrageLicense.CC_BY_ND, NvaLicenseIdentifier.CC_BY_ND,
               BrageLicense.CC_BY_SA, NvaLicenseIdentifier.CC_BY_SA,
               BrageLicense.CC_BY_NC_ND, NvaLicenseIdentifier.CC_BY_NC_ND,
               BrageLicense.CC_BY_NC_SA, NvaLicenseIdentifier.CC_BY_NC_SA);

    public static NvaLicenseIdentifier mapLicenseToNva(String licenseUri) {
        var licenseName = getLicenseName(licenseUri);
        var brageLicense = convertToBrageLicense(licenseName);
        return LICENSE_MAP.get(brageLicense);
    }

    private static String getLicenseName(String licenseUri) {
        if (isValidUri(licenseUri) && hasCreativeCommonsHost(licenseUri)) {
            return parseLicenseUri(licenseUri);
        }
        throw new DublinCoreException(NOT_MATCHING_LICENSE_MESSAGE + licenseUri);
    }

    private static boolean isValidUri(String uri) {
        var uriValidator = UrlValidator.getInstance();
        return uriValidator.isValid(uri);
    }

    private static boolean hasCreativeCommonsHost(String licenseUri) {
        var uri = URI.create(licenseUri);
        return CREATIVE_COMMONS_HOST_NAME.equals(uri.getHost());
    }

    private static BrageLicense convertToBrageLicense(String brageLicense) {
        return BrageLicense.fromValue(brageLicense);
    }

    private static String parseLicenseUri(String licenseUri) {
        return URI.create(licenseUri).getPath().split("/")[2];
    }

}

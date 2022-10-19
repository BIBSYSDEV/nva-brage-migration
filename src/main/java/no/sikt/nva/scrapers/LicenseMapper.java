package no.sikt.nva.scrapers;

import java.net.URI;
import java.util.Map;
import no.sikt.nva.exceptions.DublinCoreException;
import org.apache.commons.validator.routines.UrlValidator;

public class LicenseMapper {

    private static final String CREATIVE_COMMONS_HOST_NAME = "creativecommons.org";
    private static final String NOT_MATCHING_LICENSE_MESSAGE =
        "No configuration to map license, Rights Reserved license used";
    private static final Map<BrageLicense, NvaLicense> LICENSE_MAP =
        Map.of(BrageLicense.CC0, NvaLicense.CC0,
               BrageLicense.CC_BY, NvaLicense.CC_BY,
               BrageLicense.CC_BY_NC, NvaLicense.CC_BY_NC,
               BrageLicense.CC_BY_ND, NvaLicense.CC_BY_ND,
               BrageLicense.CC_BY_SA, NvaLicense.CC_BY_SA,
               BrageLicense.CC_BY_NC_ND, NvaLicense.CC_BY_NC_ND,
               BrageLicense.CC_BY_NC_SA, NvaLicense.CC_BY_NC_SA);

    public static String mapLicenseToNva(String licenseUri) {
        var licenseName = getLicenseName(licenseUri);
        var brageLicense = convertToBrageLicense(licenseName);
        return LICENSE_MAP.get(brageLicense).getValue();
    }

    private static String getLicenseName(String licenseUri) {
        if (isValidUri(licenseUri) && hasCreativeCommonsHost(licenseUri)) {
            return parseLicensePath(licenseUri);
        }
        throw new DublinCoreException(NOT_MATCHING_LICENSE_MESSAGE);
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

    private static String parseLicensePath(String licenseUri) {
        return URI.create(licenseUri).getPath().split("/")[2];
    }

    public enum NvaLicense {
        CC0("CC0"),
        CC_BY("CC BY"),
        CC_BY_NC("CC BY-NC"),
        CC_BY_NC_ND("CC BY-NC-ND"),
        CC_BY_NC_SA("CC BY-NC-SA"),
        CC_BY_ND("CC BY-ND"),
        CC_BY_SA("CC BY-SA");
        private final String value;

        NvaLicense(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum BrageLicense {
        CC0("cc0"),
        CC_BY("by"),
        CC_BY_NC("by-nc"),
        CC_BY_NC_ND("by-nc-nd"),
        CC_BY_NC_SA("by-nc-sa"),
        CC_BY_ND("by-nd"),
        CC_BY_SA("by-sa");

        private final String value;

        BrageLicense(String value) {
            this.value = value;
        }

        public static BrageLicense fromValue(String value) {
            for (BrageLicense license : BrageLicense.values()) {
                if (license.getValue().equalsIgnoreCase(value)) {
                    return license;
                }
            }
            return null;
        }

        public String getValue() {
            return value;
        }
    }
}

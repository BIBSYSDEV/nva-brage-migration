package no.sikt.nva.scrapers;

import java.net.URI;
import java.util.Map;
import nva.commons.core.paths.UriWrapper;
import org.apache.commons.validator.routines.UrlValidator;

public class LicenseMapper {

    public static final String CREATIVECOMMONS_HOST_NAME = "creativecommons.org";

    //    private static final Map<String, NvaLicense> LICENSE_MAP;

    public static String mapLicenseToNva(String license) {
        return null;
    }

//    public static String getLicenseName(String licenseUri) {
//       if(isValidUri(licenseUri) && hasCreativeCommonsHost(licenseUri)) {
//
//       }
//    }

    public static boolean isValidUri(String uri) {
        var uriValidator = UrlValidator.getInstance();
        return uriValidator.isValid(uri);
    }

    public static boolean hasCreativeCommonsHost(String licenseUri) {
        var uri = URI.create(licenseUri);
        return uri.getHost().equals(CREATIVECOMMONS_HOST_NAME);
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
}

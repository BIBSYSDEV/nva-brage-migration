package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicense;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UriWrapper;

public class LicenseScraper {

    public static final String CREATIVE_COMMONS_BASE_URL = "creativecommons.org";
    public static final URI DEFAULT_LICENSE = URI.create("https://rightsstatements.org/page/InC/1.0/");
    public static final String DASH = "/";
    private static final String CREATIVE_COMMONS_HOST_NAME = "creativecommons.org";
    private static final List<String> LICENSE_VERSIONS = List.of("1.0",
                                                                 "2.0",
                                                                 "3.0",
                                                                 "4.0");
    public static final String LINEBREAKS_WHITESPACES_REGEX = "(\\n)|(\\s)|(\u200b)";
    private final DublinCore dublinCore;

    public LicenseScraper(DublinCore dublinCore) {
        this.dublinCore = dublinCore;
    }

    public boolean isValidLicense(License license) {
        if (isNull(license) || isNull(license.getBrageLicense())) {
            return false;
        }
        return trim(license.getBrageLicense()).contains(CREATIVE_COMMONS_BASE_URL);
    }

    public License generateLicense() {
        return constructLicense(dublinCore);
    }

    public String extractLicense(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isLicense)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static URI toUriWithVersionOnly(URI uri, String lastPathElement) {
        var license = URI.create(String.valueOf(uri).replace(DASH + lastPathElement, StringUtils.EMPTY_STRING));
        return String.valueOf(license).endsWith(DASH)
                   ? toUriWithoutDashAtTheEnd(license)
                   : license;
    }

    private static URI toUriWithoutDashAtTheEnd(URI uri) {
        return URI.create(org.apache.commons.lang3.StringUtils.removeEnd(String.valueOf(uri), DASH));
    }

    private License defaultLicense() {
        return new License(null, new NvaLicense(DEFAULT_LICENSE));
    }

    private URI toStandardFormatLicenseUri(URI uri) {
        var lastPathElement = UriWrapper.fromUri(uri).getLastPathElement();
        if (isNotAVersion(lastPathElement)) {
            return toUriWithVersionOnly(uri, lastPathElement);
        } else {
            return String.valueOf(uri).endsWith(DASH)
                       ? toUriWithoutDashAtTheEnd(uri)
                       : uri;
        }
    }

    private boolean isNotAVersion(String lastPathElement) {
        return !LICENSE_VERSIONS.contains(lastPathElement);
    }

    private String trim(String licenseUri) {
        return Optional.of(licenseUri.replaceAll("[\\p{Cf}]", ""))
                   .map(s -> s.replaceAll(LINEBREAKS_WHITESPACES_REGEX, StringUtils.EMPTY_STRING)).get();
    }

    private URI getLicenseName(String licenseUri) {
        return hasCreativeCommonsHost(licenseUri)
                   ? URI.create(licenseUri)
                   : null;
    }

    private boolean hasCreativeCommonsHost(String licenseUri) {
        return licenseUri.contains(CREATIVE_COMMONS_HOST_NAME);
    }

    private License constructLicense(URI license) {
        return new License(extractLicense(dublinCore), new NvaLicense(license));
    }

    private License constructLicense(DublinCore dublinCore) {
        return Optional.ofNullable(extractLicense(dublinCore))
                   .map(this::trim)
                   .map(this::getLicenseName)
                   .map(this::toStandardFormatLicenseUri)
                   .map(this::constructLicense)
                   .filter(this::isValidLicense)
                   .orElse(defaultLicense());
    }
}

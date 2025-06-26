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
    public static final URI DEFAULT_LICENSE = URI.create("https://nva.sikt.no/license/copyright-act/1.0");
    public static final String DASH = "/";
    private static final String CREATIVE_COMMONS_HOST_NAME = "creativecommons.org";
    private static final List<String> LICENSE_VERSIONS = List.of("1.0",
                                                                 "2.0",
                                                                 "2.5",
                                                                 "3.0",
                                                                 "4.0");
    public static final String LINEBREAKS_WHITESPACES_REGEX = "(\\n)|(\\s)|(\u200b)";
    public static final String RIGHTS_STATEMENTS_HOST_NAME = "rightsstatements";
    public static final String RIGHTS_STATEMENTS_VERSION = "1.0";
    public static final String INVALID_LICENSES_PATH = "licences";
    public static final String VALID_LICENSES_PATH = "licenses";
    public static final String WWW_SUBDOMAIN = "www.";
    private final DublinCore dublinCore;

    public LicenseScraper(DublinCore dublinCore) {
        this.dublinCore = dublinCore;
    }

    public boolean isValidLicense(License license) {
        if (isNull(license) || isNull(license.getBrageLicense())) {
            return false;
        }
        var trimmed = trim(license.getBrageLicense());
        return trimmed.contains(CREATIVE_COMMONS_BASE_URL) || isRightsStatementsLicense(trimmed);
    }

    private boolean isRightsStatementsLicense(String trimmed) {
        return trimmed.contains(RIGHTS_STATEMENTS_HOST_NAME) || trimmed.contains(RIGHTS_STATEMENTS_VERSION);
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
        return URI.create(String.valueOf(uri).replace(DASH + lastPathElement, StringUtils.EMPTY_STRING));
    }

    private License defaultLicense() {
        return new License(null, new NvaLicense(DEFAULT_LICENSE));
    }

    private static URI toStandardFormatLicenseUri(URI uri) {
        var lastPathElement = UriWrapper.fromUri(uri).getLastPathElement();
        if (isNotAVersion(lastPathElement)) {
            return toUriWithVersionOnly(uri, lastPathElement);
        } else {
            return uri;
        }
    }

    private static boolean isNotAVersion(String lastPathElement) {
        return !LICENSE_VERSIONS.contains(lastPathElement);
    }

    private String trim(String licenseUri) {
        return Optional.of(licenseUri.replaceAll("[\\p{Cf}]", ""))
                   .map(s -> s.replaceAll(LINEBREAKS_WHITESPACES_REGEX, StringUtils.EMPTY_STRING)).get();
    }

    private boolean hasCreativeCommonsHost(String licenseUri) {
        return licenseUri.contains(CREATIVE_COMMONS_HOST_NAME);
    }

    private License constructLicense(URI license) {
        return new License(extractLicense(dublinCore), new NvaLicense(license));
    }

    private License constructLicense(DublinCore dublinCore) {
        var license = Optional.ofNullable(extractLicense(dublinCore))
                                 .map(this::trim);
        if (license.stream().anyMatch(this::hasCreativeCommonsHost)) {
            return license.filter(this::hasCreativeCommonsHost)
                       .map(URI::create)
                       .map(LicenseScraper::formatLicense)
                       .map(this::constructLicense)
                       .filter(this::isValidLicense)
                       .orElse(defaultLicense());
        }
        if (license.stream().anyMatch(this::isRightsStatementsLicense)) {
            return new License(license.orElse(null), new NvaLicense(DEFAULT_LICENSE));
        } else {
            return defaultLicense();
        }
    }

    private static URI formatLicense(URI uri) {
        return Optional.ofNullable(uri)
                   .map(LicenseScraper::updateUrlProtocol)
                   .map(LicenseScraper::replaceLicensePathIfNeeded)
                   .map(LicenseScraper::removeWWWIfNeeded)
                   .map(LicenseScraper::toStandardFormatLicenseUri)
                   .orElse(null);
    }

    private static URI removeWWWIfNeeded(URI uri) {
        var value = uri.toString();
        if (value.contains(WWW_SUBDOMAIN)) {
            return URI.create(value.replace(WWW_SUBDOMAIN, StringUtils.EMPTY_STRING));
        }
        return uri;
    }

    private static URI replaceLicensePathIfNeeded(URI uri) {
        var value = uri.toString();
        if (value.contains(INVALID_LICENSES_PATH)) {
            return URI.create(value.replace(INVALID_LICENSES_PATH, VALID_LICENSES_PATH));
        } else {
            return uri;
        }
    }

    private static URI updateUrlProtocol(URI uri) {
        return UriWrapper.fromHost(uri.getHost()).addChild(uri.getPath()).getUri();
    }
}

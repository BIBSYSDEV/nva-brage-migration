package no.sikt.nva.brage.migration.common.model.record.license;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URI;
import java.util.Objects;

public class License {
    // Will it always be version 4 of creative commons?
    private static final String CREATIVE_COMMONS_LICENCE_URI =  "https://creativecommons.org/licenses/%s/4.0/";
    private static final String RIGHTS_STATEMENTS_INC_URI = "https://rightsstatements.org/page/InC/1.0/";

    @JsonInclude
    private String brageLicense;
    private NvaLicense nvaLicense;

    public License(String brageLisense, NvaLicense nvaLicense) {
        this.brageLicense = brageLisense;
        this.nvaLicense = nvaLicense;
    }

    public License() {

    }

    public String getBrageLicense() {
        return brageLicense;
    }

    public void setBrageLicense(String brageLicense) {
        this.brageLicense = brageLicense;
    }

    public NvaLicense getNvaLicense() {
        return nvaLicense;
    }

    public void setNvaLicense(NvaLicense nvaLicense) {
        this.nvaLicense = nvaLicense;
    }

    @Override
    public int hashCode() {
        return Objects.hash(brageLicense, nvaLicense);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        License license = (License) o;
        return Objects.equals(brageLicense, license.brageLicense)
               && Objects.equals(nvaLicense, license.nvaLicense);
    }

    @Override
    public String toString() {
        return brageLicense;
    }

    public static License fromBrageLicense(BrageLicense brageLicense) {
        URI licenseUri;
        if (BrageLicense.UTGIVERS_BETINGELSER.equals(brageLicense)) {
            licenseUri = URI.create(RIGHTS_STATEMENTS_INC_URI);
        } else if (BrageLicense.CC_ZERO.equals(brageLicense)) {
            licenseUri = null;
        } else {
            licenseUri = URI.create(String.format(CREATIVE_COMMONS_LICENCE_URI, brageLicense.getUriValue()));
        }

        return new License(brageLicense.getValue(), new NvaLicense(licenseUri));
    }
}

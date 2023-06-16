package no.sikt.nva.brage.migration.common.model.record.license;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;

public class NvaLicense {

    private static final String TYPE = "License";
    private URI license;

    public NvaLicense(@JsonProperty("license") URI license) {
        this.license = license;
    }

    public URI getLicense() {
        return license;
    }

    public void setLicense(URI license) {
        this.license = license;
    }

    @JsonProperty("type")
    public String getType() {
        return TYPE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(TYPE, license);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NvaLicense nvaLicense = (NvaLicense) o;
        return Objects.equals(license, nvaLicense.license);
    }
}

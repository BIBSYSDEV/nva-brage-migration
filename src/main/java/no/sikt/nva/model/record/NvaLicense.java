package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.sikt.nva.scrapers.LicenseMapper.NvaLicenseIdentifier;

public class NvaLicense {

    private static final String TYPE = "License";
    private NvaLicenseIdentifier identifier;

    public NvaLicense(NvaLicenseIdentifier identifier) {
        this.identifier = identifier;
    }

    public NvaLicenseIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(NvaLicenseIdentifier identifier) {
        this.identifier = identifier;
    }

    @JsonProperty("type")
    public String getType() {
        return TYPE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(TYPE, identifier);
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
        return Objects.equals(identifier, nvaLicense.identifier);
    }
}
package no.sikt.nva.brage.migration.common.model.record.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent.BundleType;
import nva.commons.core.JacocoGenerated;

public class ContentFile {

    private String filename;
    private BundleType bundleType;
    private String description;
    private String unknownType;
    private UUID identifier;
    private License license;
    private String embargoDate;

    public ContentFile() {

    }

    @JsonProperty("embargoDate")
    public String getEmbargoDate() {
        return embargoDate;
    }

    public void setEmbargoDate(String embargoDate) {
        this.embargoDate = embargoDate;
    }

    @JsonProperty("identifier")
    public UUID getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    @JsonProperty("unknownType")
    public String getUnknownType() {
        return unknownType;
    }

    public void setUnknownType(String unknownType) {
        this.unknownType = unknownType;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(filename, bundleType, description, unknownType, identifier, license, embargoDate);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContentFile that = (ContentFile) o;
        return Objects.equals(filename, that.filename)
               && bundleType == that.bundleType
               && Objects.equals(description, that.description)
               && Objects.equals(unknownType, that.unknownType)
               && Objects.equals(identifier, that.identifier)
               && Objects.equals(license, that.license)
               && Objects.equals(embargoDate, that.embargoDate);
    }

    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @JsonProperty("bundleType")
    public BundleType getBundleType() {
        return bundleType;
    }

    @JsonProperty("description")
    public void setBundleType(BundleType bundleType) {
        this.bundleType = bundleType;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

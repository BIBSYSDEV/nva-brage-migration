package no.sikt.nva.model.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.sikt.nva.model.content.ResourceContent.BundleType;

public class ContentFile {

    private String filename;
    private BundleType bundleType;
    private String description;
    private String unknownType;

    public ContentFile() {

    }

    public ContentFile(String filename, BundleType bundleType, String description) {
        this.filename = filename;
        this.bundleType = bundleType;
        this.description = description;
    }

    public ContentFile(String filename, String unknownType, String description) {
        this.filename = filename;
        this.unknownType = unknownType;
        this.description = description;
    }

    @JsonProperty("unknownType")
    public String getUnknownType() {
        return unknownType;
    }

    public void setUnknownType(String unknownType) {
        this.unknownType = unknownType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, bundleType, description);
    }

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
               && Objects.equals(description, that.description);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

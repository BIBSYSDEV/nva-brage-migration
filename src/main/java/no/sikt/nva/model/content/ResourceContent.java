package no.sikt.nva.model.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class ResourceContent {

    private List<ContentFile> contentFiles;

    public ResourceContent(List<ContentFile> contentFiles) {
        this.contentFiles = contentFiles;
    }

    @JsonProperty("contentFiles")
    public List<ContentFile> getContentFiles() {
        return contentFiles;
    }

    public void setContentFiles(List<ContentFile> contentFiles) {
        this.contentFiles = contentFiles;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(contentFiles);
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
        ResourceContent that = (ResourceContent) o;
        return Objects.equals(contentFiles, that.contentFiles);
    }

    public enum BundleType {
        ORIGINAL,
        TEXT,
        THUMBNAIL,
        LICENSE,
        CCLICENSE
    }
}
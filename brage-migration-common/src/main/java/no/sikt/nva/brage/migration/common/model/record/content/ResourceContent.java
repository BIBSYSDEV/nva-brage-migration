package no.sikt.nva.brage.migration.common.model.record.content;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class ResourceContent {

    private List<ContentFile> contentFiles;

    @JsonCreator
    public ResourceContent(@JsonProperty("contentFiles") List<ContentFile> contentFiles) {
        this.contentFiles = contentFiles;
    }

    public ResourceContent(ContentFile contentFile) {
        this(new ArrayList<>(List.of(contentFile)));
    }

    @JsonProperty("contentFiles")
    public List<ContentFile> getContentFiles() {
        return nonNull(contentFiles) ? contentFiles : List.of();
    }

    public static ResourceContent emptyResourceContent() {
        return new ResourceContent(List.of());
    }

    public void setContentFiles(List<ContentFile> contentFiles) {
        this.contentFiles = contentFiles;
    }

    public ContentFile getContentFileByFilename(String filename) {
        return contentFiles.stream()
                   .filter(contentFile -> contentFile.getFilename().equals(filename))
                   .findAny()
                   .orElse(null);
    }

    public void addContentFile(ContentFile contentFile) {
        if (isNull(this.contentFiles)) {
            this.contentFiles = new ArrayList<>();
        }
        this.contentFiles.add(contentFile);
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
        ORIGINAL("ORIGINAL"),
        TEXT("TEXT"),
        THUMBNAIL("THUMBNAIL"),
        LICENSE("LICENSE"),
        CCLICENSE("CC-LICENSE"),
        ORE("ORE"),
        SWORD("SWORD"),
        IGNORED("IGNORED"),
        METADATA("METADATA");

        private final String value;

        BundleType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

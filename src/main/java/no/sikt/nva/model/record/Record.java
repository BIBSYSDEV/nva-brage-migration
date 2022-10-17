package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import no.sikt.nva.model.publisher.Publication;
import nva.commons.core.JacocoGenerated;

public class Record {

    private String customerUri;
    private URI id;
    private Path origin;
    private String type;
    private String title;
    private String language;
    private String license;
    private String embargo;
    private Boolean publisherAuthority;
    private String rightsholder;
    private List<String> tags;
    private List<String> authors;
    private Publication publication;

    @JsonInclude
    @JsonProperty("publisherAuthority")
    public Boolean getPublisherAuthority() {
        return publisherAuthority;
    }

    public void setPublisherAuthority(Boolean publisherAuthority) {
        this.publisherAuthority = publisherAuthority;
    }

    @JsonProperty("publication")
    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(customerUri, id, type, title, language, license, embargo, tags, authors, origin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Record record = (Record) o;
        return Objects.equals(customerUri, record.customerUri)
               && Objects.equals(id, record.id)
               && Objects.equals(type, record.type)
               && Objects.equals(title, record.title)
               && Objects.equals(language, record.language)
               && Objects.equals(license, record.license)
               && Objects.equals(embargo, record.embargo)
               && Objects.equals(tags, record.tags)
               && Objects.equals(authors, record.authors)
               && Objects.equals(origin, record.origin);
    }

    @JacocoGenerated
    @JsonProperty("customerUri")
    public String getCustomerUri() {
        return this.customerUri;
    }

    @JacocoGenerated
    public void setCustomerUri(String customerUri) {
        this.customerUri = customerUri;
    }

    @JsonProperty("id")
    public URI getId() {
        return this.id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    @JacocoGenerated
    @JsonProperty("type")
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JacocoGenerated
    @JsonProperty("title")
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JacocoGenerated
    @JsonProperty("authors")
    public List<String> getAuthors() {
        return this.authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    @JacocoGenerated
    @JsonProperty("language")
    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @JacocoGenerated
    @JsonProperty("license")
    public String getLicense() {
        return this.license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @JacocoGenerated
    @JsonProperty("embargo")
    public String getEmbargo() {
        return this.embargo;
    }

    @JacocoGenerated
    public void setEmbargo(String embargo) {
        this.embargo = embargo;
    }

    @JacocoGenerated
    @JsonProperty("tags")
    public List<String> getTags() {
        return this.tags;
    }

    @JacocoGenerated
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("bare_origin")
    public Path getOrigin() {
        return origin;
    }

    public void setOrigin(Path origin) {
        this.origin = origin;
    }

    @JsonProperty("rightsholder")
    public String getRightsholder() {
        return rightsholder;
    }

    public void setRightsHolder(String rightsholder) {
        this.rightsholder = rightsholder;
    }
}

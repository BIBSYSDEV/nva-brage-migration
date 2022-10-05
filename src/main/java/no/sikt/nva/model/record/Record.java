package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class Record {

    private String customerUri;
    private URI id;
    private String type;
    private String title;
    private String language;
    private String license;
    private String embargo;
    private List<String> tags;
    private List<String> authors;

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(customerUri, id, type, title, language, license, embargo, tags, authors);
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
        Record record = (Record) o;
        return Objects.equals(customerUri, record.customerUri)
               && Objects.equals(id, record.id)
               && Objects.equals(type, record.type)
               && Objects.equals(title, record.title)
               && Objects.equals(language, record.language)
               && Objects.equals(license, record.license)
               && Objects.equals(embargo, record.embargo)
               && Objects.equals(tags, record.tags)
               && Objects.equals(authors, record.authors);
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

    @JacocoGenerated
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

    @JacocoGenerated
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
}

package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import nva.commons.core.JacocoGenerated;

@JsonPropertyOrder({"customerId", "bareOrigin", "id", "cristinId", "doi", "publishedDate", "publisherAuthority",
    "rightsholder",
    "type", "embargo", "publisherAuthority", "spatialCoverage", "date", "language", "publication", "entityDescription",
    "recordContent", "errors", "warnings"})
@SuppressWarnings("PMD.TooManyFields")
public class Record {

    private EntityDescription entityDescription;
    private URI customerId;
    private URI id;
    private URI doi;
    private Path origin;
    private Type type;
    private Language language;
    private String embargo;
    private Boolean publisherAuthority;
    private String rightsholder;
    private String spatialCoverage;
    private Publication publication;
    private ResourceContent contentBundle;
    private PublishedDate publishedDate;
    private String cristinId;
    private String brageLocation;
    private List<ErrorDetails> errors;
    private List<WarningDetails> warnings;

    public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

    @JsonProperty("brageLocation")
    public String getBrageLocation() {
        return brageLocation;
    }

    public void setBrageLocation(String brageLocation) {
        this.brageLocation = brageLocation;
    }

    @JsonProperty("warnings")
    public List<WarningDetails> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<WarningDetails> warnings) {
        this.warnings = warnings;
    }

    @JsonProperty("errors")
    public List<ErrorDetails> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDetails> errors) {
        this.errors = errors;
    }

    @JsonProperty("cristinId")
    public String getCristinId() {
        return cristinId;
    }

    public void setCristinId(String cristinId) {
        this.cristinId = cristinId;
    }

    @JsonProperty("publishedDate")
    public PublishedDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(PublishedDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    @JsonProperty("recordContent")
    public ResourceContent getContentBundle() {
        return contentBundle;
    }

    public void setContentBundle(ResourceContent contentBundle) {
        this.contentBundle = contentBundle;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(entityDescription, customerId, id, doi, origin, type, language, embargo, publisherAuthority,
                            rightsholder, spatialCoverage, publication, contentBundle, publishedDate, cristinId, errors,
                            warnings, brageLocation);
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
        return Objects.equals(entityDescription, record.entityDescription)
               && Objects.equals(customerId, record.customerId)
               && Objects.equals(id, record.id)
               && Objects.equals(doi, record.doi)
               && Objects.equals(origin, record.origin)
               && Objects.equals(type, record.type)
               && Objects.equals(language, record.language)
               && Objects.equals(embargo, record.embargo)
               && Objects.equals(publisherAuthority, record.publisherAuthority)
               && Objects.equals(rightsholder, record.rightsholder)
               && Objects.equals(spatialCoverage, record.spatialCoverage)
               && Objects.equals(publication, record.publication)
               && Objects.equals(contentBundle, record.contentBundle)
               && Objects.equals(publishedDate, record.publishedDate)
               && Objects.equals(cristinId, record.cristinId)
               && Objects.equals(brageLocation, record.brageLocation)
               && listEqualsIgnoreOrder(errors, record.errors)
               && listEqualsIgnoreOrder(warnings, record.warnings);
    }

    @JacocoGenerated
    @JsonProperty("spatialCoverage")
    public String getSpatialCoverage() {
        return spatialCoverage;
    }

    public void setSpatialCoverage(String spatialCoverage) {
        this.spatialCoverage = spatialCoverage;
    }

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
    @JsonProperty("customerId")
    public URI getCustomerId() {
        return this.customerId;
    }

    @JacocoGenerated
    public void setCustomerId(URI customerId) {
        this.customerId = customerId;
    }

    @JsonProperty("id")
    public URI getId() {
        return this.id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    @JsonProperty("type")
    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @JacocoGenerated
    @JsonProperty("language")
    public Language getLanguage() {
        return this.language;
    }

    public void setLanguage(Language language) {
        this.language = language;
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

    @JsonProperty("bareOrigin")
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

    @JsonProperty("doi")
    public URI getDoi() {
        return doi;
    }

    public void setDoi(URI doi) {
        this.doi = doi;
    }

    @JsonProperty("entityDescription")
    public EntityDescription getEntityDescription() {
        return entityDescription;
    }

    public void setEntityDescription(EntityDescription entityDescription) {
        this.entityDescription = entityDescription;
    }
}
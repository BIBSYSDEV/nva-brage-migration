package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;

@JsonPropertyOrder({"customer", "resourceOwner", "brageLocation", "id", "cristinId", "doi", "publishedDate",
    "publisherAuthority",
    "rightsholder",
    "type", "partOf", "hasPart", "publisherAuthority", "spatialCoverage", "date", "language", "publication",
    "entityDescription",
    "recordContent", "errors", "warnings"})
@SuppressWarnings("PMD.TooManyFields")
public class Record {

    private ResourceOwner resourceOwner;
    private EntityDescription entityDescription;
    private Customer customer;
    private URI id;
    private URI doi;
    private Type type;
    private PublisherAuthority publisherAuthority;
    private String rightsholder;
    private List<String> spatialCoverage;
    private String partOf;
    private String part;
    private Publication publication;
    private ResourceContent contentBundle;
    private PublishedDate publishedDate;
    private String cristinId;
    private String brageLocation;
    private List<ErrorDetails> errors;
    private List<WarningDetails> warnings;

    public Record() {
    }

    public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

    @JsonProperty("resourceOwner")
    public ResourceOwner getResourceOwner() {
        return resourceOwner;
    }

    public void setResourceOwner(ResourceOwner resourceOwner) {
        this.resourceOwner = resourceOwner;
    }

    @JsonProperty("hasPart")
    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    @JsonProperty("partOf")
    public String getPartOf() {
        return partOf;
    }

    public void setPartOf(String partOf) {
        this.partOf = partOf;
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
    @JsonProperty("spatialCoverage")
    public List<String> getSpatialCoverage() {
        return spatialCoverage;
    }

    public void setSpatialCoverage(List<String> spatialCoverage) {
        this.spatialCoverage = spatialCoverage;
    }

    @JsonInclude
    @JsonProperty("publisherAuthority")
    public PublisherAuthority getPublisherAuthority() {
        return publisherAuthority;
    }

    public void setPublisherAuthority(PublisherAuthority publisherAuthority) {
        this.publisherAuthority = publisherAuthority;
    }

    @JsonProperty("publication")
    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    @JsonProperty("customer")
    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public String toJsonString() throws JsonProcessingException {
        return JsonUtils.dtoObjectMapper.writeValueAsString(this);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(resourceOwner, getEntityDescription(), getCustomer(), getId(), getDoi(), getType(),
                            getPublisherAuthority(), getRightsholder(), getSpatialCoverage(),
                            getPartOf(),
                            getPart(), getPublication(), getContentBundle(), getPublishedDate(), getCristinId(),
                            getBrageLocation(), getErrors(), getWarnings());
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
        return Objects.equals(resourceOwner, record.resourceOwner)
               && Objects.equals(getEntityDescription(), record.getEntityDescription())
               && Objects.equals(getCustomer(), record.getCustomer())
               && Objects.equals(getId(), record.getId())
               && Objects.equals(getDoi(), record.getDoi())
               && Objects.equals(getType(), record.getType())
               && Objects.equals(getPublisherAuthority(), record.getPublisherAuthority())
               && Objects.equals(getRightsholder(), record.getRightsholder())
               && Objects.equals(getSpatialCoverage(), record.getSpatialCoverage())
               && Objects.equals(getPartOf(), record.getPartOf())
               && Objects.equals(getPart(), record.getPart())
               && Objects.equals(getPublication(), record.getPublication())
               && Objects.equals(getContentBundle(), record.getContentBundle())
               && Objects.equals(getPublishedDate(), record.getPublishedDate())
               && Objects.equals(getCristinId(), record.getCristinId())
               && Objects.equals(getBrageLocation(), record.getBrageLocation())
               && Objects.equals(getErrors(), record.getErrors())
               && Objects.equals(getWarnings(), record.getWarnings());
    }
}

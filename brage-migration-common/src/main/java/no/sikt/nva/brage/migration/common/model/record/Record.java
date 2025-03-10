package no.sikt.nva.brage.migration.common.model.record;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

@JsonPropertyOrder({"customer", "resourceOwner", "brageLocation", "id", "cristinId", "doi", "link", "publishedDate",
    "publisherAuthority", "rightsholder", "type", "partOf", "hasPart", "publisherAuthority", "spatialCoverage", "date",
    "language", "publication", "entityDescription", "recordContent", "errors", "warnings", "projects", "accessCode",
    "subjectCode", "subjects"})
@SuppressWarnings("PMD.TooManyFields")
public class Record {

    private static final List<String> DEGREES = List.of(NvaType.BACHELOR_THESIS.getValue(), NvaType.MASTER_THESIS.getValue(),
                                                   NvaType.DOCTORAL_THESIS.getValue());

    private EntityDescription entityDescription;
    private Customer customer;
    private URI id;
    private URI doi;
    private Type type;
    private PublisherAuthority publisherAuthority;
    private String rightsholder;
    private List<String> spatialCoverage;
    private String partOf;
    private List<String> part;
    private Publication publication;
    private ResourceContent contentBundle;
    private PublishedDate publishedDate;
    private String cristinId;
    private String insperaIdentifier;
    private String wiseflowIdentifier;
    private String brageLocation;
    private Set<ErrorDetails> errors;
    private Set<WarningDetails> warnings;
    private URI link;
    private List<URI> subjects;
    private String subjectCode;
    private String accessCode;
    private List<Project> projects;
    private Set<String> prioritizedFields;

    public Record() {
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Record)) {
            return false;
        }
        Record record = (Record) o;
        return Objects.equals(getEntityDescription(), record.getEntityDescription()) &&
               Objects.equals(getCustomer(), record.getCustomer()) &&
               Objects.equals(getId(), record.getId()) && Objects.equals(getDoi(), record.getDoi()) &&
               Objects.equals(getType(), record.getType()) &&
               Objects.equals(getPublisherAuthority(), record.getPublisherAuthority()) &&
               Objects.equals(getRightsholder(), record.getRightsholder()) &&
               Objects.equals(getSpatialCoverage(), record.getSpatialCoverage()) &&
               Objects.equals(getPartOf(), record.getPartOf()) &&
               Objects.equals(getPart(), record.getPart()) &&
               Objects.equals(getPublication(), record.getPublication()) &&
               Objects.equals(getContentBundle(), record.getContentBundle()) &&
               Objects.equals(getPublishedDate(), record.getPublishedDate()) &&
               Objects.equals(getCristinId(), record.getCristinId()) &&
               Objects.equals(getInsperaIdentifier(), record.getInsperaIdentifier()) &&
               Objects.equals(getWiseflowIdentifier(), record.getWiseflowIdentifier()) &&
               Objects.equals(getBrageLocation(), record.getBrageLocation()) &&
               Objects.equals(getErrors(), record.getErrors()) &&
               Objects.equals(getWarnings(), record.getWarnings()) &&
               Objects.equals(getLink(), record.getLink()) &&
               Objects.equals(getSubjects(), record.getSubjects()) &&
               Objects.equals(getSubjectCode(), record.getSubjectCode()) &&
               Objects.equals(getAccessCode(), record.getAccessCode()) &&
               Objects.equals(getProjects(), record.getProjects()) &&
               Objects.equals(prioritizedFields, record.prioritizedFields);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getEntityDescription(), getCustomer(), getId(), getDoi(), getType(),
                            getPublisherAuthority(),
                            getRightsholder(), getSpatialCoverage(), getPartOf(), getPart(), getPublication(),
                            getContentBundle(), getPublishedDate(), getCristinId(), getInsperaIdentifier(),
                            getWiseflowIdentifier(), getBrageLocation(), getErrors(), getWarnings(), getLink(),
                            getSubjects(), getSubjectCode(), getAccessCode(), getProjects(), prioritizedFields);
    }

    @JsonProperty("projects")
    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    @JsonProperty("insperaIdentifier")
    public String getInsperaIdentifier() {
        return insperaIdentifier;
    }

    @JsonProperty("wiseflowIdentifier")
    public String getWiseflowIdentifier() {
        return wiseflowIdentifier;
    }

    public void setInsperaIdentifier(String insperaIdentifier) {
        this.insperaIdentifier = insperaIdentifier;
    }

    public void setWiseflowIdentifier(String wiseflowIdentifier) {
        this.wiseflowIdentifier = wiseflowIdentifier;
    }

    @JsonProperty("subjectCode")
    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public List<URI> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<URI> subjects) {
        this.subjects = subjects;
    }

    @JsonProperty("link")
    public URI getLink() {
        return link;
    }

    public void setLink(URI link) {
        this.link = link;
    }

    @JsonProperty("hasPart")
    public List<String> getPart() {
        return part;
    }

    public void setPart(List<String> part) {
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
    public Set<WarningDetails> getWarnings() {
        return warnings;
    }

    public void setWarnings(Set<WarningDetails> warnings) {
        this.warnings = warnings;
    }

    @JsonProperty("errors")
    public Set<ErrorDetails> getErrors() {
        return errors;
    }

    public void setErrors(Set<ErrorDetails> errors) {
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

    @JsonProperty("prioritizedProperties")
    public Set<String> getPrioritizedProperties() {
        return nonNull(prioritizedFields) ? prioritizedFields : new HashSet<>();
    }

    public void setPrioritizedProperties(Set<String> prioritizedFields) {
        this.prioritizedFields = prioritizedFields;
    }

    @JsonProperty("entityDescription")
    public EntityDescription getEntityDescription() {
        return entityDescription;
    }

    public void setEntityDescription(EntityDescription entityDescription) {
        this.entityDescription = entityDescription;
    }

    public String toJsonString() throws JsonProcessingException {
        return JsonUtils.singleLineObjectMapper.writeValueAsString(this);
    }

    public boolean hasOrigin(String handle) {
        var recordOriginCollection = this.getBrageLocation().split("/")[0];
        return handle.split("/")[1].equals(recordOriginCollection);
    }

    @JsonIgnore
    public boolean isDegree(){
        return nonNull(type) && StringUtils.isNotEmpty(type.getNva()) && DEGREES.contains(type.getNva());
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }
}

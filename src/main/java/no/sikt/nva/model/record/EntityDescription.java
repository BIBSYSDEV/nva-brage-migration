package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class EntityDescription {

    private List<String> descriptions;
    private List<String> abstracts;
    private String mainTitle;
    private List<String> alternativeTitles;
    private List<Contributor> contributors;
    private List<String> tags;
    private PublicationInstance publicationInstance;

    @JsonProperty("publicationInstance")
    public PublicationInstance getPublicationInstance() {
        return publicationInstance;
    }

    @JacocoGenerated
    public void setPublicationInstance(PublicationInstance publicationInstance) {
        this.publicationInstance = publicationInstance;
    }

    @JacocoGenerated
    @JsonProperty("tags")
    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("contributors")
    public List<Contributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    @JsonProperty("mainTitle")
    public String getMainTitle() {
        return this.mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    @JacocoGenerated
    @JsonProperty
    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    @JacocoGenerated
    @JsonProperty
    public List<String> getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(List<String> abstracts) {
        this.abstracts = abstracts;
    }

    @JsonProperty("alternativeTitles")
    public List<String> getAlternativeTitles() {
        return alternativeTitles;
    }

    public void setAlternativeTitles(List<String> alternativeTitles) {
        this.alternativeTitles = alternativeTitles;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(descriptions, abstracts, mainTitle, alternativeTitles, contributors, tags,
                            publicationInstance);
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
        EntityDescription that = (EntityDescription) o;
        return Objects.equals(descriptions, that.descriptions)
               && Objects.equals(abstracts, that.abstracts)
               && Objects.equals(mainTitle, that.mainTitle)
               && Objects.equals(alternativeTitles, that.alternativeTitles)
               && Objects.equals(contributors, that.contributors)
               && Objects.equals(tags, that.tags)
               && Objects.equals(publicationInstance, that.publicationInstance);
    }
}

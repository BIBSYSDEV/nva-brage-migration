package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import nva.commons.core.JacocoGenerated;

public class EntityDescription {

    private PublicationDate publicationDate;
    private List<String> descriptions;
    private Set<String> abstracts;
    private String mainTitle;
    private Set<String> alternativeTitles;
    private Set<Contributor> contributors;
    private Set<String> tags;
    private PublicationInstance publicationInstance;
    private Language language;

    public EntityDescription() {

    }

    @JsonProperty("language")
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @JsonProperty("publicationDate")
    public PublicationDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(PublicationDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    @JsonProperty("publicationInstance")
    public PublicationInstance getPublicationInstance() {
        return publicationInstance;
    }

    public void setPublicationInstance(PublicationInstance publicationInstance) {
        this.publicationInstance = publicationInstance;
    }

    @JsonProperty("tags")
    public Set<String> getTags() {
        return this.tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("contributors")
    public Set<Contributor> getContributors() {
        return contributors;
    }

    public void setContributors(Set<Contributor> contributors) {
        this.contributors = contributors;
    }

    @JsonProperty("mainTitle")
    public String getMainTitle() {
        return this.mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    @JsonProperty
    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    @JsonProperty
    public Set<String> getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(Set<String> abstracts) {
        this.abstracts = abstracts;
    }

    @JsonProperty("alternativeTitles")
    public Set<String> getAlternativeTitles() {
        return alternativeTitles;
    }

    public void setAlternativeTitles(Set<String> alternativeTitles) {
        this.alternativeTitles = alternativeTitles;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getPublicationDate(), getDescriptions(), getAbstracts(), getMainTitle(),
                            getAlternativeTitles(),
                            getContributors(), getTags(), getPublicationInstance(), getLanguage());
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
        return Objects.equals(getPublicationDate(), that.getPublicationDate())
               && Objects.equals(getDescriptions(), that.getDescriptions())
               && Objects.equals(getAbstracts(), that.getAbstracts())
               && Objects.equals(getMainTitle(), that.getMainTitle())
               && Objects.equals(getAlternativeTitles(), that.getAlternativeTitles())
               && Objects.equals(getContributors(), that.getContributors())
               && Objects.equals(getTags(), that.getTags())
               && Objects.equals(getPublicationInstance(), that.getPublicationInstance())
               && Objects.equals(getLanguage(), that.getLanguage());
    }
}

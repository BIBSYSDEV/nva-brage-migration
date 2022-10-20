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

    @JacocoGenerated
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

    @Override
    public int hashCode() {
        return Objects.hash(descriptions,
                            abstracts,
                            alternativeTitles,
                            contributors,
                            tags,
                            mainTitle);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof EntityDescription)) {
            return false;
        }
        var entityDescription = (EntityDescription) obj;
        return Objects.equals(descriptions, entityDescription.descriptions)
               && Objects.equals(abstracts, entityDescription.abstracts)
               && Objects.equals(mainTitle, entityDescription.mainTitle)
               && Objects.equals(alternativeTitles, entityDescription.alternativeTitles)
               && Objects.equals(tags, entityDescription.tags)
               && Objects.equals(contributors, entityDescription.contributors);
    }
}

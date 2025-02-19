package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PrioritizedProperties {

    CONTRIBUTORS_WITH_CREATOR_ROLE("contributorsWithCreatorRole"),
    MAIN_TITLE("mainTitle"),
    ALTERNATIVE_TITLES("alternativeTitles"),
    ABSTRACT("abstract"),
    ALTERNATIVE_ABSTRACTS("alternativeAbstracts"),
    FUNDINGS("fundings"),
    REFERENCE("reference"),
    TAGS("tags"),
    PUBLISHER("publisher"),
    PUBLICATION_DATE("publicationDate");

    private final String value;

    PrioritizedProperties(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}

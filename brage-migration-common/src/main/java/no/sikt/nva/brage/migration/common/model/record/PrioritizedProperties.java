package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PrioritizedProperties {

    MAIN_TITLE("mainTitle"),
    ALTERNATIVE_TITLES("alternativeTitles"),
    ABSTRACT("abstract"),
    ALTERNATIVE_ABSTRACTS("alternativeAbstracts"),
    FUNDINGS("fundings"),
    REFERENCE("reference"),
    TAGS("tags"),
    PUBLISHER("publisher");

    private final String value;

    PrioritizedProperties(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}

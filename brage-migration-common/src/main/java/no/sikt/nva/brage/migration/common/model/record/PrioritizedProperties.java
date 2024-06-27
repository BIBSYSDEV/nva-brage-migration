package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PrioritizedProperties {

    PUBLISHER("publisher"),
    CONTRIBUTORS_WITH_AUTHOR_ROLE("contributorsWithAuthorRole");

    private final String value;

    PrioritizedProperties(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}

package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PrioritizedProperties {

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
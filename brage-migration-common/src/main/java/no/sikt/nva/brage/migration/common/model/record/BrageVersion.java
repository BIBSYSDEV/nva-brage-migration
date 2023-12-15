package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BrageVersion {
    ACCEPTED("Akseptert"),
    PUBLISHED("Publisert");

    private final String value;

    BrageVersion(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        for (BrageVersion version : BrageVersion.values()) {
            if (version.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

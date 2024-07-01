package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum PublisherVersion {
    PUBLISHED_VERSION("PublishedVersion"),
    ACCEPTED_VERSION("AcceptedVersion");

    private final String value;

    PublisherVersion(String value) {
        this.value = value;
    }

    public static PublisherVersion fromValue(String value) {
        return Arrays.stream(PublisherVersion.values())
                   .filter(version -> version.getValue().equalsIgnoreCase(value))
                   .findFirst()
                   .orElseThrow();
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

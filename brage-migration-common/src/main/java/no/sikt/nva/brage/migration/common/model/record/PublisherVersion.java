package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PublisherVersion {
    PUBLISHED_VERSION("PublishedVersion"),
    ACCEPTED_VERSION("AcceptedVersion");

    private final String value;

    PublisherVersion(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
package no.sikt.nva.brage.migration.common.model.record;

import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.StringUtils.WHITESPACES;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;

public enum PublisherVersion {
    PUBLISHED_VERSION("PublishedVersion"),
    ACCEPTED_VERSION("AcceptedVersion");

    private final String value;

    PublisherVersion(String value) {
        this.value = value;
    }

    public static Optional<PublisherVersion> fromValue(String value) {
        return Arrays.stream(PublisherVersion.values())
                   .filter(version -> version.getValue().equalsIgnoreCase(trim(value)))
                   .findFirst();
    }

    private static String trim(String value) {
        return value.replaceAll(WHITESPACES, EMPTY_STRING).trim();
    }

    public static boolean isSupportedPublisherVersion(String value) {
        return Arrays.stream(PublisherVersion.values())
            .anyMatch(version -> version.getValue().equalsIgnoreCase(value));
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

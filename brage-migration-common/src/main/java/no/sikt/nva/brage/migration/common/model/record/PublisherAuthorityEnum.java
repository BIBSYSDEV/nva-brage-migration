package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;

public enum PublisherAuthorityEnum {
    ACCEPTED("Akseptert"),
    PUBLISHED("Publisert");

    private final String value;

    PublisherAuthorityEnum(String value) {
        this.value = value;
    }

    public static PublisherAuthorityEnum fromValue(String value) {
        for (PublisherAuthorityEnum pubAuth : PublisherAuthorityEnum.values()) {
            if (pubAuth.getValue().equalsIgnoreCase(value)) {
                return pubAuth;
            }
        }
        return null;
    }

    public static boolean isValid(String value) {
        for (PublisherAuthorityEnum pubAuth : PublisherAuthorityEnum.values()) {
            if (pubAuth.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public PublisherAuthority toPublisherAuthority() {
        if (ACCEPTED.getValue().equals(this.getValue())) {
            return new PublisherAuthority(Collections.singleton("acceptedVersion"), false);
        }
        if (PUBLISHED.getValue().equals(this.getValue())) {
            return new PublisherAuthority(Collections.singleton("publishedVersion"), true);
        }
        return null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

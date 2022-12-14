package no.sikt.nva.brage.migration.common.model.record.license;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NvaLicenseIdentifier {
    CC_BY("CC BY"),
    CC_BY_NC("CC BY-NC"),
    CC_BY_NC_ND("CC BY-NC-ND"),
    CC_BY_NC_SA("CC BY-NC-SA"),
    CC_BY_ND("CC BY-ND"),
    CC_BY_SA("CC BY-SA"),
    CC_ZERO("CC0"),
    DEFAULT_LICENSE("RightsReserved");
    private final String value;

    NvaLicenseIdentifier(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

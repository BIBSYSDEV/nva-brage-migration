package no.sikt.nva.brage.migration.common.model.record.license;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BrageLicense {
    CC_BY("CC BY"),
    CC_BY_NC("CC BY-NC"),
    CC_BY_NC_ND("CC BY-NC-ND"),
    CC_BY_NC_SA("CC BY-NC-SA"),
    CC_BY_ND("CC BY-ND"),
    CC_BY_SA("CC BY-SA"),
    CC_ZERO("zero"),
    UTGIVERS_BETINGELSER("Utgivers betingelser");

    private final String value;

    BrageLicense(String value) {
        this.value = value;
    }

    public static BrageLicense fromValue(String value) {
        for (BrageLicense license : BrageLicense.values()) {
            if (license.getValue().equalsIgnoreCase(value)) {
                return license;
            }
        }
        return null;
    }

    public static boolean isValid(String value) {
        for (BrageLicense licence : BrageLicense.values()) {
            if (licence.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public String getUriValue() {
        switch (this) {
            case CC_BY:
                return "by";
            case CC_BY_NC:
                return "by-nc";
            case CC_BY_NC_ND:
                return "by-nc-nd";
            case CC_BY_NC_SA:
                return "by-nc-sa";
            case CC_BY_ND:
                return "by-nd";
            case CC_BY_SA:
                return "by-sa";
            case UTGIVERS_BETINGELSER:
                return "InC";
            case CC_ZERO:
            default:
                return "";
        }
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

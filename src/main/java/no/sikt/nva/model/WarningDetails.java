package no.sikt.nva.model;

import java.util.List;
import java.util.Objects;

public class WarningDetails {

    private final Warning warningCode;
    private final List<String> details;

    public WarningDetails(Warning warningCode, List<String> details) {
        this.warningCode = warningCode;
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WarningDetails)) {
            return false;
        }
        var warningDetail = (WarningDetails) o;
        return this.warningCode.equals(warningDetail.warningCode);
    }

    @Override
    public String toString() {
        return warningCode + " = " + details;
    }

    @Override
    public int hashCode() {
        return Objects.hash(warningCode);
    }

    public enum Warning {
        VERSION_WARNING,
        SUBJECT_WARNING
    }

}
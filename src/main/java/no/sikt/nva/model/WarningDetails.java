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

    public WarningDetails(Warning warningCode, String detail) {
        this.warningCode = warningCode;
        this.details = List.of(detail);
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
        SUBJECT_WARNING,
        INVALID_DATE_WARNING,
        DATE_NOT_PRESENT_WARNING,
        LANGUAGE_MAPPED_TO_UNDEFINED,
        MULTIPLE_DESCRIPTION_PRESENT,
        VOLUME_NOT_NUMBER_WARNING,
        ISSUE_NOT_NUMBER_WARNING,
        PAGE_NUMBER_FORMAT_NOT_RECOGNIZED,
        MULTIPLE_UNMAPPABLE_TYPES,
        CRISTIN_ID_PRESENT
    }

}

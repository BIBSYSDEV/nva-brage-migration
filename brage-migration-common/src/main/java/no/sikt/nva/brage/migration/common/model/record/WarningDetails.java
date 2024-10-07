package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class WarningDetails {

    private final Warning warningCode;
    private Collection<String> details;

    public WarningDetails(@JsonProperty("warningCode") Warning warningCode,
                          @JsonProperty("details") Collection<String> details) {
        this.warningCode = warningCode;
        this.details = details;
    }

    public WarningDetails(Warning warningCode, String detail) {
        this.warningCode = warningCode;
        this.details = Collections.singleton(detail);
    }

    public WarningDetails(Warning warningCode) {
        this.warningCode = warningCode;
    }

    @JsonProperty("warningCode")
    public Warning getWarningCode() {
        return warningCode;
    }

    @JsonProperty("details")
    public Collection<String> getDetails() {
        return details;
    }

    public void setDetails(Set<String> details) {
        this.details = details;
    }

    @Override
    public int hashCode() {
        return Objects.hash(warningCode);
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

    public enum Warning {
        SUBJECT_WARNING,
        LANGUAGE_MAPPED_TO_UNDEFINED,
        MULTIPLE_DESCRIPTION_PRESENT,
        VOLUME_NOT_NUMBER_WARNING,
        ISSUE_NOT_NUMBER_WARNING,
        PAGE_NUMBER_FORMAT_NOT_RECOGNIZED,
        MULTIPLE_ISBN_VALUES_WARNING,
        INVALID_ISBN_WARNING,
        NONEXISTENT_COLLECTION,
        EMPTY_COLLECTION,
        NO_CONTRIBUTORS,
        MISSING_FILES,
        CONFERENCE_OBJECT_OR_LECTURE_WILL_BE_MAPPED_TO_CONFERENCE_REPORT,
        UNKNOWN_PROJECT

    }
}

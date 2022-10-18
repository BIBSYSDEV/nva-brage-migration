package no.sikt.nva.model;

import java.util.List;
import java.util.Objects;

public class ErrorDetails {

    private final Error errorCode;
    private final List<String> details;

    public ErrorDetails(Error errorCode, List<String> details) {
        this.errorCode = errorCode;
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorDetails)) {
            return false;
        }
        var errorDetail = (ErrorDetails) o;
        return this.errorCode.equals(errorDetail.errorCode);
    }

    @Override
    public String toString() {
        return errorCode + " = " + details;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode);
    }

    public enum Error {
        CRISTIN_ID_PRESENT,
        INVALID_TYPE,
        INVALID_ISSN,
        INVALID_ISBN
    }
}

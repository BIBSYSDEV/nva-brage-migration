package no.sikt.nva.exceptions;

public class LicenseExtractingException extends RuntimeException {

    public LicenseExtractingException(String message, Exception exception) {
        super(message, exception);
    }
}

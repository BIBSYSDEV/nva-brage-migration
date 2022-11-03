package no.sikt.nva.exceptions;

public class ContentException extends Exception {

    public ContentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentException(String message) {
        super(message);
    }
}


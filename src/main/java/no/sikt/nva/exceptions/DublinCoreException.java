package no.sikt.nva.exceptions;

public class DublinCoreException extends RuntimeException {


    public DublinCoreException(String message) {
        super(message);
    }

    public DublinCoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DublinCoreException(String message, Exception exception) {
        super(message, exception);
    }
}

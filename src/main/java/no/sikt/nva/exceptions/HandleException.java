package no.sikt.nva.exceptions;

public class HandleException extends RuntimeException {

    public HandleException(Throwable cause) {
        super(cause);
    }

    public HandleException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandleException(String message) {
        super(message);
    }
}

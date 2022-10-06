package no.sikt.nva.exceptions;

public class RecordsWriterException extends RuntimeException {

    public RecordsWriterException(String message, Exception exception) {
        super(message, exception);
    }

}

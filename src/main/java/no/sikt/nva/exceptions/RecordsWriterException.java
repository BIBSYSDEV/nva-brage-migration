package no.sikt.nva.exceptions;

public class RecordsWriterException extends RuntimeException {

    public RecordsWriterException(String message, String filename) {
        super(message + " " + filename);
    }
}

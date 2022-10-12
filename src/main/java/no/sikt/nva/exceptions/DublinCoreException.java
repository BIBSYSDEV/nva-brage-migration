package no.sikt.nva.exceptions;

public class DublinCoreException extends RuntimeException {

    public static final String DELIMITER = ", ";

    public static final String PROBLEM_LIST_EXPLANATION =
        "The dublin_core.xml has the following problems: %s";

    public DublinCoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DublinCoreException(String message, Exception exception) {
        super(message, exception);
    }
}

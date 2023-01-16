package no.sikt.nva.exceptions;

public class ContributorExtractorException extends RuntimeException {

    public ContributorExtractorException(String message, Exception exception) {
        super(message, exception);
    }


}

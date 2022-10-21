package no.sikt.nva.exceptions;

import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.model.ErrorDetails;

public class DublinCoreException extends RuntimeException {

    public static final String DELIMITER = ", ";

    public DublinCoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DublinCoreException(String message) {
        super(message);
    }

    public DublinCoreException(List<ErrorDetails> errorList) {
        super(collectProblems(errorList));
    }

    private static String collectProblems(List<ErrorDetails> errorList) {
        var problemsAsString = errorList.stream()
                                   .map(ErrorDetails::toString)
                                   .collect(Collectors.toList());
        return String.join(DELIMITER, problemsAsString);
    }
}

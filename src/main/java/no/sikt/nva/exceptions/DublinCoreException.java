package no.sikt.nva.exceptions;

import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.DublinCoreValidator.Error;

public class DublinCoreException extends RuntimeException {

    public static final String DELIMITER = ", ";

    public static final String PROBLEM_LIST_EXPLANATION =
        "The dublin_core.xml has the following errors: %s";

    public DublinCoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DublinCoreException(List<Error> errorList) {
        super(String.format(PROBLEM_LIST_EXPLANATION, collectProblems(errorList)));
    }

    private static String collectProblems(List<Error> errorList) {
        var problemsAsString = errorList.stream().map(Enum::toString).collect(Collectors.toList());
        return String.join(DELIMITER, problemsAsString);
    }
}

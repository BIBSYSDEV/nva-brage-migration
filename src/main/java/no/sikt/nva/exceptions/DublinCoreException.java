package no.sikt.nva.exceptions;

import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.DublinCoreValidator.Problem;

public class DublinCoreException extends RuntimeException {

    public static final String DELIMITER = ", ";

    public static final String PROBLEM_LIST_EXPLANATION =
        "The dublin_core.xml has the following problems: %s";

    public DublinCoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public DublinCoreException(List<Problem> problemList) {
        super(String.format(PROBLEM_LIST_EXPLANATION, collectProblems(problemList)));
    }

    private static String collectProblems(List<Problem> problemList) {
        var problemsAsString = problemList.stream().map(Enum::toString).collect(Collectors.toList());
        return String.join(DELIMITER, problemsAsString);
    }
}

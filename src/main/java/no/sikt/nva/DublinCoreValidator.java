package no.sikt.nva;

import java.util.ArrayList;
import java.util.List;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.ISSNValidator;

public final class DublinCoreValidator {

    public static List<Problem> getDublinCoreErrors(DublinCore dublinCore) {
        var problems = new ArrayList<Problem>();
        if (hasCristinIdentifier(dublinCore)) {
            problems.add(Problem.CRISTIN_ID_PRESENT);
        }
        if (!isValidIssn(dublinCore)) {
            problems.add(Problem.INVALID_ISSN);
        }
        if (!isValidIsbn(dublinCore)) {
            problems.add(Problem.INVALID_ISBN);
        }
        return problems;
    }

    private static boolean hasCristinIdentifier(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isCristinDcValue);
    }

    private static boolean isValidIssn(DublinCore dublinCore) {
        if (hasIssn(dublinCore)) {
            var issn = DublinCoreParser.extractIssn(dublinCore);
            ISSNValidator validator = new ISSNValidator();
            return validator.isValid(issn);
        }
        return true;
    }

    private static boolean isValidIsbn(DublinCore dublinCore) {
        if (hasIsbn(dublinCore)) {
            var isbn = DublinCoreParser.extractIsbn(dublinCore);
            ISBNValidator validator = new ISBNValidator();
            return validator.isValid(isbn);
        }
        return true;
    }

    private static boolean hasIssn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isIssnValue);
    }

    private static boolean hasIsbn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isIsbnValue);
    }

    public enum Problem {
        CRISTIN_ID_PRESENT,
        INVALID_ISSN,
        INVALID_ISBN
    }
}

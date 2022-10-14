package no.sikt.nva;

import java.util.ArrayList;
import java.util.List;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.ISSNValidator;

public final class DublinCoreValidator {

    public static final String VERSION_STRING_NVE = "publishedVersion";

    public static List<Error> getDublinCoreErrors(DublinCore dublinCore) {
        var problems = new ArrayList<Error>();
        if (hasCristinIdentifier(dublinCore)) {
            problems.add(Error.CRISTIN_ID_PRESENT);
        }
        if (!containsPresentValidIssn(dublinCore)) {
            problems.add(Error.INVALID_ISSN);
        }
        if (!containsPresentValidIsbn(dublinCore)) {
            problems.add(Error.INVALID_ISBN);
        }
        return problems;
    }

    public static List<Warning> getDublinCoreWarnings(DublinCore dublinCore) {
        var warnings = new ArrayList<Warning>();
        if (!versionIsEitherMissingOrValid(dublinCore)) {
            warnings.add(Warning.VERSION_WARNING);
        }
        return warnings;
    }

    private static boolean hasCristinIdentifier(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isCristinDcValue);
    }

    private static boolean containsPresentValidIssn(DublinCore dublinCore) {
        if (hasIssn(dublinCore)) {
            var issn = DublinCoreParser.extractIssn(dublinCore);
            ISSNValidator validator = new ISSNValidator();
            return validator.isValid(issn);
        }
        return true;
    }

    private static boolean containsPresentValidIsbn(DublinCore dublinCore) {
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

    private static boolean versionIsEitherMissingOrValid(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isVersion)
                   .findAny().map(DublinCoreValidator::isValidVersion)
                   .orElse(false);
    }

    private static boolean isValidVersion(DcValue version) {
        return VERSION_STRING_NVE.equals(version.getValue());
    }

    public enum Error {
        CRISTIN_ID_PRESENT,
        INVALID_ISSN,
        INVALID_ISBN
    }

    public enum Warning {
        VERSION_WARNING
    }
}

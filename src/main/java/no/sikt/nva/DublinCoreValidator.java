package no.sikt.nva;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.ISSNValidator;

public final class DublinCoreValidator {

    public static final String VERSION_STRING_NVE = "publishedVersion";

    public static List<Error> getDublinCoreErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        var problems = new ArrayList<Error>();
        if (hasCristinIdentifier(dublinCore)) {
            problems.add(Error.CRISTIN_ID_PRESENT);
        }
        if (!hasValidType(dublinCore)) {
            problems.add(Error.INVALID_TYPE);
        }
        if (!containsPresentValidIssn(dublinCore, brageLocation)) {
            problems.add(Error.INVALID_ISSN);
        }
        if (!containsPresentValidIsbn(dublinCore, brageLocation)) {
            problems.add(Error.INVALID_ISBN);
        }
        return problems;
    }

    public static List<Warning> getDublinCoreWarnings(DublinCore dublinCore) {
        var warnings = new ArrayList<Warning>();
        if (!versionIsValid(dublinCore) && versionIsPresent(dublinCore)) {
            warnings.add(Warning.VERSION_WARNING);
        }
        SubjectScraper.getSubjectsWwarnings(dublinCore).ifPresent(warnings::add);

        return warnings;
    }

    private static boolean hasCristinIdentifier(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isCristinDcValue);
    }

    private static boolean hasValidType(DublinCore dublinCore) {
        var types = dublinCore.getDcValues().stream()
                        .filter(DcValue::isType)
                        .map(DcValue::getValue)
                        .collect(Collectors.toList());

        return TypeMapper.hasValidTypes(types);
    }

    private static boolean containsPresentValidIssn(DublinCore dublinCore, BrageLocation brageLocation) {
        if (hasIssn(dublinCore)) {
            var issn = DublinCoreParser.extractIssn(dublinCore, brageLocation);
            ISSNValidator validator = new ISSNValidator();
            return validator.isValid(issn);
        }
        return true;
    }

    private static boolean containsPresentValidIsbn(DublinCore dublinCore, BrageLocation brageLocation) {
        if (hasIsbn(dublinCore)) {
            var isbn = DublinCoreParser.extractIsbn(dublinCore, brageLocation);
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

    private static boolean versionIsValid(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isVersion)
                   .findAny().map(DublinCoreValidator::isValidVersion).orElse(false);
    }

    private static boolean versionIsPresent(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isVersion);
    }

    private static boolean isValidVersion(DcValue version) {
        return VERSION_STRING_NVE.equals(version.getValue());
    }

    public enum Error {
        CRISTIN_ID_PRESENT,
        INVALID_TYPE,
        INVALID_ISSN,
        INVALID_ISBN
    }

    public enum Warning {
        VERSION_WARNING,
        SUBJECT_WARNING
    }
}

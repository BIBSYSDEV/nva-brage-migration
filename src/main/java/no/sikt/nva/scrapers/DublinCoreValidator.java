package no.sikt.nva.scrapers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.ErrorDetails.Error;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.ISSNValidator;

public final class DublinCoreValidator {

    public static final String VERSION_STRING_NVE = "publishedVersion";

    public static List<ErrorDetails> getDublinCoreErrors(DublinCore dublinCore, BrageLocation brageLocation) {

        var errors = new ArrayList<ErrorDetails>();
        geCristinidErrorDetails(dublinCore).ifPresent(errors::add);
        getInvalidTypes(dublinCore).ifPresent(errors::add);
        getIssnErrors(dublinCore, brageLocation).ifPresent(errors::add);
        getIsbnErrors(dublinCore, brageLocation).ifPresent(errors::add);
        return errors;
    }

    public static Optional<ErrorDetails> geCristinidErrorDetails(DublinCore dublinCore) {
        var errorList = getCristinIdentifierErrors(dublinCore);
        if (errorList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new ErrorDetails(Error.CRISTIN_ID_PRESENT, errorList));
        }
    }

    public static List<WarningDetails> getDublinCoreWarnings(DublinCore dublinCore) {
        var warnings = new ArrayList<WarningDetails>();
        getVersionWarnings(dublinCore).ifPresent(warnings::add);
        SubjectScraper.getSubjectsWarnings(dublinCore).ifPresent(warnings::add);
        return warnings;
    }

    private static Optional<WarningDetails> getVersionWarnings(DublinCore dublinCore) {
        if (!versionIsPresent(dublinCore)) {
            return Optional.empty();
        }
        return getVersionWarning(dublinCore);
    }

    private static List<String> getCristinIdentifierErrors(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isCristinDcValue).map(DcValue::toXmlString).collect(
                Collectors.toList());
    }

    private static Optional<ErrorDetails> getInvalidTypes(DublinCore dublinCore) {
        var types = dublinCore.getDcValues().stream()
                        .filter(DcValue::isType)
                        .map(DcValue::getValue)
                        .collect(Collectors.toList());

        if (TypeMapper.hasValidTypes(types)) {
            return Optional.empty();
        } else {
            return Optional.of(new ErrorDetails(Error.INVALID_TYPE, types));
        }
    }

    private static Optional<ErrorDetails> getIssnErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        if (hasIssn(dublinCore)) {
            var issn = DublinCoreScraper.extractIssn(dublinCore, brageLocation);
            ISSNValidator validator = new ISSNValidator();
            if (!validator.isValid(issn)) {
                return Optional.of(new ErrorDetails(Error.INVALID_ISSN, List.of(issn)));
            }
        }
        return Optional.empty();
    }

    private static Optional<ErrorDetails> getIsbnErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        if (hasIsbn(dublinCore)) {
            var isbn = DublinCoreScraper.extractIsbn(dublinCore, brageLocation);
            ISBNValidator validator = new ISBNValidator();
            if (!validator.isValid(isbn)) {
                return Optional.of(new ErrorDetails(Error.INVALID_ISBN, List.of(isbn)));
            }
        }
        return Optional.empty();
    }

    private static boolean hasIssn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isIssnValue);
    }

    private static boolean hasIsbn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isIsbnValue);
    }

    private static Optional<WarningDetails> getVersionWarning(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DublinCoreValidator::dcValueIsVersionAndVersionIsUnvalid)
                   .findAny()
                   .map(dcValue -> new WarningDetails(WarningDetails.Warning.VERSION_WARNING,
                                                      List.of(dcValue.getValue())));
    }

    private static boolean dcValueIsVersionAndVersionIsUnvalid(DcValue dcValue) {
        return dcValue.isVersion() && !isValidVersion(dcValue);
    }

    private static boolean versionIsPresent(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isVersion);
    }

    private static boolean isValidVersion(DcValue version) {
        return VERSION_STRING_NVE.equals(version.getValue());
    }
}
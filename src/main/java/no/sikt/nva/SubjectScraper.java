package no.sikt.nva;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.DublinCoreValidator.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Qualifier;
import nva.commons.core.JacocoGenerated;

public final class SubjectScraper {

    @JacocoGenerated
    private SubjectScraper() {

    }

    public static Optional<Warning> getSubjectsWarnings(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(SubjectScraper::isUnrecognizedSubjectType)
                   .findAny()
                   .map(dcValue -> Warning.SUBJECT_WARNING);
    }

    public static List<String> extractTags(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isSubject)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(
                       Collectors.toList());
    }

    private static boolean isUnrecognizedSubjectType(DcValue dcValue) {
        return dcValue.isSubject() && isNotRecognized(dcValue);
    }

    private static boolean isNotRecognized(DcValue dcValue) {
        return !Qualifier.NONE.equals(dcValue.getQualifier()) && !Qualifier.KEYWORD.equals(dcValue.getQualifier());
    }
}

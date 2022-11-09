package no.sikt.nva.scrapers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Qualifier;
import nva.commons.core.JacocoGenerated;

public final class SubjectScraper {

    @JacocoGenerated
    private SubjectScraper() {

    }

    public static Optional<WarningDetails> getSubjectsWarnings(DublinCore dublinCore) {
        var unrecognizedSubjects = dublinCore.getDcValues()
                                       .stream()
                                       .filter(SubjectScraper::isUnrecognizedSubjectType)
                                       .map(DcValue::toXmlString)
                                       .collect(Collectors.toList());
        if (!unrecognizedSubjects.isEmpty()) {
            return Optional.of(new WarningDetails(Warning.SUBJECT_WARNING, unrecognizedSubjects));
        } else {
            return Optional.empty();
        }
    }

    public static List<String> extractTags(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(SubjectScraper::isSubjectAndNotSpecificallyIgnored)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(
                       Collectors.toList());
    }

    private static boolean isSubjectAndNotSpecificallyIgnored(DcValue dcValue) {
        return dcValue.isSubject() && !specificallyIgnored(dcValue);
    }

    private static boolean specificallyIgnored(DcValue dcValue) {
        return Qualifier.NORWEGIAN_SCIENCE_INDEX.equals(dcValue.getQualifier());
    }

    private static boolean isUnrecognizedSubjectType(DcValue dcValue) {
        return dcValue.isSubject() && isNotRecognized(dcValue);
    }

    private static boolean isNotRecognized(DcValue dcValue) {
        return !specificallyIgnored(dcValue)
               && !Qualifier.NONE.equals(dcValue.getQualifier())
               && !Qualifier.KEYWORD.equals(dcValue.getQualifier());
    }
}

package no.sikt.nva;

import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.publisher.Publication;
import no.sikt.nva.model.record.Record;

public class DublinCoreParser {

    public static void validateAndParseDublinCore(DublinCore dublinCore, Record record) {
        var problems = DublinCoreValidator.getDublinCoreErrors(dublinCore);
        if (problems.isEmpty()) {
            updateDublinCoreFromRecord(dublinCore, record);
        } else {
            throw new DublinCoreException(problems);
        }
    }

    private static void updateDublinCoreFromRecord(DublinCore dublinCore, Record record) {
        record.setAuthors(extractAuthors(dublinCore));
        record.setPublication(extractPublication(dublinCore));
        record.setType(extractType(dublinCore));
        record.setTitle(extractTitle(dublinCore));
        record.setLanguage(extractLanguage(dublinCore));
    }

    private static String extractPublisher(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isPublisher)
                   .findAny().orElse(new DcValue()).getValue();
    }

    private static Publication extractPublication(DublinCore dublinCore) {
        var publication = new Publication();
        publication.setIssn(extractIssn(dublinCore));
        publication.setJournal(extractJournal(dublinCore));
        publication.setPublisher(extractPublisher(dublinCore));

        return publication;
    }

    private static String extractJournal(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isJournal)
                   .findAny().orElse(new DcValue()).getValue();
    }

    private static List<String> extractAuthors(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isAuthor)
                   .map(DcValue::getValue)
                   .collect(Collectors.toList());
    }

    private static String extractType(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isType)
                   .findAny().orElse(new DcValue()).getValue();
    }

    private static String extractLanguage(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isLanguage)
                   .findAny().orElse(new DcValue()).getValue();
    }

    private static String extractTitle(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isTitle)
                   .findAny().orElse(new DcValue()).getValue();
    }

    private static String extractIssn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isIssnValue)
                   .findAny().orElse(new DcValue()).getValue();
    }
}

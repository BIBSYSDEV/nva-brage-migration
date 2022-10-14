package no.sikt.nva;

import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.publisher.Publication;
import no.sikt.nva.model.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DublinCoreParser {

    public static final String FIELD_WAS_NOT_SCRAPED_IN_LOCATION_LOG_MESSAGE =
        "Field was not scraped %s in location %s";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreParser.class);

    public static Record validateAndParseDublinCore(DublinCore dublinCore, BrageLocation brageLocation) {
        var problems = DublinCoreValidator.getDublinCoreErrors(dublinCore);
        if (problems.isEmpty()) {
            var records = createRecordFromDublinCoreAndBrageLocation(dublinCore, brageLocation);
            logUnscrapedValues(dublinCore, brageLocation);
            return records;
        } else {
            throw new DublinCoreException(problems);
        }
    }

    public static String extractTitle(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isTitle)
                   .findAny().orElse(new DcValue()).getValue();
    }

    private static void logUnscrapedValues(DublinCore dublinCore, BrageLocation brageLocation) {
        dublinCore.getDcValues()
            .stream()
            .filter(DublinCoreParser::hasNotBeenScraped)
            .forEach(dcValue -> logUnscrapedDcValue(dcValue, brageLocation));
    }

    private static boolean hasNotBeenScraped(DcValue dcValue) {
        return !dcValue.isAuthor()
               && !dcValue.isIssnValue()
               && !dcValue.isTitle()
               && !dcValue.isType()
               && !dcValue.isLanguage()
               && !dcValue.isJournal()
               && !dcValue.isPublisher()
               && !dcValue.isUriIdentifier();
    }

    private static Record createRecordFromDublinCoreAndBrageLocation(DublinCore dublinCore,
                                                                     BrageLocation brageLocation) {
        var record = new Record();
        record.setId(brageLocation.getHandle());
        record.setOrigin(brageLocation.getBrageBundlePath());
        record.setAuthors(extractAuthors(dublinCore));
        record.setPublication(extractPublication(dublinCore));
        record.setType(extractType(dublinCore));
        record.setTitle(extractTitle(dublinCore));
        record.setLanguage(extractLanguage(dublinCore));
        return record;
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

    private static String extractIssn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isIssnValue)
                   .findAny().orElse(new DcValue()).getValue();
    }

    private static void logUnscrapedDcValue(DcValue dcValue, BrageLocation brageLocation) {
        logger.info(String.format(FIELD_WAS_NOT_SCRAPED_IN_LOCATION_LOG_MESSAGE,
                                  dcValue.toXmlString(),
                                  brageLocation.getOriginInformation()));
    }
}

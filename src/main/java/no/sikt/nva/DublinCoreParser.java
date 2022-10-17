package no.sikt.nva;

import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
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

    public static void validateAndParseDublinCore(DublinCore dublinCore, Record record) {
        var problems = DublinCoreValidator.getDublinCoreErrors(dublinCore);
        if (problems.isEmpty()) {
            updateDublinCoreFromRecord(dublinCore, record);
            logUnscrapedValues(dublinCore, record);
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
        record.setRightsHolder(extractRightsholder(dublinCore));
    }

    private static Publication extractPublication(DublinCore dublinCore) {
        var publication = new Publication();
        publication.setIssn(extractIssn(dublinCore));
        publication.setIsbn(extractIsbn(dublinCore));
        publication.setJournal(extractJournal(dublinCore));
        publication.setPublisher(extractPublisher(dublinCore));

        return publication;
    }

    private static void logUnscrapedValues(DublinCore dublinCore, Record record) {
        dublinCore.getDcValues()
            .stream()
            .filter(DublinCoreParser::hasNotBeenScraped)
            .forEach(dcValue -> logUnscrapedDcValue(dcValue, record));
    }

    private static void logUnscrapedDcValue(DcValue dcValue, Record record) {
        logger.info(String.format(FIELD_WAS_NOT_SCRAPED_IN_LOCATION_LOG_MESSAGE,
                                  dcValue.toXmlString(),
                                  record.getOriginInformation()));
    }

    public static String extractIssn(DublinCore dublinCore) {
        var issnList = dublinCore.getDcValues().stream()
                           .filter(DcValue::isIssnValue)
                           .map(DcValue::getValue)
                           .collect(Collectors.toList());

        return handleIssnList(issnList);
    }

    public static String extractIsbn(DublinCore dublinCore) {
        var isbnList = dublinCore.getDcValues().stream()
                           .filter(DcValue::isIsbnValue)
                           .map(DcValue::getValue)
                           .collect(Collectors.toList());

        return handleIsbnList(isbnList);
    }

    private static boolean hasNotBeenScraped(DcValue dcValue) {
        return !dcValue.isAuthor()
               && !dcValue.isIssnValue()
               && !dcValue.isTitle()
               && !dcValue.isType()
               && !dcValue.isLanguage()
               && !dcValue.isJournal()
               && !dcValue.isPublisher()
               && !dcValue.isIsbnValue();
    }

    private static String extractPublisher(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isPublisher)
                   .findAny().orElse(new DcValue()).getValue();
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

    private static String extractRightsholder(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isRightsholder)
                   .findAny().orElse(new DcValue()).getValue();
    }

    private static String handleIssnList(List<String> issnList) {
        if (issnList.isEmpty()) {
            return new DcValue().getValue();
        }

        if (isSingleton(issnList)) {
            logger.warn("Contains many issn values");
        }

        return issnList.get(0);
    }

    private static String handleIsbnList(List<String> isbnList) {
        if (isSingleton(isbnList)) {
            logger.warn("Contains many isbn values");
        }
        if (isbnList.isEmpty()) {
            return new DcValue().getValue();
        }
        return isbnList.get(0);
    }

    private static boolean isSingleton(List<String> isbnList) {
        return isbnList.size() != 1;
    }


}

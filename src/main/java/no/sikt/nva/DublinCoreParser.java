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

    public static final String FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE = "Field was not scraped\n";
    public static final String DELIMITER = "\n";
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

    public static String extractIssn(DublinCore dublinCore) {
        var issnList = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isIssnValue)
                           .map(DcValue::scrapeValueAndSetToScraped)
                           .collect(Collectors.toList());

        return handleIssnList(issnList);
    }

    public static String extractIsbn(DublinCore dublinCore) {
        var isbnList = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isIsbnValue)
                           .map(DcValue::scrapeValueAndSetToScraped)
                           .collect(Collectors.toList());

        return handleIsbnList(isbnList);
    }

    public static String extractTitle(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isTitle)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static Publication extractPublication(DublinCore dublinCore) {
        var publication = new Publication();
        publication.setIssn(extractIssn(dublinCore));
        publication.setIsbn(extractIsbn(dublinCore));
        publication.setJournal(extractJournal(dublinCore));
        publication.setPublisher(extractPublisher(dublinCore));

        return publication;
    }

    private static void logUnscrapedValues(DublinCore dublinCore, BrageLocation brageLocation) {
        List<String> unscrapedDcValues = findUnscrapedFields(dublinCore);
        logUnscrapedFields(brageLocation, unscrapedDcValues);
    }

    private static void logUnscrapedFields(BrageLocation brageLocation, List<String> unscrapedDcValues) {
        if (!unscrapedDcValues.isEmpty()) {
            logger.info(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE
                        + String.join(DELIMITER, unscrapedDcValues)
                        + DELIMITER
                        + brageLocation.getOriginInformation());
        }
    }

    private static List<String> findUnscrapedFields(DublinCore dublinCore) {
        var unscrapedDcValues = dublinCore.getDcValues()
                                    .stream()
                                    .filter(DublinCoreParser::shouldBeLoggedAsUnscraped)
                                    .map(DcValue::toXmlString)
                                    .collect(Collectors.toList());
        return unscrapedDcValues;
    }

    private static boolean shouldBeLoggedAsUnscraped(DcValue dcValue) {
        return !dcValue.isScraped() && !fieldIsIgnored(dcValue);
    }

    private static boolean fieldIsIgnored(DcValue dcValue) {
        return dcValue.isLicenseInformation() || dcValue.isHandle();
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
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPublisher)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static String extractJournal(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isJournal)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static List<String> extractAuthors(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isAuthor)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static String extractType(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isType)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static String extractLanguage(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isLanguage)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
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

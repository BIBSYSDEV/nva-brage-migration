package no.sikt.nva.scrapers;

import static no.sikt.nva.scrapers.DublinCoreValidator.VERSION_STRING_NVE;
import static no.sikt.nva.scrapers.DublinCoreValidator.getDublinCoreWarnings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.publisher.Publication;
import no.sikt.nva.model.record.Contributor;
import no.sikt.nva.model.record.Identity;
import no.sikt.nva.model.record.Record;
import no.sikt.nva.model.record.Type;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DublinCoreScraper {

    public static final String FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE = "Field was not scraped\n";
    public static final String DELIMITER = "\n";
    public static final String WARNING_TEXT = "The dublin_core.xml has following warnings: ";
    public static final String CONTRIBUTOR = "Contributor";
    public static final String ADVISOR = "Advisor";
    public static final String AUTHOR = "Author";
    public static final String EDITOR = "Editor";
    public static final String ILLUSTRATOR = "Illustrator";
    public static final String OTHER_CONTRIBUTOR = "Other";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreScraper.class);

    public static Record validateAndParseDublinCore(DublinCore dublinCore, BrageLocation brageLocation) {
        var errors = DublinCoreValidator.getDublinCoreErrors(dublinCore, brageLocation);
        var warnings = getDublinCoreWarnings(dublinCore);
        if (errors.isEmpty()) {
            var record = createRecordFromDublinCoreAndBrageLocation(dublinCore, brageLocation);
            logUnscrapedValues(dublinCore, brageLocation);
            logWarningsIfNotEmpty(brageLocation, warnings);
            return record;
        } else {
            throw new DublinCoreException(errors);
        }
    }

    public static String extractIssn(DublinCore dublinCore, BrageLocation brageLocation) {
        var issnList = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isIssnValue)
                           .map(DcValue::scrapeValueAndSetToScraped)
                           .collect(Collectors.toList());

        return handleIssnList(issnList, brageLocation);
    }

    public static String extractIsbn(DublinCore dublinCore, BrageLocation brageLocation) {
        var isbnList = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isIsbnValue)
                           .map(DcValue::scrapeValueAndSetToScraped)
                           .collect(Collectors.toList());

        return handleIsbnList(isbnList, brageLocation);
    }

    public static String extractTitle(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isTitle)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static void logWarningsIfNotEmpty(BrageLocation brageLocation, List<WarningDetails> warnings) {
        if (!warnings.isEmpty()) {
            logger.warn(WARNING_TEXT + warnings + StringUtils.SPACE + brageLocation.getOriginInformation());
        }
    }

    private static Record createRecordFromDublinCoreAndBrageLocation(DublinCore dublinCore,
                                                                     BrageLocation brageLocation) {
        var record = new Record();
        record.setId(brageLocation.getHandle());
        record.setOrigin(brageLocation.getBrageBundlePath());
        record.setPublication(extractPublication(dublinCore, brageLocation));
        record.setType(mapOriginTypeToNvaType(extractType(dublinCore)));
        record.setTitle(extractTitle(dublinCore));
        record.setLanguage(extractLanguage(dublinCore));
        record.setRightsHolder(extractRightsholder(dublinCore));
        record.setContributors(extractContributors(dublinCore));
        record.setPublisherAuthority(extractVersion(dublinCore));
        record.setTags(SubjectScraper.extractTags(dublinCore));
        return record;
    }

    private static Publication extractPublication(DublinCore dublinCore, BrageLocation brageLocation) {
        var publication = new Publication();
        publication.setIssn(extractIssn(dublinCore, brageLocation));
        publication.setIsbn(extractIsbn(dublinCore, brageLocation));
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
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DublinCoreScraper::shouldBeLoggedAsUnscraped)
                   .map(DcValue::toXmlString)
                   .collect(Collectors.toList());
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
        record.setPublication(extractPublication(dublinCore, brageLocation));
        record.setType(mapOriginTypeToNvaType(extractType(dublinCore)));
        record.setTitle(extractTitle(dublinCore));
        record.setLanguage(extractLanguage(dublinCore));
        record.setRightsHolder(extractRightsholder(dublinCore));
        record.setPublisherAuthority(extractVersion(dublinCore));
        record.setTags(SubjectScraper.extractTags(dublinCore));
        record.setDate(extractDate(dublinCore));
        record.setContributors(extractContributors(dublinCore));
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

    private static List<String> extractType(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isType)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static String extractLanguage(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isLanguage)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static String extractRightsholder(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isRightsholder)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static String extractDate(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isDate)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();

    private static List<Contributor> extractContributors(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isContributor)
                   .map(DublinCoreScraper::createContributorFromDcValue)
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private static Optional<Contributor> createContributorFromDcValue(DcValue dcValue) {
        Identity identity = new Identity(dcValue.getValue());
        if (dcValue.isAuthor()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, AUTHOR));
        }
        if (dcValue.isAdvisor()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, ADVISOR));
        }
        if (dcValue.isEditor()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, EDITOR));
        }
        if (dcValue.isIllustrator()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, ILLUSTRATOR));
        }
        if (dcValue.isOtherContributor()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, OTHER_CONTRIBUTOR));
        }
        return Optional.empty();
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static String handleIssnList(List<String> issnList, BrageLocation brageLocation) {
        if (issnList.size() > 1) {
            logger.warn("Following resource contains many issn values" + brageLocation.getOriginInformation());
        }
        if (issnList.isEmpty()) {
            return new DcValue().getValue();
        }
        return issnList.get(0);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static String handleIsbnList(List<String> isbnList, BrageLocation brageLocation) {
        if (isbnList.size() > 1) {
            logger.warn("Following resource contains many isbn values: " + brageLocation.getOriginInformation());
        }
        if (isbnList.isEmpty()) {
            return new DcValue().getValue();
        }
        return isbnList.get(0);
    }

    private static Boolean extractVersion(DublinCore dublinCore) {
        var version = dublinCore.getDcValues().stream()
                          .filter(DcValue::isVersion)
                          .findAny();
        if (version.isPresent()) {
            return mapToNvaVersion(version.get()).orElse(null);
        } else {
            return null;
        }
    }

    private static Optional<Boolean> mapToNvaVersion(DcValue version) {
        return VERSION_STRING_NVE.equals(version.scrapeValueAndSetToScraped())
                   ? Optional.of(true)
                   : Optional.empty();
    }

    private static Type mapOriginTypeToNvaType(List<String> types) {
        return new Type(types, TypeMapper.convertBrageTypeToNvaType(types));
    }
}

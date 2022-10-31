package no.sikt.nva.scrapers;

import static no.sikt.nva.scrapers.DublinCoreValidator.DEHYPHENATION_REGEX;
import static no.sikt.nva.scrapers.DublinCoreValidator.VERSION_STRING_NVE;
import static no.sikt.nva.scrapers.DublinCoreValidator.getDublinCoreWarnings;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.record.Contributor;
import no.sikt.nva.model.record.Date;
import no.sikt.nva.model.record.EntityDescription;
import no.sikt.nva.model.record.Identity;
import no.sikt.nva.model.record.Language;
import no.sikt.nva.model.record.Publication;
import no.sikt.nva.model.record.PublicationInstance;
import no.sikt.nva.model.record.Record;
import no.sikt.nva.model.record.Type;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import nva.commons.core.language.LanguageMapper;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public final class DublinCoreScraper {

    public static final String FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE = "Field was not scraped\n";
    public static final String DELIMITER = "\n";
    public static final String CONTRIBUTOR = "Contributor";
    public static final String ADVISOR = "Advisor";
    public static final String AUTHOR = "Author";
    public static final String EDITOR = "Editor";
    public static final String ILLUSTRATOR = "Illustrator";
    public static final String OTHER_CONTRIBUTOR = "Other";
    public static final String FIRST_DAY_OF_A_MONTH = "-01";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreScraper.class);
    private final boolean enableOnlineValidation;


    @JacocoGenerated
    public DublinCoreScraper(boolean enableOnlineValidation) {
        this.enableOnlineValidation = enableOnlineValidation;
    }

    public Record validateAndParseDublinCore(DublinCore dublinCore, BrageLocation brageLocation) {
        var errors = new ArrayList<ErrorDetails>();
        if (onlineValidationIsEnabled()) {
            DoiValidator.getDoiErrorDetailsOnline(dublinCore).ifPresent(errors::addAll);
        }
        errors.addAll(DublinCoreValidator.getDublinCoreErrors(dublinCore, brageLocation));
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

    public boolean onlineValidationIsEnabled() {
        return enableOnlineValidation;
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
                           .map(isbn -> isbn.replaceAll(DEHYPHENATION_REGEX, StringUtils.EMPTY_STRING))
                           .collect(Collectors.toList());

        return handleIsbnList(isbnList, brageLocation);
    }

    public static String extractMainTitle(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isMainTitle)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    public static List<String> extractAlternativeTitles(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isAlternativeTitle)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static void logWarningsIfNotEmpty(BrageLocation brageLocation, List<WarningDetails> warnings) {
        if (!warnings.isEmpty()) {
            logger.warn(warnings + StringUtils.SPACE + brageLocation.getOriginInformation());
        }
    }

    private static Record createRecordFromDublinCoreAndBrageLocation(DublinCore dublinCore,
                                                                     BrageLocation brageLocation) {
        var record = new Record();
        record.setId(brageLocation.getHandle());
        record.setOrigin(brageLocation.getBrageBundlePath());
        record.setPublication(extractPublication(dublinCore, brageLocation));
        record.setType(mapOriginTypeToNvaType(extractType(dublinCore)));
        record.setLanguage(extractLanguage(dublinCore));
        record.setRightsHolder(extractRightsholder(dublinCore));
        record.setPublisherAuthority(extractVersion(dublinCore));
        record.setDoi(extractDoi(dublinCore));
        record.setDate(extractDate(dublinCore));
        record.setEntityDescription(extractEntityDescription(dublinCore));
        record.setSpatialCoverage(extractSpatialCoverage(dublinCore));
        return record;
    }

    private static EntityDescription extractEntityDescription(DublinCore dublinCore) {
        var entityDescription = new EntityDescription();
        entityDescription.setAbstracts(extractAbstracts(dublinCore));
        entityDescription.setDescriptions(extractDescriptions(dublinCore));
        entityDescription.setMainTitle(extractMainTitle(dublinCore));
        entityDescription.setAlternativeTitles(extractAlternativeTitles(dublinCore));
        entityDescription.setContributors(extractContributors(dublinCore));
        entityDescription.setTags(SubjectScraper.extractTags(dublinCore));
        entityDescription.setPublicationInstance(extractPublicationInstance(dublinCore));
        return entityDescription;
    }

    private static List<String> extractDescriptions(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isDescription)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static List<String> extractAbstracts(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isAbstract)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static URI extractDoi(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isDoi)
                   .findFirst()
                   .map(dcValue -> UriWrapper.fromUri(dcValue.scrapeValueAndSetToScraped()).getUri())
                   .orElse(null);
    }

    private static Publication extractPublication(DublinCore dublinCore, BrageLocation brageLocation) {
        var publication = new Publication();
        publication.setIssn(extractIssn(dublinCore, brageLocation));
        publication.setIsbn(extractIsbn(dublinCore, brageLocation));
        publication.setJournal(extractJournal(dublinCore));
        publication.setPublisher(extractPublisher(dublinCore));
        publication.setPartOfSeries(extractPartOfSeries(dublinCore));
        return publication;
    }

    private static PublicationInstance extractPublicationInstance(DublinCore dublinCore) {
        var publicationInstance = new PublicationInstance();
        publicationInstance.setIssue(extractIssue(dublinCore));
        publicationInstance.setPageNumber(extractPageNumber(dublinCore));
        publicationInstance.setVolume(extractVolume(dublinCore));
        return publicationInstance;
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

    @SuppressWarnings("PMD")
    private static boolean shouldBeLoggedAsUnscraped(DcValue dcValue) {
        return !dcValue.isScraped() && !fieldHasBeenScrapedFromOtherFiles(dcValue)
               && !shouldBeIgnored(dcValue);
    }

    private static boolean fieldHasBeenScrapedFromOtherFiles(DcValue dcValue) {
        return dcValue.isLicenseInformation()
               || dcValue.isHandle();
    }

    private static boolean shouldBeIgnored(DcValue dcValue) {
        return dcValue.isCopyrightDate() || dcValue.isProjectRelation();
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

    private static String extractPartOfSeries(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPartOfSeries)
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

    private static Language extractLanguage(DublinCore dublinCore) {
        var brageLanguage = dublinCore.getDcValues()
                                .stream()
                                .filter(DcValue::isLanguage)
                                .findAny()
                                .orElse(new DcValue())
                                .scrapeValueAndSetToScraped();
        var nvaLanguage = LanguageMapper.toUri(brageLanguage);
        return new Language(brageLanguage, nvaLanguage);
    }

    private static String extractRightsholder(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isRightsholder)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static String extractSpatialCoverage(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isSpatialCoverage)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static String extractVolume(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isVolume)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static String extractIssue(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isIssue)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static String extractPageNumber(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isPageNumber)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static Date extractDate(DublinCore dublinCore) {
        var date = dublinCore.getDcValues().stream()
                       .filter(DcValue::isDate)
                       .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();

        if (isNull(date)) {
            return null;
        }
        if (DublinCoreValidator.containsYearOnly(date)) {
            return new Date(date, date);
        }
        if (DublinCoreValidator.containsYearAndMonth(date)) {
            return new Date(date, date + FIRST_DAY_OF_A_MONTH);
        }
        return new Date(date, date);
    }

    private static boolean isNull(String date) {
        return date == null;
    }

    private static List<Contributor> extractContributors(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isContributor)
                   .map(DublinCoreScraper::createContributorFromDcValue)
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private static Optional<Contributor> createContributorFromDcValue(DcValue dcValue) {
        Identity identity = new Identity(dcValue.scrapeValueAndSetToScraped());
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
        return version.flatMap(DublinCoreScraper::mapToNvaVersion).orElse(null);
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

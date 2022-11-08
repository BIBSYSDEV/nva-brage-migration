package no.sikt.nva.scrapers;

import static java.util.Objects.nonNull;
import static no.sikt.nva.validators.DublinCoreValidator.DEHYPHENATION_REGEX;
import static no.sikt.nva.validators.DublinCoreValidator.VERSION_STRING_NVE;
import static no.sikt.nva.validators.DublinCoreValidator.getDublinCoreWarnings;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.channelregister.ChannelRegister;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.record.Language;
import no.sikt.nva.model.record.Publication;
import no.sikt.nva.model.record.PublicationDate;
import no.sikt.nva.model.record.Record;
import no.sikt.nva.model.record.Type;
import no.sikt.nva.scrapers.TypeMapper.NvaType;
import no.sikt.nva.validators.DoiValidator;
import no.sikt.nva.validators.DublinCoreValidator;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import nva.commons.core.language.LanguageMapper;
import nva.commons.core.paths.UriWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public final class DublinCoreScraper {

    public static final String FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE = "Field was not scraped\n";
    public static final String DELIMITER = "\n";
    public static final ChannelRegister channelRegister = ChannelRegister.getRegister();
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreScraper.class);
    private final boolean enableOnlineValidation;

    @JacocoGenerated
    public DublinCoreScraper(boolean enableOnlineValidation) {
        this.enableOnlineValidation = enableOnlineValidation;
    }

    public static String extractIssn(DublinCore dublinCore, BrageLocation brageLocation) {
        var issnList = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isIssnValue)
                           .map(DcValue::scrapeValueAndSetToScraped)
                           .collect(Collectors.toList());

        return handleIssnList(issnList, brageLocation);
    }

    public static String getIgnoredFieldNames() {
        List<DcValue> dcValues = new ArrayList<>();
        dcValues.add(new DcValue(Element.DATE, Qualifier.COPYRIGHT, null));
        dcValues.add(new DcValue(Element.RELATION, Qualifier.PROJECT, null));
        dcValues.add(new DcValue(Element.DESCRIPTION, Qualifier.PROVENANCE, null));
        return dcValues.stream().map(DcValue::toXmlString).collect(Collectors.joining(DELIMITER));
    }

    public static String extractIsbn(DublinCore dublinCore, BrageLocation brageLocation) {
        var isbnList = dublinCore.getDcValues()
                           .stream()
                           .filter(DublinCoreScraper::dcValueIsIsbnAndNotEmpty)
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

    public static String extractJournal(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isJournal)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    public static List<String> extractType(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isType)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
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

    private static boolean dcValueIsIsbnAndNotEmpty(DcValue dcValue) {
        if (dcValue.isIsbnValue() && StringUtils.isEmpty(dcValue.getValue())) {
            dcValue.scrapeValueAndSetToScraped();
        }
        return dcValue.isIsbnValue() && StringUtils.isNotEmpty(dcValue.getValue());
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
        record.setType(mapOriginTypeToNvaType(extractType(dublinCore)));
        record.setLanguage(extractLanguage(dublinCore));
        record.setRightsHolder(extractRightsholder(dublinCore));
        record.setPublisherAuthority(extractVersion(dublinCore));
        record.setDoi(extractDoi(dublinCore));
        record.setEntityDescription(EntityDescriptionExtractor.extractEntityDescription(dublinCore));
        record.setSpatialCoverage(extractSpatialCoverage(dublinCore));
        record.setPublication(createPublicationWithIdentifier(dublinCore, brageLocation, record));
        record.setPublishedDate(extractAvailableDate(dublinCore));
        return record;
    }

    @NotNull
    private static Publication createPublicationWithIdentifier(DublinCore dublinCore, BrageLocation brageLocation,
                                                               Record record) {
        var publication = extractPublication(dublinCore, brageLocation);
        record.setPublication(publication);
        var recordType = record.getType().getNva();
        if (NvaType.JOURNAL_ARTICLE.getValue().equals(recordType)) {
            publication.setId(createPublicationIdUriForJournal(dublinCore, brageLocation, record));
        } else {
            var identifier = channelRegister.lookUpInChannelRegister(record);
            if (nonNull(identifier)) {
                publication.setId(createPublicationIdForNonJournal(record, identifier));
            }
        }
        return publication;
    }

    @NotNull
    private static String createPublicationIdForNonJournal(Record record, String identifier) {
        return identifier + "/" + createValidDateForNvaUri(record.getEntityDescription().getPublicationDate());
    }

    private static String createPublicationIdUriForJournal(DublinCore dublinCore, BrageLocation brageLocation,
                                                           Record record) {
        return createPublicationIdForNonJournal(record, channelRegister.extractIdentifier(dublinCore, brageLocation));
    }

    private static String createValidDateForNvaUri(PublicationDate publicationDate) {
        if (nonNull(publicationDate) && containsMonthOrYear(publicationDate)) {
            return publicationDate.getNva().split("-")[0];
        }
        if (nonNull(publicationDate)) {
            return publicationDate.getNva();
        }
        return StringUtils.EMPTY_STRING;
    }

    private static boolean containsMonthOrYear(PublicationDate publicationDate) {
        return Arrays.stream(publicationDate.getNva().split("-")).count() == 2
               || Arrays.stream(publicationDate.getNva().split("-")).count() == 3;
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
        return dcValue.isCopyrightDate() || dcValue.isProjectRelation() || dcValue.isProvenanceDescription();
    }

    private static String extractPublisher(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPublisher)
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

    @SuppressWarnings("PMD.PrematureDeclaration")
    private static String extractAvailableDate(DublinCore dublinCore) {
        var availableDate = dublinCore.getDcValues().stream()
                                .filter(DcValue::isAvailableDate)
                                .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
        var accessionedDate = dublinCore.getDcValues().stream()
                                  .filter(DcValue::isAccessionedDate)
                                  .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
        if (nonNull(availableDate)) {
            return availableDate;
        }
        if (nonNull(accessionedDate)) {
            return accessionedDate;
        } else {
            return null;
        }
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static String handleIssnList(List<String> issnList, BrageLocation brageLocation) {
        if (issnList.size() > 1) {
            logger.warn("Following resource contains many issn values" + brageLocation.getOriginInformation());
        }
        if (issnList.isEmpty()) {
            return StringUtils.EMPTY_STRING;
        }
        return issnList.get(0);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static String handleIsbnList(List<String> isbnList, BrageLocation brageLocation) {
        if (isbnList.size() > 1) {
            logger.warn("Following resource contains many isbn values: " + brageLocation.getOriginInformation());
        }
        if (isbnList.isEmpty()) {
            return StringUtils.EMPTY_STRING;
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

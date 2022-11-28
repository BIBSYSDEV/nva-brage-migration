package no.sikt.nva.scrapers;

import static java.util.Objects.nonNull;
import static no.sikt.nva.validators.DublinCoreValidator.ACCEPTED_VERSION_STRING;
import static no.sikt.nva.validators.DublinCoreValidator.DEHYPHENATION_REGEX;
import static no.sikt.nva.validators.DublinCoreValidator.PUBLISHED_VERSION_STRING;
import static no.sikt.nva.validators.DublinCoreValidator.getDublinCoreWarnings;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Journal;
import no.sikt.nva.brage.migration.common.model.record.Publication;
import no.sikt.nva.brage.migration.common.model.record.PublicationContext;
import no.sikt.nva.brage.migration.common.model.record.PublishedDate;
import no.sikt.nva.brage.migration.common.model.record.Publisher;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Series;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.channelregister.ChannelRegister;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.validators.DoiValidator;
import no.sikt.nva.validators.DublinCoreValidator;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public final class DublinCoreScraper {

    public static final String FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE = "Field was not scraped\n";
    public static final String DELIMITER = "\n";
    public static final ChannelRegister channelRegister = ChannelRegister.getRegister();
    public static final String SCRAPING_HAS_FAILED = "Scraping has failed: ";
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
        dcValues.add(new DcValue(Element.DESCRIPTION, Qualifier.SPONSORSHIP, null));
        dcValues.add(new DcValue(Element.IDENTIFIER, Qualifier.CITATION, null));
        dcValues.add(new DcValue(Element.SUBJECT, Qualifier.NORWEGIAN_SCIENCE_INDEX, null));
        dcValues.add(new DcValue(Element.DATE, Qualifier.CREATED, null));
        dcValues.add(new DcValue(Element.DATE, Qualifier.UPDATED, null));
        return dcValues.stream().map(DcValue::toXmlString).collect(Collectors.joining(DELIMITER));
    }

    public static String extractIsbn(DublinCore dublinCore, BrageLocation brageLocation) {
        var isbnList = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isIsbnAndNotEmptyValue)
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

    public static String extractPublisher(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPublisher)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    public Record validateAndParseDublinCore(DublinCore dublinCore, BrageLocation brageLocation) {
        try {
            var errors = DublinCoreValidator.getDublinCoreErrors(dublinCore, brageLocation);
            if (onlineValidationIsEnabled()) {
                DoiValidator.getDoiErrorDetailsOnline(dublinCore).ifPresent(errors::addAll);
            }
            var warnings = getDublinCoreWarnings(dublinCore);
            var record = createRecordFromDublinCoreAndBrageLocation(dublinCore, brageLocation);
            record.setErrors(errors);
            record.setWarnings(warnings);
            logUnscrapedValues(dublinCore, brageLocation);
            logWarningsIfNotEmpty(brageLocation, warnings);
            logErrorsIfNotEmpty(brageLocation, errors);
            return record;
        } catch (Exception e) {
            throw new DublinCoreException(SCRAPING_HAS_FAILED + e);
        }
    }

    public boolean onlineValidationIsEnabled() {
        return enableOnlineValidation;
    }

    private static void logWarningsIfNotEmpty(BrageLocation brageLocation, List<WarningDetails> warnings) {
        if (!warnings.isEmpty()) {
            logger.warn(warnings + StringUtils.SPACE + brageLocation.getOriginInformation());
        }
    }

    private static void logErrorsIfNotEmpty(BrageLocation brageLocation, List<ErrorDetails> error) {
        if (!error.isEmpty()) {
            logger.error(error + StringUtils.SPACE + brageLocation.getOriginInformation());
        }
    }

    private static Record createRecordFromDublinCoreAndBrageLocation(DublinCore dublinCore,
                                                                     BrageLocation brageLocation) {
        var record = new Record();
        record.setId(brageLocation.getHandle());
        record.setOrigin(brageLocation.getBrageBundlePath());
        record.setType(mapOriginTypeToNvaType(extractType(dublinCore)));
        record.setLanguage(BrageNvaLanguageMapper.extractLanguage(dublinCore));
        record.setRightsHolder(extractRightsholder(dublinCore));
        record.setPublisherAuthority(extractVersion(dublinCore));
        record.setDoi(extractDoi(dublinCore));
        record.setEntityDescription(EntityDescriptionExtractor.extractEntityDescription(dublinCore));
        record.setSpatialCoverage(extractSpatialCoverage(dublinCore));
        record.setPublication(createPublicationWithIdentifier(dublinCore, brageLocation, record));
        record.setPublishedDate(extractAvailableDate(dublinCore));
        record.setCristinId(extractCristinId(dublinCore));
        record.setPartOf(extractPartOf(dublinCore));
        return record;
    }

    private static Publication createPublicationWithIdentifier(DublinCore dublinCore, BrageLocation brageLocation,
                                                               Record record) {
        var publication = extractPublication(dublinCore, brageLocation);
        publication.setPublicationContext(new PublicationContext());
        publication.getPublicationContext().setBragePublisher(extractPublisher(dublinCore));
        record.setPublication(publication);

        var nvaType = record.getType().getNva();
        if (nonNull(nvaType)) {
            searchForSeriesAndJournalsInChannelRegister(dublinCore, brageLocation, record);
            searchForPublisherInChannelRegister(record);
        }
        return publication;
    }

    private static void searchForSeriesAndJournalsInChannelRegister(DublinCore dublinCore, BrageLocation brageLocation,
                                                                    Record record) {
        var nvaType = record.getType().getNva();
        if (isSearchableInJournals(nvaType)) {
            setIdFromJournals(dublinCore, brageLocation, record);
        }
    }

    private static void setIdFromJournals(DublinCore dublinCore, BrageLocation brageLocation, Record record) {
        var publication = record.getPublication();
        if (NvaType.REPORT.getValue().equals(record.getType().getNva())) {
            var seriesId = channelRegister.extractIdentifierFromJournals(dublinCore, brageLocation);
            publication.getPublicationContext().setSeries(new Series(seriesId));
        } else {
            var journalId = channelRegister.extractIdentifierFromJournals(dublinCore, brageLocation);
            if (nonNull(journalId)) {
                publication.getPublicationContext().setJournal(new Journal(journalId));
            }
        }
    }

    private static void searchForPublisherInChannelRegister(Record record) {
        var publication = record.getPublication();
        if (isSearchableInPublishers(record)) {
            var publisherId = channelRegister.lookUpInChannelRegisterForPublisher(record);
            if (nonNull(publisherId)) {
                publication.getPublicationContext().setPublisher(new Publisher(publisherId));
            }
        }
    }

    private static boolean isSearchableInJournals(String nvaType) {
        return nvaType.equals(NvaType.JOURNAL_ARTICLE.getValue())
               || nvaType.equals(NvaType.SCIENTIFIC_ARTICLE.getValue())
               || nvaType.equals(NvaType.REPORT.getValue());
    }

    private static boolean isSearchableInPublishers(Record record) {
        var publication = record.getPublication();
        var nvaType = record.getType().getNva();
        var bragePublisher = publication.getPublicationContext().getBragePublisher();
        return nonNull(bragePublisher)
               && nvaType.equals(BrageType.REPORT.getValue())
               || nvaType.equals(BrageType.BOOK.getValue())
               || nvaType.equals(NvaType.SCIENTIFIC_MONOGRAPH.getValue());
    }

    private static URI extractDoi(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isDoi)
                   .findFirst()
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(DoiValidator::updateDoiStructureIfNeeded)
                   .map(convertToUriAttempt())
                   .orElse(null);
    }

    private static Function<String, URI> convertToUriAttempt() {
        return doi -> {
            try {
                return URI.create(doi);
            } catch (Exception e) {
                return null;
            }
        };
    }

    private static Publication extractPublication(DublinCore dublinCore, BrageLocation brageLocation) {
        var publication = new Publication();
        publication.setIssn(extractIssn(dublinCore, brageLocation));
        publication.setIsbn(extractIsbn(dublinCore, brageLocation));
        publication.setJournal(extractJournal(dublinCore));
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
        return dcValue.isCopyrightDate()
               || dcValue.isProjectRelation()
               || dcValue.isProvenanceDescription()
               || dcValue.isSponsorShipDescription()
               || dcValue.isCitationIdentifier()
               || dcValue.isNsiSubject()
               || dcValue.isCreatedDate()
               || dcValue.isUpdatedDate()
               || dcValue.isRelationUri()
               || dcValue.isNoneDate();
    }

    private static String extractCristinId(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isCristinDcValue)
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

    private static String extractPartOf(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPartOf)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static String extractRightsholder(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isRightsholder)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static List<String> extractSpatialCoverage(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isSpatialCoverage)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private static PublishedDate extractAvailableDate(DublinCore dublinCore) {
        var availableDates = dublinCore.getDcValues().stream()
                                 .filter(DcValue::isAvailableDate)
                                 .map(DcValue::scrapeValueAndSetToScraped)
                                 .collect(Collectors.toList());
        var accessionedDate = dublinCore.getDcValues().stream()
                                  .filter(DcValue::isAccessionedDate)
                                  .map(DcValue::scrapeValueAndSetToScraped)
                                  .collect(Collectors.toList());

        var publishedDate = new PublishedDate();
        if (!availableDates.isEmpty()) {
            publishedDate.setBrageDates(availableDates);
            publishedDate.setNvaDate(availableDates.get(0));
            return publishedDate;
        }
        if (!accessionedDate.isEmpty()) {
            publishedDate.setBrageDates(accessionedDate);
            publishedDate.setNvaDate(accessionedDate.get(0));
            return publishedDate;
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
            return null;
        }
        return issnList.get(0);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static String handleIsbnList(List<String> isbnList, BrageLocation brageLocation) {
        if (isbnList.size() > 1) {
            logger.warn(new WarningDetails(Warning.MULTIPLE_ISBN_VALUES_WARNING, isbnList)
                        + StringUtils.SPACE
                        + brageLocation.getOriginInformation());
        }
        if (isbnList.isEmpty()) {
            return null;
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
        if (PUBLISHED_VERSION_STRING.equals(version.scrapeValueAndSetToScraped())) {
            return Optional.of(true);
        }
        if (ACCEPTED_VERSION_STRING.equals(version.scrapeValueAndSetToScraped())) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
    }

    private static Type mapOriginTypeToNvaType(List<String> types) {
        return new Type(types, TypeMapper.convertBrageTypeToNvaType(types));
    }
}

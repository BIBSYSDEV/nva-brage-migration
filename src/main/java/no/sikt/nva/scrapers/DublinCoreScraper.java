package no.sikt.nva.scrapers;

import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_VERSIONS;
import static no.sikt.nva.validators.DublinCoreValidator.ACCEPTED_VERSION_STRING;
import static no.sikt.nva.validators.DublinCoreValidator.DEHYPHENATION_REGEX;
import static no.sikt.nva.validators.DublinCoreValidator.PUBLISHED_VERSION_STRING;
import static no.sikt.nva.validators.DublinCoreValidator.getDublinCoreWarnings;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Journal;
import no.sikt.nva.brage.migration.common.model.record.Publication;
import no.sikt.nva.brage.migration.common.model.record.PublicationContext;
import no.sikt.nva.brage.migration.common.model.record.PublishedDate;
import no.sikt.nva.brage.migration.common.model.record.Publisher;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthority;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Series;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.channelregister.ChannelRegister;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.validators.DoiValidator;
import no.sikt.nva.validators.DublinCoreValidator;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public final class DublinCoreScraper {

    public static final String SUBMITTED_VERSION = "submittedVersion";
    public static final String FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE = "Field was not scraped\n";
    public static final String NEW_LINE_DELIMITER = "\n";
    public static final ChannelRegister channelRegister = ChannelRegister.getRegister();
    public static final String SCRAPING_HAS_FAILED = "Scraping has failed: ";
    public static final String CRISTIN_POST = "[CRISTIN_POST]";
    public static final String DELIMITER = "-";
    public static final String REGEX_ISSN = "[^0-9-xX]";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreScraper.class);
    private static Map<String, Contributor> contributors;
    private final boolean enableOnlineValidation;
    private final boolean shouldLookUpInChannelRegister;

    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    public DublinCoreScraper(boolean enableOnlineValidation, boolean shouldLookUpInChannelRegister,
                             Map<String, Contributor> contributors) {
        this.enableOnlineValidation = enableOnlineValidation;
        this.shouldLookUpInChannelRegister = shouldLookUpInChannelRegister;
        DublinCoreScraper.contributors = contributors;
    }

    public static List<String> extractIssn(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isIssnValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(issn -> issn.replaceAll(REGEX_ISSN, StringUtils.EMPTY_STRING).toUpperCase(Locale.ROOT))
                   .map(DublinCoreScraper::addDelimiter)
                   .collect(Collectors.toList());
    }

    public static String getIgnoredFieldNames() {
        List<DcValue> dcValues = new ArrayList<>();
        dcValues.add(new DcValue(Element.DATE, Qualifier.COPYRIGHT, null));
        dcValues.add(new DcValue(Element.RELATION, Qualifier.PROJECT, null));
        dcValues.add(new DcValue(Element.DESCRIPTION, Qualifier.PROVENANCE, null));
        dcValues.add(new DcValue(Element.DESCRIPTION, Qualifier.SPONSORSHIP, null));
        dcValues.add(new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, null));
        dcValues.add(new DcValue(Element.IDENTIFIER, Qualifier.CITATION, null));
        dcValues.add(new DcValue(Element.SUBJECT, Qualifier.NORWEGIAN_SCIENCE_INDEX, null));
        dcValues.add(new DcValue(Element.DATE, Qualifier.CREATED, null));
        dcValues.add(new DcValue(Element.DATE, Qualifier.UPDATED, null));
        dcValues.add(new DcValue(Element.DATE, Qualifier.NONE, null));
        dcValues.add(new DcValue(Element.RELATION, Qualifier.URI, null));
        dcValues.add(new DcValue(Element.DATE, Qualifier.EMBARGO, null));
        dcValues.add(new DcValue(Element.SOURCE, Qualifier.NONE, null));
        dcValues.add(new DcValue(Element.CREATOR, Qualifier.NONE, null));
        dcValues.add(new DcValue(Element.FORMAT, Qualifier.EXTENT, null));
        dcValues.add(new DcValue(Element.FORMAT, Qualifier.MIME_TYPE, null));
        dcValues.add(new DcValue(Element.IDENTIFIER, Qualifier.NONE, null));
        dcValues.add(new DcValue(Element.IDENTIFIER, Qualifier.OTHER, null));
        return dcValues.stream().map(DcValue::toXmlString).collect(Collectors.joining(NEW_LINE_DELIMITER));
    }

    public static List<String> extractIsbn(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isIsbnAndNotEmptyValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(isbn -> isbn.replaceAll(DEHYPHENATION_REGEX, StringUtils.EMPTY_STRING))
                   .map(isbn -> isbn.replaceAll("[^0-9]", ""))
                   .collect(Collectors.toList());
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
        var journals = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isJournal)
                           .map(DcValue::scrapeValueAndSetToScraped)
                           .collect(Collectors.toList());
        return journals.isEmpty() ? null : journals.get(0);
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

    public static Publication extractPublication(DublinCore dublinCore) {
        var publication = new Publication();
        publication.setIssnList(extractIssn(dublinCore));
        publication.setIsbnList(extractIsbn(dublinCore));
        publication.setJournal(extractJournal(dublinCore));
        publication.setPartOfSeries(extractPartOfSeries(dublinCore));
        return publication;
    }

    public static boolean isSingleton(List<String> versions) {
        return versions.size() == 1;
    }

    public static List<String> translateTypesInNorwegian(List<String> types) {
        return types.stream()
                   .map(TypeTranslator::translateToEnglish)
                   .collect(Collectors.toList());
    }

    public Record validateAndParseDublinCore(DublinCore dublinCore, BrageLocation brageLocation) {
        try {
            var errors = DublinCoreValidator.getDublinCoreErrors(dublinCore);
            if (lookUpInChannelRegisterIsEnabled()) {
                ChannelRegister.getChannelRegisterErrors(dublinCore, brageLocation).ifPresent(errors::add);
            }
            if (onlineValidationIsEnabled()) {
                DoiValidator.getDoiErrorDetailsOnline(dublinCore).ifPresent(errors::addAll);
            }
            var warnings = getDublinCoreWarnings(dublinCore);
            var record = createRecordFromDublinCoreAndBrageLocation(dublinCore, brageLocation);
            record.setErrors(errors);
            record.setWarnings(warnings);
            logUnscrapedValues(dublinCore, brageLocation);
            var isCristinPost = isInCristin(dublinCore);
            logWarningsIfNotEmpty(brageLocation, warnings, isCristinPost);
            logErrorsIfNotEmpty(brageLocation, errors, isCristinPost);
            return record;
        } catch (Exception e) {
            throw new DublinCoreException(SCRAPING_HAS_FAILED + e);
        }
    }

    public boolean onlineValidationIsEnabled() {
        return enableOnlineValidation;
    }

    public boolean lookUpInChannelRegisterIsEnabled() {
        return shouldLookUpInChannelRegister;
    }

    private static String addDelimiter(String issn) {
        return nonNull(issn) && issn.length() >= 8 && !issn.contains(DELIMITER)
                   ? issn.substring(0, 4) + DELIMITER + issn.substring(4)
                   : issn;
    }

    private static void logWarningsIfNotEmpty(BrageLocation brageLocation, List<WarningDetails> warnings,
                                              boolean isCristinPost) {
        if (!warnings.isEmpty()) {
            if (isCristinPost) {
                logger.warn(CRISTIN_POST + warnings + StringUtils.SPACE + brageLocation.getOriginInformation());
            } else {
                logger.warn(warnings + StringUtils.SPACE + brageLocation.getOriginInformation());
            }
        }
    }

    private static void logErrorsIfNotEmpty(BrageLocation brageLocation, List<ErrorDetails> error,
                                            boolean isCristinPost) {
        if (!error.isEmpty()) {
            if (isCristinPost) {
                logger.error(CRISTIN_POST + error + StringUtils.SPACE + brageLocation.getOriginInformation());
            } else {
                logger.error(error + StringUtils.SPACE + brageLocation.getOriginInformation());
            }
        }
    }

    private static Record createRecordFromDublinCoreAndBrageLocation(DublinCore dublinCore,
                                                                     BrageLocation brageLocation) {
        var record = new Record();
        record.setId(brageLocation.getHandle());
        record.setType(mapOriginTypeToNvaType(extractType(dublinCore)));
        record.setRightsHolder(extractRightsholder(dublinCore));
        record.setPublisherAuthority(extractVersion(dublinCore, brageLocation));
        record.setDoi(extractDoi(dublinCore));
        record.setEntityDescription(EntityDescriptionExtractor.extractEntityDescription(dublinCore, contributors));
        record.setSpatialCoverage(extractSpatialCoverage(dublinCore));
        record.setPublication(createPublicationWithIdentifier(dublinCore, brageLocation, record));
        record.setPublishedDate(extractAvailableDate(dublinCore));
        record.setCristinId(extractCristinId(dublinCore));
        record.setPartOf(extractPartOf(dublinCore));
        record.setPart(extractHasPart(dublinCore));
        return record;
    }

    private static Publication createPublicationWithIdentifier(DublinCore dublinCore, BrageLocation brageLocation,
                                                               Record record) {
        var publication = extractPublication(dublinCore);
        publication.setPublicationContext(new PublicationContext());
        publication.getPublicationContext().setBragePublisher(extractPublisher(dublinCore));
        record.setPublication(publication);
        if (nonNull(record.getType().getNva())) {
            searchForSeriesAndJournalsInChannelRegister(brageLocation, record);
            searchForPublisherInChannelRegister(record);
        }
        return publication;
    }

    private static void searchForSeriesAndJournalsInChannelRegister(BrageLocation brageLocation, Record record) {
        if (isSearchableInJournals(record.getType().getNva())) {
            setIdFromJournals(brageLocation, record);
        }
    }

    private static void setIdFromJournals(BrageLocation brageLocation, Record record) {
        if (isReport(record)) {
            setChannelRegisterIdentifierForReport(brageLocation, record);
        }
        if (isJournal(record)) {
            setChannelRegisterIdentifierForJournal(brageLocation, record);
        }
    }

    private static void setChannelRegisterIdentifierForJournal(BrageLocation brageLocation, Record record) {
        record.getPublication().getPublicationContext().setJournal(
            new Journal(extractChannelRegisterIdentifierForSeriesJournal(brageLocation, record.getPublication())));
    }

    private static void setChannelRegisterIdentifierForReport(BrageLocation brageLocation, Record record) {
        record.getPublication().getPublicationContext().setSeries(
            new Series(extractChannelRegisterIdentifierForSeriesJournal(brageLocation, record.getPublication())));
    }

    private static String extractChannelRegisterIdentifierForSeriesJournal(BrageLocation brageLocation,
                                                                           Publication publication) {
        return publication.getIssnList().stream()
                   .map(issn -> channelRegister.lookUpInJournal(publication, brageLocation))
                   .filter(Objects::nonNull)
                   .findAny()
                   .orElse(null);
    }

    private static boolean isJournal(Record record) {
        return NvaType.JOURNAL_ARTICLE.getValue().equals(record.getType().getNva())
               || NvaType.SCIENTIFIC_ARTICLE.getValue().equals(record.getType().getNva());
    }

    private static boolean isReport(Record record) {
        return NvaType.REPORT.getValue().equals(record.getType().getNva());
    }

    private static void searchForPublisherInChannelRegister(Record record) {
        if (isSearchableInPublishers(record)) {
            var publisherId = channelRegister.lookUpInChannelRegisterForPublisher(record);
            if (nonNull(publisherId)) {
                record.getPublication().getPublicationContext().setPublisher(new Publisher(publisherId));
            }
        }
    }

    private static boolean isSearchableInJournals(String nvaType) {
        return nvaType.equals(NvaType.JOURNAL_ARTICLE.getValue())
               || nvaType.equals(NvaType.SCIENTIFIC_ARTICLE.getValue())
               || nvaType.equals(NvaType.REPORT.getValue());
    }

    private static boolean isSearchableInPublishers(Record record) {
        return nonNull(record.getPublication().getPublicationContext().getBragePublisher())
               && record.getType().getNva().equals(NvaType.REPORT.getValue())
               || record.getType().getNva().equals(NvaType.BOOK.getValue())
               || record.getType().getNva().equals(NvaType.SCIENTIFIC_MONOGRAPH.getValue());
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

    private static void logUnscrapedValues(DublinCore dublinCore, BrageLocation brageLocation) {
        List<String> unscrapedDcValues = findUnscrapedFields(dublinCore);
        logUnscrapedFields(brageLocation, unscrapedDcValues);
    }

    private static void logUnscrapedFields(BrageLocation brageLocation, List<String> unscrapedDcValues) {
        if (!unscrapedDcValues.isEmpty()) {
            logger.info(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE
                        + String.join(NEW_LINE_DELIMITER, unscrapedDcValues)
                        + NEW_LINE_DELIMITER
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
               || dcValue.isNoneDate()
               || dcValue.isLocalCode()
               || dcValue.isEmbargo()
               || dcValue.isSourceNone()
               || dcValue.isCreatorNone()
               || dcValue.isFormatExtent()
               || dcValue.isFormatMimeType()
               || dcValue.isIdentifierNone()
               || dcValue.isOtherIdentifier();
    }

    private static String extractCristinId(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isCristinDcValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(DublinCoreScraper::removeAllUnnecessaryStrings)
                   .filter(DublinCoreScraper::isNumeric)
                   .findAny()
                   .orElse(null);
    }

    private static String removeAllUnnecessaryStrings(String s) {
        return Arrays.stream(s.split(StringUtils.SPACE))
                   .filter(Objects::nonNull)
                   .filter(DublinCoreScraper::isNumeric)
                   .findFirst().orElse(null);
    }

    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String extractPartOfSeries(DublinCore dublinCore) {
        var partOfSeriesValues = dublinCore.getDcValues()
                                     .stream()
                                     .filter(DcValue::isPartOfSeries)
                                     .map(DcValue::scrapeValueAndSetToScraped)
                                     .collect(Collectors.toList());

        return partOfSeriesValues.isEmpty()
                   ? new DcValue().scrapeValueAndSetToScraped()
                   : partOfSeriesValues.get(0);
    }

    private static String extractPartOf(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPartOf)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static List<String> extractHasPart(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isHasPart)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
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

    private static PublisherAuthority extractVersion(DublinCore dublinCore, BrageLocation brageLocation) {
        var version = dublinCore.getDcValues().stream()
                          .filter(DcValue::isOneOfTwoPossibleVersions)
                          .map(DcValue::scrapeValueAndSetToScraped)
                          .collect(Collectors.toList());
        return mapToNvaVersion(version, brageLocation);
    }

    private static PublisherAuthority mapToNvaVersion(List<String> versions, BrageLocation brageLocation) {
        var uniqueVersions = new ArrayList<>(new HashSet<>(versions));

        if (isSingleton(uniqueVersions)) {
            return mapSingleVersion(uniqueVersions);
        }
        if (containsMultipleValues(uniqueVersions)) {
            return mapMultipleVersions(versions, brageLocation);
        }
        return new PublisherAuthority(versions, null);
    }

    private static boolean containsMultipleValues(List<String> versions) {
        return versions.size() >= 2;
    }

    private static PublisherAuthority mapMultipleVersions(List<String> versions, BrageLocation brageLocation) {
        if (versions.contains(PUBLISHED_VERSION_STRING)) {
            return new PublisherAuthority(Collections.singletonList(PUBLISHED_VERSION_STRING), true);
        }
        if (versions.contains(ACCEPTED_VERSION_STRING)) {
            return new PublisherAuthority(Collections.singletonList(ACCEPTED_VERSION_STRING), false);
        }
        if (versions.contains(SUBMITTED_VERSION)) {
            return new PublisherAuthority(Collections.singletonList(SUBMITTED_VERSION), false);
        } else {
            logger.error(new ErrorDetails(MULTIPLE_VERSIONS, versions)
                         + StringUtils.SPACE
                         + brageLocation.getOriginInformation());
            return new PublisherAuthority(versions, null);
        }
    }

    private static PublisherAuthority mapSingleVersion(List<String> versions) {
        var version = versions.get(0);
        if (PUBLISHED_VERSION_STRING.equals(version)) {
            return new PublisherAuthority(Collections.singletonList(version), true);
        } else if (ACCEPTED_VERSION_STRING.equals(version)) {
            return new PublisherAuthority(Collections.singletonList(version), false);
        } else {
            return new PublisherAuthority(Collections.singletonList(version), null);
        }
    }

    private static Type mapOriginTypeToNvaType(List<String> types) {
        var uniqueTypes = new ArrayList<>(new HashSet<>(translateTypesInNorwegian(types)));
        return new Type(types, TypeMapper.convertBrageTypeToNvaType(uniqueTypes));
    }

    private static boolean isInCristin(DublinCore dublinCore) {
        var cristinId = extractCristinId(dublinCore);
        return nonNull(cristinId) && !cristinId.isEmpty();
    }
}

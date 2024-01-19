package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_DC_VERSION_VALUES;
import static no.sikt.nva.channelregister.ChannelRegister.SEARCHABLE_TYPES_IN_JOURNALS;
import static no.sikt.nva.channelregister.ChannelRegister.SEARCHABLE_TYPES_IN_PUBLISHERS;
import static no.sikt.nva.scrapers.CustomerMapper.FFI;
import static no.sikt.nva.validators.DublinCoreValidator.ACCEPTED_VERSION_STRING;
import static no.sikt.nva.validators.DublinCoreValidator.DEHYPHENATION_REGEX;
import static no.sikt.nva.validators.DublinCoreValidator.PUBLISHED_VERSION_STRING;
import static no.sikt.nva.validators.DublinCoreValidator.getDublinCoreErrors;
import static no.sikt.nva.validators.DublinCoreValidator.getDublinCoreWarnings;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import nva.commons.core.SingletonCollector;
import nva.commons.core.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public class DublinCoreScraper {

    public static final String SUBMITTED_VERSION = "submittedVersion";
    public static final String FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE = "This field will not be migrated\n";
    public static final String NEW_LINE_DELIMITER = "\n";
    public static final String SCRAPING_HAS_FAILED = "Scraping has failed: ";
    public static final String CRISTIN_POST = "[CRISTIN_POST]";
    public static final String DELIMITER = "-";
    public static final String REGEX_ISSN = "[^0-9-xX]";
    public static final String COMA_ISSN_DELIMITER = ",";
    public static final String EMPTY_SPACES_LINEBREAKS_REGEX = "(\n)|(\b)|(\u200b)|(\t)|(\")";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreScraper.class);
    private static Map<String, Contributor> contributors;
    private final boolean enableOnlineValidation;
    private final boolean shouldLookUpInChannelRegister;
    public ChannelRegister channelRegister;

    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    public DublinCoreScraper(boolean enableOnlineValidation, boolean shouldLookUpInChannelRegister,
                             Map<String, Contributor> contributors) {
        this.enableOnlineValidation = enableOnlineValidation;
        this.shouldLookUpInChannelRegister = shouldLookUpInChannelRegister;
        DublinCoreScraper.contributors = contributors;
        if (shouldLookUpInChannelRegister) {
            this.channelRegister = ChannelRegister.getRegister();
        }
    }


    public static Set<String> extractIssn(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isIssnValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .filter(Objects::nonNull)
                   .filter(value -> !value.isEmpty())
                   .map(DublinCoreScraper::attemptToRepairIssn)
                   .map(DublinCoreScraper::attemptToRepairIssn)
                   .collect(Collectors.toSet());
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

    public static Set<String> extractIsbn(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isIsbnAndNotEmptyValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .distinct()
                   .map(DublinCoreScraper::attemptToRepairIsbnAndIsmn)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());
    }

    public static String attemptToRepairIsbnAndIsmn(String value) {
        return Optional.ofNullable(value)
                   .map(isbn -> isbn.replaceAll(DEHYPHENATION_REGEX, StringUtils.EMPTY_STRING))
                   .map(isbn -> isbn.replaceAll("[^0-9]", ""))
                   .map(isbn -> isbn.replaceAll(StringUtils.WHITESPACES, StringUtils.EMPTY_STRING))
                   .orElse(null);
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

    public static Set<String> extractType(DublinCore dublinCore, String customer) {
        var types = extractType(dublinCore);
        return types.isEmpty() && customerHasAgreedToImportTypeLessPostsAsReport(customer)
                   ? Set.of(NvaType.REPORT.getValue())
                   : types;
    }

    private static boolean customerHasAgreedToImportTypeLessPostsAsReport(String customer) {
        return nonNull(customer) && FFI.equals(customer);
    }

    @NotNull
    private static Set<String> extractType(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isType)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(value -> value.replaceAll(EMPTY_SPACES_LINEBREAKS_REGEX, StringUtils.EMPTY_STRING))
                   .collect(Collectors.toSet());
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
        publication.isIssmList(extractIsmn(dublinCore));
        publication.setPartOfSeries(extractPartOfSeries(dublinCore));
        return publication;
    }

    public static Set<String> extractIsmn(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isIsmnAndNotEmptyValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .distinct()
                   .map(DublinCoreScraper::attemptToRepairIsbnAndIsmn)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());

    }

    public static boolean isSingleton(Set<String> versions) {
        return versions.size() == 1;
    }

    public static Set<String> translateTypesInNorwegian(Set<String> types) {
        return types.stream().map(TypeTranslator::translateToEnglish).collect(Collectors.toSet());
    }

    public static String removeWrongPlacedDelimiters(String value) {
        if (isNull(value) || value.isEmpty()) {
            return value;
        }
        if (firstLetterIsDelimiter(value)) {
            return value.substring(1);
        }
        if (lastLetterIsDelimiter(value)) {
            return value.substring(0, value.length() - 1);
        }
        if (firstLetterIsDelimiter(value) && lastLetterIsDelimiter(value)) {
            return value.substring(1).substring(0, value.length() - 1);
        }
        return value;
    }

    public static String addDelimiter(String issn) {
        return nonNull(issn) && issn.length() >= 8 && !issn.contains(DELIMITER) ? issn.substring(0, 4)
                                                                                  + DELIMITER
                                                                                  + issn.substring(4) : issn;
    }

    public static String extractEmbargo(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isEmbargoEndDate)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toSet())
                   .stream()
                   .collect(SingletonCollector.collectOrElse(null));
    }

    public static String extractCristinId(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isCristinDcValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(DublinCoreScraper::removeAllUnnecessaryStrings)
                   .filter(Objects::nonNull)
                   .filter(DublinCoreScraper::isNumeric)
                   .findAny()
                   .orElse(null);
    }

    public static Type mapOriginTypeToNvaType(Set<String> types, DublinCore dublinCore) {
        var uniqueTypes = translateTypesInNorwegian(types);
        var type = new Type(types, TypeMapper.convertBrageTypeToNvaType(uniqueTypes));
        if (isNull(type.getNva()) && nonNull(extractCristinId(dublinCore))) {
            return new Type(types, NvaType.CRISTIN_RECORD.getValue());
        } else {
            return type;
        }
    }

    public static String extractSubjectCode(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isSubjectCode)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .findFirst()
                   .orElse(null);
    }

    public Record validateAndParseDublinCore(DublinCore dublinCore,
                                             BrageLocation brageLocation,
                                             String customer) {
        try {
            var errors = getDublinCoreErrors(dublinCore, customer);
            if (lookUpInChannelRegisterIsEnabled()) {
                channelRegister.getChannelRegisterErrors(dublinCore,
                                                         brageLocation,
                                                         customer)
                    .ifPresent(errors::add);
            }
            if (onlineValidationIsEnabled()) {
                DoiValidator.getDoiErrorDetailsOnline(dublinCore).ifPresent(errors::addAll);
            }
            var warnings = getDublinCoreWarnings(dublinCore, customer);
            var record = createRecordFromDublinCoreAndBrageLocation(dublinCore,
                                                                    brageLocation,
                                                                    shouldLookUpInChannelRegister,
                                                                    customer);
            record.setErrors(errors);
            record.setWarnings(warnings);
            logUnscrapedValues(dublinCore, brageLocation);
            var isCristinPost = isInCristin(dublinCore);
            logWarningsIfNotEmpty(brageLocation, warnings, isCristinPost);
            logErrorsIfNotEmpty(brageLocation, errors, isCristinPost);
            return record;
        } catch (Exception e) {
            throw new DublinCoreException(SCRAPING_HAS_FAILED + e.getMessage());
        }
    }

    public boolean onlineValidationIsEnabled() {
        return enableOnlineValidation;
    }

    public boolean lookUpInChannelRegisterIsEnabled() {
        return shouldLookUpInChannelRegister;
    }

    private static String attemptToRepairIssn(String issn) {
        return Optional.ofNullable(issn)
                   .map(DublinCoreScraper::extractFirstIssnWhenMultipleValues)
                   .map(DublinCoreScraper::removeWrongPlacedDelimiters)
                   .map(DublinCoreScraper::addDelimiter)
                   .map(DublinCoreScraper::replaceLowerCaseCheckDigit)
                   .map(Mapper::mapToHardcodedValue)
                   .orElse(null);
    }

    private static String extractFirstIssnWhenMultipleValues(String string) {
        return string.contains(COMA_ISSN_DELIMITER)
                   ? string.split(COMA_ISSN_DELIMITER)[0]
                   : string;
    }

    private static String replaceLowerCaseCheckDigit(String value) {
        return value.replaceAll(REGEX_ISSN, StringUtils.EMPTY_STRING).toUpperCase(Locale.ROOT);
    }

    private static boolean lastLetterIsDelimiter(String value) {
        return DELIMITER.equals(String.valueOf(value.toCharArray()[value.length() - 1]));
    }

    private static void logWarningsIfNotEmpty(BrageLocation brageLocation, Set<WarningDetails> warnings,
                                              boolean isCristinPost) {
        if (!warnings.isEmpty()) {
            if (isCristinPost) {
                logger.warn(CRISTIN_POST + warnings + StringUtils.SPACE + brageLocation.getOriginInformation());
            } else {
                logger.warn(warnings + StringUtils.SPACE + brageLocation.getOriginInformation());
            }
        }
    }

    private static void logErrorsIfNotEmpty(BrageLocation brageLocation, Set<ErrorDetails> error,
                                            boolean isCristinPost) {
        if (!error.isEmpty()) {
            if (isCristinPost) {
                logger.error(CRISTIN_POST + error + StringUtils.SPACE + brageLocation.getOriginInformation());
            } else {
                logger.error(error + StringUtils.SPACE + brageLocation.getOriginInformation());
            }
        }
    }

    private static Set<URI> extractSubjects(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isLocalCode)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(DublinCoreScraper::toUri)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toSet());
    }

    private static URI toUri(String value) {
        return attempt(() -> new URL(value).toURI()).orElse(failure -> null);
    }

    private static boolean brageJournalChannelPresent(Publication publication) {
        return issnPresent(publication) || journalNameIsPresent(publication);
    }

    private static boolean journalNameIsPresent(Publication publication) {
        return StringUtils.isNotBlank(publication.getJournal());
    }

    private static boolean issnPresent(Publication publication) {
        return !publication.getIssnSet().isEmpty();
    }

    private static boolean isJournal(Record record) {
        return NvaType.JOURNAL_ARTICLE.getValue().equals(record.getType().getNva())
               || NvaType.SCIENTIFIC_ARTICLE.getValue().equals(record.getType().getNva());
    }

    private static boolean isReport(Record record) {
        return NvaType.REPORT.getValue().equals(record.getType().getNva());
    }

    private static boolean isSearchableInJournals(Record record) {
        return SEARCHABLE_TYPES_IN_JOURNALS.stream()
                   .map(NvaType::getValue)
                   .collect(Collectors.toList())
                   .contains(record.getType().getNva());
    }

    private static boolean isSearchableInPublishers(Record record) {
        return SEARCHABLE_TYPES_IN_PUBLISHERS.stream()
                   .map(NvaType::getValue)
                   .collect(Collectors.toList())
                   .contains(record.getType().getNva());
    }

    private static URI extractDoi(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isDoi)
                   .findFirst()
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(DoiValidator::updateDoiStructureIfNeeded)
                   .map(DoiValidator::replaceCharactersIfNeeded)
                   .map(DoiValidator::attemptToReturnDoi)
                   .map(convertToUriAttempt())
                   .orElse(null);
    }

    private static URI extractLink(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isLink)
                   .findFirst()
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(DoiValidator::updateDoiStructureIfNeeded)
                   .map(DoiValidator::updateLinkStructureIfNeeded)
                   .map(DoiValidator::replaceCharactersIfNeeded)
                   .map(DoiValidator::attemptToReturnLink)
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
        return !dcValue.isScraped() && !fieldHasBeenScrapedFromOtherFiles(dcValue) && !shouldBeIgnored(dcValue);
    }

    private static boolean fieldHasBeenScrapedFromOtherFiles(DcValue dcValue) {
        return dcValue.isLicenseInformation() || dcValue.isHandle();
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
               || dcValue.isEmbargo()
               || dcValue.isSourceNone()
               || dcValue.isCreatorNone()
               || dcValue.isFormatExtent()
               || dcValue.isFormatMimeType()
               || dcValue.isIdentifierNone()
               || dcValue.isOtherIdentifier();
    }

    private static String removeAllUnnecessaryStrings(String s) {
        return Arrays.stream(s.split(StringUtils.SPACE))
                   .filter(Objects::nonNull)
                   .filter(DublinCoreScraper::isNumeric)
                   .findFirst()
                   .orElse(null);
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

        return partOfSeriesValues.isEmpty() ? new DcValue().scrapeValueAndSetToScraped() : partOfSeriesValues.get(0);
    }

    private static String extractPartOf(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPartOf)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static Set<String> extractHasPart(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isHasPart)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toSet());
    }

    private static String extractRightsholder(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isRightsholder)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    private static List<String> extractSpatialCoverage(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isSpatialCoverage)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private static PublishedDate extractAvailableDate(DublinCore dublinCore) {
        var availableDates = dublinCore.getDcValues()
                                 .stream()
                                 .filter(DcValue::isAvailableDate)
                                 .map(DcValue::scrapeValueAndSetToScraped)
                                 .collect(Collectors.toSet());
        var accessionedDate = dublinCore.getDcValues()
                                  .stream()
                                  .filter(DcValue::isAccessionedDate)
                                  .map(DcValue::scrapeValueAndSetToScraped)
                                  .collect(Collectors.toSet());

        var publishedDate = new PublishedDate();
        if (!availableDates.isEmpty()) {
            publishedDate.setBrageDates(availableDates);
            publishedDate.setNvaDate(availableDates.iterator().next());
            return publishedDate;
        }
        if (!accessionedDate.isEmpty()) {
            publishedDate.setBrageDates(accessionedDate);
            publishedDate.setNvaDate(accessionedDate.iterator().next());
            return publishedDate;
        } else {
            return null;
        }
    }

    private static PublisherAuthority extractVersion(DublinCore dublinCore, BrageLocation brageLocation) {
        var version = dublinCore.getDcValues()
                          .stream()
                          .filter(DcValue::isOneOfTwoPossibleVersions)
                          .map(DcValue::scrapeValueAndSetToScraped)
                          .collect(Collectors.toSet());
        return mapToNvaVersion(version, brageLocation);
    }

    private static PublisherAuthority mapToNvaVersion(Set<String> versions, BrageLocation brageLocation) {
        var uniqueVersions = new HashSet<>(versions);
        if (isSingleton(uniqueVersions)) {
            return mapSingleVersion(uniqueVersions);
        }
        if (containsMultipleValues(uniqueVersions)) {
            return mapMultipleVersions(versions, brageLocation);
        }
        return new PublisherAuthority(versions, null);
    }

    private static boolean containsMultipleValues(Set<String> versions) {
        return versions.size() >= 2;
    }

    private static PublisherAuthority mapMultipleVersions(Set<String> versions, BrageLocation brageLocation) {
        if (versions.contains(PUBLISHED_VERSION_STRING)) {
            return new PublisherAuthority(Collections.singleton(PUBLISHED_VERSION_STRING), true);
        }
        if (versions.contains(ACCEPTED_VERSION_STRING)) {
            return new PublisherAuthority(Collections.singleton(ACCEPTED_VERSION_STRING), false);
        }
        if (versions.contains(SUBMITTED_VERSION)) {
            return new PublisherAuthority(Collections.singleton(SUBMITTED_VERSION), false);
        } else {
            logger.error(new ErrorDetails(MULTIPLE_DC_VERSION_VALUES, versions)
                         + StringUtils.SPACE
                         + brageLocation.getOriginInformation());
            return new PublisherAuthority(versions, null);
        }
    }

    private static PublisherAuthority mapSingleVersion(Set<String> versions) {
        var version = versions.iterator().next();
        if (PUBLISHED_VERSION_STRING.equals(version)) {
            return new PublisherAuthority(Collections.singleton(version), true);
        } else if (ACCEPTED_VERSION_STRING.equals(version)) {
            return new PublisherAuthority(Collections.singleton(version), false);
        } else {
            return new PublisherAuthority(Collections.singleton(version), null);
        }
    }

    public static boolean isInCristin(DublinCore dublinCore) {
        var cristinId = extractCristinId(dublinCore);
        return nonNull(cristinId) && !cristinId.isEmpty();
    }

    private static boolean firstLetterIsDelimiter(String value) {
        return DELIMITER.equals(String.valueOf(value.toCharArray()[0]));
    }

    private Record createRecordFromDublinCoreAndBrageLocation(DublinCore dublinCore,
                                                              BrageLocation brageLocation,
                                                              boolean shouldLookUpInChannelRegister,
                                                              String customer) {
        var record = new Record();
        record.setId(brageLocation.getHandle());
        record.setType(mapOriginTypeToNvaType(extractType(dublinCore, customer), dublinCore));
        record.setRightsHolder(extractRightsholder(dublinCore));
        record.setPublisherAuthority(extractVersion(dublinCore, brageLocation));
        record.setDoi(extractDoi(dublinCore));
        record.setLink(extractLink(dublinCore));
        record.setEntityDescription(EntityDescriptionExtractor.extractEntityDescription(dublinCore, contributors, customer));
        record.setSpatialCoverage(extractSpatialCoverage(dublinCore));
        record.setPublication(createPublicationWithIdentifier(dublinCore,
                                                              brageLocation,
                                                              record,
                                                              shouldLookUpInChannelRegister,
                                                              customer));
        record.setPublishedDate(extractAvailableDate(dublinCore));
        record.setCristinId(extractCristinId(dublinCore));
        record.setPartOf(extractPartOf(dublinCore));
        record.setPart(extractHasPart(dublinCore));
        record.setSubjects(extractSubjects(dublinCore));
        return record;
    }

    private Publication createPublicationWithIdentifier(DublinCore dublinCore,
                                                        BrageLocation brageLocation,
                                                        Record record,
                                                        boolean shouldLookUpInChannelRegister,
                                                        String customer) {
        var publication = extractPublication(dublinCore);
        publication.setPublicationContext(new PublicationContext());
        publication.getPublicationContext().setBragePublisher(extractPublisher(dublinCore));
        record.setPublication(publication);
        if (nonNull(record.getType().getNva()) && shouldLookUpInChannelRegister) {
            searchForSeriesAndJournalsInChannelRegister(brageLocation, record);
            searchForPublisherInChannelRegister(record, customer);
        }
        return publication;
    }

    private void searchForSeriesAndJournalsInChannelRegister(BrageLocation brageLocation, Record record) {
        if (isSearchableInJournals(record)) {
            setIdFromJournals(brageLocation, record);
        }
    }

    private void setIdFromJournals(BrageLocation brageLocation, Record record) {
        if (isReport(record)) {
            setChannelRegisterIdentifierForReport(brageLocation, record);
        }
        if (isJournal(record)) {
            setChannelRegisterIdentifierForJournal(brageLocation, record);
        }
    }

    private void setChannelRegisterIdentifierForJournal(BrageLocation brageLocation, Record record) {
        record.getPublication()
            .getPublicationContext()
            .setJournal(
                new Journal(extractChannelRegisterIdentifierForSeriesJournal(brageLocation, record.getPublication())));
    }

    private void setChannelRegisterIdentifierForReport(BrageLocation brageLocation, Record record) {
        record.getPublication()
            .getPublicationContext()
            .setSeries(
                new Series(extractChannelRegisterIdentifierForSeriesJournal(brageLocation, record.getPublication())));
    }

    private String extractChannelRegisterIdentifierForSeriesJournal(BrageLocation brageLocation,
                                                                    Publication publication) {
        return brageJournalChannelPresent(publication)
                   ? channelRegister.lookUpInJournal(publication, brageLocation)
                   : null;
    }

    private void searchForPublisherInChannelRegister(Record record, String customer) {
        if (isSearchableInPublishers(record)) {
            var publisherId = channelRegister.lookUpInChannelRegisterForPublisher(record, customer);
            if (nonNull(publisherId)) {
                record.getPublication().getPublicationContext().setPublisher(new Publisher(publisherId));
            }
        }
    }
}

package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.BrageType.OTHER_TYPE_OF_REPORT;
import static no.sikt.nva.brage.migration.common.model.BrageType.REPORT;
import static no.sikt.nva.brage.migration.common.model.BrageType.RESEARCH_REPORT;
import static no.sikt.nva.brage.migration.common.model.record.PublisherVersion.ACCEPTED_VERSION;
import static no.sikt.nva.brage.migration.common.model.record.PublisherVersion.PUBLISHED_VERSION;
import static no.sikt.nva.scrapers.CustomerMapper.FFI;
import static no.sikt.nva.scrapers.CustomerMapper.UIO;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.AUTHOR;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.OTHER_CONTRIBUTOR;
import static no.sikt.nva.validators.DublinCoreValidator.DEHYPHENATION_REGEX;
import static no.sikt.nva.validators.DublinCoreValidator.getDublinCoreErrors;
import static no.sikt.nva.validators.DublinCoreValidator.getDublinCoreWarnings;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import no.sikt.nva.brage.migration.common.model.record.Affiliation;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.FundingSources;
import no.sikt.nva.brage.migration.common.model.record.Journal;
import no.sikt.nva.brage.migration.common.model.record.PartOfSeries;
import no.sikt.nva.brage.migration.common.model.record.Project;
import no.sikt.nva.brage.migration.common.model.record.Publication;
import no.sikt.nva.brage.migration.common.model.record.PublicationContext;
import no.sikt.nva.brage.migration.common.model.record.PublishedDate;
import no.sikt.nva.brage.migration.common.model.record.Publisher;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthority;
import no.sikt.nva.brage.migration.common.model.record.PublisherVersion;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Series;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.channelregister.ChannelRegister;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.Embargo;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.validators.DoiValidator;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.StringUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public class DublinCoreScraper {

    public static final String FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE = "This field will not be migrated\n";
    public static final String NEW_LINE_DELIMITER = "\n";
    public static final String SCRAPING_HAS_FAILED = "Scraping has failed: ";
    public static final String CRISTIN_POST = "[CRISTIN_POST]";
    public static final String DELIMITER = "-";
    public static final String REGEX_ISSN = "[^0-9-xX]";
    public static final String COMA_ISSN_DELIMITER = ",";
    public static final String EMPTY_SPACES_LINEBREAKS_REGEX = "(\n)|(\b)|(\u200b)|(\t)|(\")";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreScraper.class);
    public static final String DOT = "\\.";
    public static final String CONTAINS_NUBMER_PATTERN = ".*\\d+.*";
    public static final String NEW_ISMN_FIRST_ELEMENT = "9790";
    public static final String ORCID_HOST = "https://orcid.org/";
    public static final String ORCID_HOST_REGEX = "https?://(www\\.)?orcid\\.org/";
    public static final String OLD_ISMN_FIRST_ELEMENT = "m";
    public static final int MAX_SUBJECT_CODE_LENGTH = 15;
    private static Map<String, Contributor> contributors;
    private final boolean enableOnlineValidation;
    private final boolean shouldLookUpInChannelRegister;
    private ChannelRegister channelRegister;
    private final FundingSources fundingSources;

    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    public DublinCoreScraper(boolean enableOnlineValidation, boolean shouldLookUpInChannelRegister,
                             Map<String, Contributor> contributors) {
        this.enableOnlineValidation = enableOnlineValidation;
        this.shouldLookUpInChannelRegister = shouldLookUpInChannelRegister;
        DublinCoreScraper.contributors = contributors;
        this.fundingSources = readFundingSources();
        if (shouldLookUpInChannelRegister) {
            this.channelRegister = ChannelRegister.getRegister();
        }
    }

    public static List<URI> extractOrcIds(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isOrcId)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(DublinCoreScraper::toUriWithOrcidPrefix)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    private static URI toUriWithOrcidPrefix(String value) {
        var orcid = value.replaceAll(ORCID_HOST_REGEX, StringUtils.EMPTY_STRING);
        return Optional.ofNullable(orcid)
            .filter(StringUtils::isNotBlank)
            .map(DublinCoreScraper::toOrcidUri)
            .orElse(null);
    }

    private static URI toOrcidUri(String orcid) {
        return UriWrapper.fromHost(ORCID_HOST).addChild(orcid).getUri();
    }

    private FundingSources readFundingSources() {
        var value = IoUtils.stringFromResources(Path.of("funding_sources.json"));
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(value, FundingSources.class))
                   .orElseThrow();
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
                   .map(DublinCoreScraper::attemptToRepairIsbn)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());
    }

    public static String attemptToRepairIsbn(String value) {
        return Optional.ofNullable(value)
                   .map(isbn -> isbn.replaceAll(DEHYPHENATION_REGEX, StringUtils.EMPTY_STRING))
                   .map(isbn -> isbn.replaceAll("[^0-9]", ""))
                   .map(isbn -> isbn.replaceAll(StringUtils.WHITESPACES, StringUtils.EMPTY_STRING))
                   .orElse(null);
    }

    public static String attemptToRepairIsmn(String value) {
        return Optional.ofNullable(value)
                   .map(isbn -> isbn.replaceAll(DEHYPHENATION_REGEX, StringUtils.EMPTY_STRING))
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

    public static String extractJournal(DublinCore dublinCore, String customer) {
        var journal = extractJournalFromDublinCore(dublinCore);
        var issnSet = extractIssn(dublinCore);
        if (isNull(journal) && issnSet.isEmpty() && customer.equals(UIO)) {
            return extractJournalFromCitationField(dublinCore);
        } else {
            return journal;
        }
    }

    private static String extractJournalFromDublinCore(DublinCore dublinCore) {
        var journals = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isJournal)
                           .map(DcValue::scrapeValueAndSetToScraped)
                           .collect(Collectors.toList());
        return journals.isEmpty() ? null : journals.get(0);
    }

    public static String extractJournalFromCitationField(DublinCore dublinCore) {
        var journals = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isCitationIdentifier)
                           .map(DcValue::scrapeValueAndSetToScraped)
                           .map(DublinCoreScraper::getJournalNameFromString)
                           .filter(Objects::nonNull)
                           .collect(Collectors.toList());
        return journals.isEmpty() ? null : journals.get(0);
    }

    private static String getJournalNameFromString(String value) {
        return attempt(() -> value.split(DOT)[0]).orElse(failure -> null);
    }

    public static Set<String> extractType(DublinCore dublinCore, String customer) {
        var types = extractType(dublinCore);
        return types.isEmpty() && customerHasAgreedToImportTypeLessPostsAsReport(customer)
                   ? Set.of(NvaType.RESEARCH_REPORT.getValue())
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

    public static Publication extractPublication(DublinCore dublinCore, String customer) {
        var publication = new Publication();
        publication.setIssnList(extractIssn(dublinCore));
        publication.setIsbnList(extractIsbn(dublinCore));
        publication.setJournal(extractJournal(dublinCore, customer));
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
                   .map(DublinCoreScraper::attemptToRepairIsmn)
                   .map(DublinCoreScraper::fixOldIsmnFormatIfNeeded)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toSet());

    }

    private static String fixOldIsmnFormatIfNeeded(String value) {
        var formattedIsmn = value.toLowerCase(Locale.ROOT);
        if (formattedIsmn.contains(OLD_ISMN_FIRST_ELEMENT)) {
            return formattedIsmn.replaceAll(OLD_ISMN_FIRST_ELEMENT, NEW_ISMN_FIRST_ELEMENT);
        } else {
            return value;
        }
    }

    public static boolean isSingleton(Collection<?> versions) {
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

    public static String extractEmbargo(DublinCore dublinCore, String customer) {
        return UIO.equals(customer) ? extractEmbargo(dublinCore) : null;
    }

    public static String extractEmbargo(DublinCore dublinCore) {
        return dublinCore
                   .getDcValues()
                   .stream()
                   .filter(DcValue::isEmbargoEndDate)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .filter(StringUtils::isNotEmpty)
                   .map(stringDate -> new SimpleEntry<>(stringDate, Embargo.getDateAsInstant(stringDate)))
                   .max(DublinCoreScraper::compareDates)
                   .map(SimpleEntry::getKey)
                   .orElse(null);
    }

    private static int compareDates(SimpleEntry<String, Instant> entry1, SimpleEntry<String, Instant> entry2) {
        return entry1.getValue().compareTo(entry2.getValue());
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

    public static String extractInsperaIdentifier(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isInsperaIdentifier)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .findAny()
                   .orElse(null);
    }

    public static String extractWiseflowIdentifier(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isaWiseflowIdentifier)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .findAny()
                   .orElse(null);
    }

    public static Type mapOriginTypeToNvaType(Set<String> types, DublinCore dublinCore, String customer) {
        var uniqueTypes = translateTypesInNorwegian(types);
        var type = new Type(uniqueTypes, TypeMapper.convertBrageTypeToNvaType(uniqueTypes));
        if (isNull(type.getNva()) && nonNull(extractCristinId(dublinCore))) {
            return new Type(types, NvaType.CRISTIN_RECORD.getValue());
        }
        if (FFI.equals(customer) && shouldBeMappedToResearchReport(type)) {
            return new Type(types, NvaType.RESEARCH_REPORT.getValue());
        }
        else {
            return type;
        }
    }

    private static boolean shouldBeMappedToResearchReport(Type type) {
        return type.getBrage().stream()
                   .anyMatch(brageType ->
                                 REPORT.getValue().equals(brageType)
                                 || RESEARCH_REPORT.getValue().equals(brageType)
                                 || OTHER_TYPE_OF_REPORT.getValue().equals(brageType));
    }

    public static String extractSubjectCodeFromFsXml(DublinCore dublinCore) {
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
            var warnings = getDublinCoreWarnings(dublinCore, customer, fundingSources);
            var record = createRecordFromDublinCoreAndBrageLocation(dublinCore,
                                                                    brageLocation,
                                                                    shouldLookUpInChannelRegister,
                                                                    customer);

            handleRecordFromFFI(record, customer);

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

    private void handleRecordFromFFI(Record record, String customer) {
        if (FFI.equals(customer)) {
            var ffiPublisher = new Publisher("39D2B1A7-091E-44C5-9579-65A75C6A6F01");
            if (publisherIsMissing(record)) {
                record.getPublication().getPublicationContext().setPublisher(ffiPublisher);
            }
            var nvaType = record.getType().getNva();
            if (hasFfiPublisherAndIsReport(record, ffiPublisher, nvaType)) {
                record.getEntityDescription().getContributors()
                    .forEach(contributor -> contributor.setAffiliations(Set.of(new Affiliation("7428.0.0.0", "FFI", null))));
            }
            record.getEntityDescription().getContributors().forEach(DublinCoreScraper::updateRoleToAuthorWhenRolesIsOther);
        }
    }

    private static void updateRoleToAuthorWhenRolesIsOther(Contributor contributor) {
        if (contributor.getRole().equals(OTHER_CONTRIBUTOR)) {
            contributor.setRole(AUTHOR);
        }
    }

    private static boolean hasFfiPublisherAndIsReport(Record record, Publisher ffiPublisher, String nvaType) {
        return record.getPublication().getPublicationContext().getPublisher().equals(ffiPublisher)
               && (nvaType.equals(NvaType.REPORT.getValue()) || nvaType.equals(NvaType.RESEARCH_REPORT.getValue()));
    }

    private static boolean publisherIsMissing(Record record) {
        return Optional.ofNullable(record.getPublication())
                   .map(Publication::getPublicationContext)
                   .map(PublicationContext::getPublisher)
                   .map(Publisher::getPid)
                   .isEmpty();
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

    private static List<URI> extractSubjects(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isLocalCode)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(DublinCoreScraper::toUri)
                   .filter(Objects::nonNull)
                   .distinct()
                   .collect(Collectors.toList());
    }

    public static List<String> extractSubjectCodesFromDublinCore(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isLocalCode)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .filter(DublinCoreScraper::isSubjectCode)
                   .distinct()
                   .collect(Collectors.toList());
    }

    private static boolean isSubjectCode(String value) {
        return value.length() < MAX_SUBJECT_CODE_LENGTH;
    }

    private static URI toUri(String value) {
        return attempt(() -> new URL(value).toURI()).orElse(failure -> null);
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
                   .map(convertToDoiUriAttempt())
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

    private static Function<String, URI> convertToDoiUriAttempt() {
        return doi -> {
            try {
                return URI.create(doi);
            } catch (Exception e) {
                return DoiValidator.isValidDoi(doi)
                           ? URI.create(DoiValidator.encodeDoiPathIfNeeded(doi))
                           : null;
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

    private static PartOfSeries extractPartOfSeries(DublinCore dublinCore) {
        var partOfSeriesValues = dublinCore.getDcValues()
                                     .stream()
                                     .filter(DcValue::isPartOfSeries)
                                     .map(DcValue::scrapeValueAndSetToScraped)
                                     .collect(Collectors.toList());
       return convertListToPartOfSeries(partOfSeriesValues, dublinCore);
    }

    private static PartOfSeries convertListToPartOfSeries(List<String> list, DublinCore dublinCore) {
        if (isSingleton(list)) {
            var parts = Arrays.stream(list.get(0).split(";"))
                            .map(String::trim)
                            .filter(value -> !value.isEmpty())
                            .collect(Collectors.toList());
            return constructPartOfSeries(parts, dublinCore);
        } else {
            return constructPartOfSeries(list, dublinCore);
        }
    }

    private static PartOfSeries constructPartOfSeries(List<String> list, DublinCore dublinCore) {
        var firstEntry = attempt(() -> list.get(0)).orElse(failure -> null);
        var secondEntry = Optional.ofNullable(attempt(() -> list.get(1)).orElse(failure -> null))
                         .or(() -> Optional.ofNullable(extractPartOf(dublinCore)))
                         .orElse(null);
        return extractSeries(firstEntry, secondEntry);
    }

    private static PartOfSeries extractSeries(String firstEntry, String secondEntry) {
        return isSeriesName(firstEntry)
                   ? new PartOfSeries(firstEntry, secondEntry)
                   : new PartOfSeries(secondEntry, firstEntry);
    }

    private static boolean isSeriesName(String value) {
        return StringUtils.isNotBlank(value) && !value.matches("\\d{4}\\W\\d{2}") && !value.matches("\\d+");
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
                   .distinct()
                   .collect(Collectors.toList());
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

    public static PublisherAuthority extractPublisherAuthority(DublinCore dublinCore) {
        var version = getVersions(dublinCore);
        return mapToNvaVersion(version);
    }

    public static Set<String> getVersions(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isOneOfTwoPossibleVersions)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toSet());
    }

    private static PublisherAuthority mapToNvaVersion(Set<String> versions) {
        var uniqueVersions = new HashSet<>(versions);
        if (isSingleton(uniqueVersions)) {
            return mapSingleVersion(uniqueVersions);
        }
        if (containsMultipleValues(uniqueVersions)) {
            var versionSet = versions.stream().map(v -> v.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
            return mapMultipleVersions(versionSet);
        }
        return new PublisherAuthority(versions, null);
    }

    private static boolean containsMultipleValues(Set<String> versions) {
        return versions.size() >= 2;
    }

    private static PublisherAuthority mapMultipleVersions(Set<String> versions) {
        if (containsVersion(versions, ACCEPTED_VERSION)) {
            return new PublisherAuthority(Collections.singleton(ACCEPTED_VERSION.getValue()), ACCEPTED_VERSION);
        }
        if (containsVersion(versions, PUBLISHED_VERSION)) {
            return new PublisherAuthority(Collections.singleton(PUBLISHED_VERSION.getValue()), PUBLISHED_VERSION);
        } else {
            return new PublisherAuthority(versions, null);
        }
    }

    private static boolean containsVersion(Set<String> versions, PublisherVersion publisherVersion) {
        return versions.stream().map(PublisherVersion::fromValue)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .anyMatch(publisherVersion::equals);
    }

    private static PublisherAuthority mapSingleVersion(Set<String> versions) {
        var version = versions.iterator().next();
        return new PublisherAuthority(Collections.singleton(version),
                                      PublisherVersion.fromValue(version).orElse(null));
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
        record.setType(mapOriginTypeToNvaType(extractType(dublinCore, customer), dublinCore, customer));
        record.setRightsHolder(extractRightsholder(dublinCore));
        record.setPublisherAuthority(extractPublisherAuthority(dublinCore));
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
        record.setAccessCode(extractAccessCode(dublinCore));
        record.setProjects(extractProjects(dublinCore, fundingSources));
        record.setPrioritizedProperties(determinePrioritizedProperties(dublinCore, customer));
        record.setInsperaIdentifier(extractInsperaIdentifier(dublinCore));
        record.setWiseflowIdentifier(extractWiseflowIdentifier(dublinCore));
        return record;
    }

    private Set<String> determinePrioritizedProperties(DublinCore dublinCore, String customer) {
        return PrioritizeField.getPrioritizedFields(dublinCore, customer);
    }

    public static List<Project> extractProjects(DublinCore dublinCore, FundingSources fundingSources) {
            return dublinCore.getDcValues().stream()
                       .filter(DcValue::isProjectRelation)
                       .map(DcValue::scrapeValueAndSetToScraped)
                       .map(value -> Project.fromBrageValue(value, fundingSources))
                       .filter(Objects::nonNull)
                       .distinct()
                       .collect(Collectors.toList());
    }

    private String extractAccessCode(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isAccessCode)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .filter(Objects::nonNull)
                   .map(AccessCodeMapper::toAccessCode)
                   .findFirst().orElse(null);
    }

    private Publication createPublicationWithIdentifier(DublinCore dublinCore,
                                                        BrageLocation brageLocation,
                                                        Record record,
                                                        boolean shouldLookUpInChannelRegister,
                                                        String customer) {
        var publication = extractPublication(dublinCore, customer);
        publication.setPublicationContext(new PublicationContext());
        publication.getPublicationContext().setBragePublisher(extractPublisher(dublinCore));
        record.setPublication(publication);
        if (nonNull(record.getType().getNva()) && shouldLookUpInChannelRegister) {
            searchForSeriesAndJournalsInChannelRegister(brageLocation, record, customer);
            searchForPublisherInChannelRegister(record, customer);
        }
        return publication;
    }

    private void searchForSeriesAndJournalsInChannelRegister(BrageLocation brageLocation, Record record,
                                                             String customer) {
        record.getPublication().getPublicationContext().setSeries(new Series(getSeriesPid(brageLocation, record,
                                                                                          customer).orElse(null)));
        record.getPublication().getPublicationContext().setJournal(new Journal(getJournalPid(brageLocation, record,
                                                                                             customer).orElse(null)));
    }

    private Optional<String> getJournalPid(BrageLocation brageLocation, Record record, String customer) {
        return Optional.ofNullable(record.getPublication())
                   .map(publication -> channelRegister.lookUpInJournal(publication, brageLocation, customer));
    }

    private Optional<String> getSeriesPid(BrageLocation brageLocation, Record record, String customer) {
        return Optional.of(record.getPublication())
                   .map(publication -> channelRegister.lookUpInJournal(publication, brageLocation, customer))
                   .or(() -> Optional.ofNullable(channelRegister.lookUpInJournalByTitle(record.getPublication().getPartOfSeries().getName(), brageLocation)));
    }

    private void searchForPublisherInChannelRegister(Record record, String customer) {
        var publisherId = channelRegister.lookUpInChannelRegisterForPublisher(record, customer);
        if (nonNull(publisherId)) {
            record.getPublication().getPublicationContext().setPublisher(new Publisher(publisherId));
        }
    }
}

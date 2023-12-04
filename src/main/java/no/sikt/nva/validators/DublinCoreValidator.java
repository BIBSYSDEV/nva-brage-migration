package no.sikt.nva.validators;

import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DATE_NOT_PRESENT_DC_DATE_ISSUED;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_DATE_ISSUED;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_RIGHTS_URI;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_TYPE;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_ISSN;
import static no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning.INVALID_ISBN_WARNING;
import static no.sikt.nva.scrapers.DublinCoreScraper.mapOriginTypeToNvaType;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.LOCAL_DATE_MAX_LENGTH;
import static no.sikt.nva.scrapers.TypeMapper.convertBrageTypeToNvaType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.ErrorDetails.Error;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.BrageNvaLanguageMapper;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.EntityDescriptionExtractor;
import no.sikt.nva.scrapers.LicenseScraper;
import no.sikt.nva.scrapers.PageConverter;
import no.sikt.nva.scrapers.SubjectScraper;
import no.sikt.nva.scrapers.TypeMapper;
import no.sikt.nva.scrapers.TypeTranslator;
import nva.commons.core.StringUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.ISSNValidator;

@SuppressWarnings("PMD.GodClass")
public final class DublinCoreValidator {

    public static final String PUBLISHED_VERSION_STRING = "publishedVersion";
    public static final String ACCEPTED_VERSION_STRING = "acceptedVersion";
    public static final String DEHYPHENATION_REGEX = "(‐|·|-|\u00AD|&#x20;)";
    public static final String YEAR_PERIOD_REGEX = "[0-9]{4}-[0-9]{4}";
    public static final String REGEX_BRACKETS_AND_DOT = "(\\.)|(\\[)|(\\])";
    public static final String UNKNOWN_YEAR_PERIOD_REGEX = "[0-9]{4}[?]";
    public static final String QUESTION_MARK = "?";
    public static final String HYPHEN = "-";
    public static final String DASH = "–";
    private static final int ONE_DESCRIPTION = 1;
    public static final String NO_CUSTOMER = null;

    public static Set<ErrorDetails> getDublinCoreErrors(DublinCore dublinCore, String customer) {

        var errors = new HashSet<ErrorDetails>();
        DoiValidator.getDoiErrorDetailsOffline(dublinCore).ifPresent(errors::addAll);
        getInvalidTypes(dublinCore, customer).ifPresent(errors::add);
        getIssnErrors(dublinCore).ifPresent(errors::add);
        getDateError(dublinCore).ifPresent(errors::add);
        BrageNvaLanguageMapper.getLanguageError(dublinCore).ifPresent(errors::add);
        getMultipleUnmappableTypeError(dublinCore, customer).ifPresent(errors::add);
        getMultipleValues(dublinCore).ifPresent(errors::addAll);
        getLicenseError(dublinCore).ifPresent(errors::add);
        return errors;
    }

    public static Set<WarningDetails> getDublinCoreWarnings(DublinCore dublinCore, String customer) {
        var warnings = new HashSet<WarningDetails>();
        SubjectScraper.getSubjectsWarnings(dublinCore).ifPresent(warnings::add);
        BrageNvaLanguageMapper.getLanguageWarning(dublinCore).ifPresent(warnings::add);
        getDescriptionsWarning(dublinCore).ifPresent(warnings::add);
        getVolumeWarning(dublinCore).ifPresent(warnings::add);
        getIsbnWarnings(dublinCore).ifPresent(warnings::add);
        getPageNumberWarning(dublinCore).ifPresent(warnings::add);
        getNonContributorsError(dublinCore).ifPresent(warnings::add);
        getTypesWarnings(dublinCore, customer).ifPresent(warnings::add);
        return warnings;
    }

    public static boolean containsYearAndMonth(String date) {
        var yearAndMonthList = Arrays.asList(date.split(HYPHEN));
        return yearAndMonthList.size() == 2 && yearAndMonthList.get(yearAndMonthList.size() - 1).matches("\\d{2}");
    }

    public static boolean containsYearOnly(String date) {
        return date.matches("\\d{4}");
    }

    public static Set<String> filterOutNullValues(Set<String> values) {
        return values.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toSet());
    }

    public static boolean containsTwoDigitYearOnly(String date) {
        return date.matches("\\d{2}");
    }

    public static boolean isPeriodDate(String date) {
        return date.matches(YEAR_PERIOD_REGEX) || date.matches(UNKNOWN_YEAR_PERIOD_REGEX);
    }

    private static Optional<WarningDetails> getTypesWarnings(DublinCore dublinCore, String customer) {
        if (isConferenceObjectOrLecture(dublinCore, customer)) {
            return Optional.of(
                new WarningDetails(Warning.CONFERENCE_OBJECT_OR_LECTURE_WILL_BE_MAPPED_TO_CONFERENCE_REPORT,
                                   DublinCoreScraper.extractType(dublinCore, customer)));
        } else {
            return Optional.empty();
        }
    }

    private static boolean isConferenceObjectOrLecture(DublinCore dublinCore, String customer) {
        var types = DublinCoreScraper.extractType(dublinCore, customer);
        return types.contains(BrageType.CONFERENCE_OBJECT.getValue()) || types.contains(
            BrageType.CONFERENCE_LECTURE.getValue());
    }

    private static Optional<ErrorDetails> getLicenseError(DublinCore dublinCore) {
        if (hasLicense(dublinCore)) {
            var licenseScraper = new LicenseScraper(dublinCore);
            var license = licenseScraper.generateLicense();
            if (licenseScraper.isValidLicense(license)) {
                return Optional.empty();
            } else {
                return Optional.of(new ErrorDetails(INVALID_DC_RIGHTS_URI,
                                                    Collections.singleton(licenseScraper.extractLicense(dublinCore))));
            }
        }
        return Optional.empty();
    }

    private static boolean hasLicense(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().anyMatch(DcValue::isLicense);
    }

    private static Optional<List<ErrorDetails>> getMultipleValues(DublinCore dublinCore) {
        var duplicates = new ArrayList<ErrorDetails>();
        checkIssuesForMultipleValues(dublinCore, duplicates);
        checkPublicationDateForMultipleValues(dublinCore, duplicates);
        checkCristinIdentifierForMultipleValues(dublinCore, duplicates);
        checkMainTitleForMultipleValues(dublinCore, duplicates);
        return !duplicates.isEmpty() ? Optional.of(duplicates) : Optional.empty();
    }

    private static void checkMainTitleForMultipleValues(DublinCore dublinCore, List<ErrorDetails> duplicates) {
        var titles = getTitles(dublinCore);
        if (hasManyValues(titles)) {
            duplicates.add(new ErrorDetails(Error.MULTIPLE_VALUES, titles));
        }
    }

    private static Set<String> getTitles(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isMainTitle)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toSet());
    }

    private static void checkPublicationDateForMultipleValues(DublinCore dublinCore, List<ErrorDetails> duplicates) {
        var dates = getDates(dublinCore);
        if (hasManyValues(dates)) {
            duplicates.add(new ErrorDetails(Error.MULTIPLE_VALUES, dates));
        }
    }

    private static void checkIssuesForMultipleValues(DublinCore dublinCore, List<ErrorDetails> duplicates) {
        var issues = getIssues(dublinCore);
        if (hasManyValues(issues)) {
            duplicates.add(new ErrorDetails(Error.MULTIPLE_VALUES, issues));
        }
    }

    private static void checkCristinIdentifierForMultipleValues(DublinCore dublinCore, List<ErrorDetails> duplicates) {
        var cristinIds = getCristinIds(dublinCore);
        if (hasManyValues(cristinIds)) {
            duplicates.add(new ErrorDetails(Error.MULTIPLE_VALUES, cristinIds));
        }
    }

    private static Set<String> getCristinIds(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isCristinDcValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toSet());
    }

    private static boolean hasManyValues(Set<String> issues) {
        return issues.size() > 1;
    }

    private static Set<String> getDates(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPublicationDate)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toSet());
    }

    private static Set<String> getIssues(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isIssue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toSet());
    }

    private static Optional<WarningDetails> getNonContributorsError(DublinCore dublinCore) {
        if (hasContributors(dublinCore)) {
            return Optional.empty();
        } else {
            return Optional.of(new WarningDetails(Warning.NO_CONTRIBUTORS, Collections.emptySet()));
        }
    }

    private static boolean hasContributors(DublinCore dublinCore) {
        return !EntityDescriptionExtractor.extractContributors(dublinCore, Map.of(), NO_CUSTOMER).isEmpty();
    }

    private static Optional<ErrorDetails> getIssnErrors(DublinCore dublinCore) {
        if (hasIssn(dublinCore)) {
            var invalidIssnList = getInvalidIssnList(dublinCore);
            var originalIssnValues = extractOriginalIssnValues(dublinCore);
            return !invalidIssnList.isEmpty() ? Optional.of(new ErrorDetails(INVALID_ISSN, originalIssnValues))
                       : Optional.empty();
        }
        return Optional.empty();
    }

    private static List<String> getInvalidIssnList(DublinCore dublinCore) {
        return DublinCoreScraper.extractIssn(dublinCore)
                   .stream()
                   .filter(issn -> !ISSNValidator.getInstance().isValid(issn))
                   .collect(Collectors.toList());
    }

    private static Set<String> extractOriginalIssnValues(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isIssnValue)
                   .map(DcValue::getValue)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toSet());
    }

    private static Optional<WarningDetails> getIsbnWarnings(DublinCore dublinCore) {
        if (hasIsbn(dublinCore)) {
            var invalidIsbnList = getInvalidIsbnList(dublinCore);
            return !invalidIsbnList.isEmpty() ? Optional.of(new WarningDetails(INVALID_ISBN_WARNING, invalidIsbnList))
                       : Optional.empty();
        }
        return Optional.empty();
    }

    private static Set<String> getInvalidIsbnList(DublinCore dublinCore) {
        return DublinCoreScraper.extractIsbn(dublinCore)
                   .stream()
                   .filter(isbn -> !ISBNValidator.getInstance().isValid(isbn))
                   .collect(Collectors.toSet());
    }

    private static Optional<WarningDetails> getDescriptionsWarning(DublinCore dublinCore) {
        var descriptionList = EntityDescriptionExtractor.extractDescriptions(dublinCore);
        if (descriptionList.size() > ONE_DESCRIPTION) {
            return Optional.of(new WarningDetails(Warning.MULTIPLE_DESCRIPTION_PRESENT, descriptionList));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<WarningDetails> getVolumeWarning(DublinCore dublinCore) {
        if (hasVolume(dublinCore)) {
            var volume = EntityDescriptionExtractor.extractVolume(dublinCore);
            try {
                assert volume != null;
                Integer.parseInt(volume);
                return Optional.empty();
            } catch (Exception e) {
                return Optional.of(new WarningDetails(Warning.VOLUME_NOT_NUMBER_WARNING, volume));
            }
        }
        return Optional.empty();
    }

    private static Optional<WarningDetails> getPageNumberWarning(DublinCore dublinCore) {
        if (hasPageNumber(dublinCore)) {
            var pageNumber = dublinCore.getDcValues()
                                 .stream()
                                 .filter(DcValue::isPageNumber)
                                 .findAny()
                                 .map(DcValue::getValue)
                                 .orElse(StringUtils.EMPTY_STRING)
                                 .replaceAll(REGEX_BRACKETS_AND_DOT, StringUtils.EMPTY_STRING)
                                 .trim();
            return PageConverter.isValidPageNumber(pageNumber) ? Optional.empty()
                       : Optional.of(new WarningDetails(Warning.PAGE_NUMBER_FORMAT_NOT_RECOGNIZED, pageNumber));
        }
        return Optional.empty();
    }

    private static Optional<ErrorDetails> getDateError(DublinCore dublinCore) {
        if (hasDate(dublinCore)) {
            var date = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isPublicationDate)
                           .findAny()
                           .map(DcValue::getValue)
                           .map(value -> value.replaceAll(StringUtils.WHITESPACES, StringUtils.EMPTY_STRING))
                           .map(value -> value.replaceAll(DASH, HYPHEN))
                           .map(DublinCoreValidator::modifyIfDateIsOfLocalDateTimeFormat)
                           .map(DublinCoreValidator::constructDateFromPeriod)
                           .orElse(new DcValue().scrapeValueAndSetToScraped());

            if (containsYearOnly(date)) {
                return Optional.empty();
            }
            if (containsYearAndMonth(date)) {
                return Optional.empty();
            }
            if (containsYearAndMonthAndDate(date)) {
                return Optional.empty();
            }
            return Optional.of(new ErrorDetails(INVALID_DC_DATE_ISSUED, Set.of(date)));
        }
        return Optional.of(new ErrorDetails(DATE_NOT_PRESENT_DC_DATE_ISSUED, Set.of()));
    }

    private static String constructDateFromPeriod(String value) {
        if (value.matches(YEAR_PERIOD_REGEX)) {
            return value.split(HYPHEN)[0];
        }
        if (value.matches(UNKNOWN_YEAR_PERIOD_REGEX)) {
            return value.replace(QUESTION_MARK, StringUtils.EMPTY_STRING);
        }
        if (value.matches("[0-9]{4}[-]")) {
            return value.replace(HYPHEN, StringUtils.EMPTY_STRING);
        }
        return value;
    }

    private static String modifyIfDateIsOfLocalDateTimeFormat(String date) {
        try {
            if (date.length() > LOCAL_DATE_MAX_LENGTH) {
                var instant = Instant.parse(date);
                return LocalDate.ofInstant(instant, ZoneOffset.UTC).toString();
            } else {
                return date;
            }
        } catch (Exception e) {
            return date;
        }
    }

    private static boolean containsYearAndMonthAndDate(String date) {
        DateValidator validator = DateValidator.getInstance();
        return validator.isValid(date, Locale.CANADA);
    }

    private static boolean hasDate(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().anyMatch(DcValue::isPublicationDate);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static Optional<ErrorDetails> getInvalidTypes(DublinCore dublinCore, String customer) {
        var uniqueTypes = DublinCoreScraper.extractType(dublinCore, customer).stream()
                              .map(TypeTranslator::translateToEnglish)
                              .collect(Collectors.toSet());
        if (uniqueTypes.isEmpty()) {
            return Optional.of(new ErrorDetails(INVALID_DC_TYPE, uniqueTypes));
        }
        if (containsMultipleTypes(uniqueTypes)) {
            return mapMultipleTypes(uniqueTypes);
        }
        if (TypeMapper.hasValidType(uniqueTypes.iterator().next())) {
            return Optional.empty();
        } else {
            return Optional.of(new ErrorDetails(INVALID_DC_TYPE, uniqueTypes));
        }
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static Optional<ErrorDetails> getMultipleUnmappableTypeError(DublinCore dublinCore, String customer) {
        var types = DublinCoreScraper.translateTypesInNorwegian(DublinCoreScraper.extractType(dublinCore, customer))
                        .stream()
                        .distinct()
                        .collect(Collectors.toSet());
        var mappedType = mapOriginTypeToNvaType(types, dublinCore);
        if (nonNull(mappedType.getNva())) {
            return Optional.empty();
        }
        if (containsMultipleTypes(types) && getInvalidTypes(dublinCore, customer).isEmpty() && !types.contains(
            BrageType.PEER_REVIEWED.getValue())) {
            return Optional.of(new ErrorDetails(Error.MULTIPLE_UNMAPPABLE_TYPES, types));
        }
        return Optional.empty();
    }

    private static boolean containsMultipleTypes(Set<String> types) {
        return types.size() >= 2;
    }

    private static Optional<ErrorDetails> mapMultipleTypes(Set<String> types) {
        var typeList = new ArrayList<>(types);
        var firstTypeToMap = typeList.get(0);
        if (firstTypeToMap.equals(BrageType.PEER_REVIEWED.getValue())) {
            var nextTypeToMap = typeList.get(1);
            if (TypeMapper.hasValidType(nextTypeToMap)) {
                return Optional.empty();
            } else {
                return Optional.of(new ErrorDetails(INVALID_DC_TYPE, types));
            }
        }
        if (TypeMapper.hasValidType(firstTypeToMap)) {
            return Optional.empty();
        }
        if (nonNull(convertBrageTypeToNvaType(types))) {
            return Optional.empty();
        }
        return Optional.of(new ErrorDetails(INVALID_DC_TYPE, types));
    }

    private static boolean hasIssn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().anyMatch(DcValue::isIssnValue);
    }

    private static boolean hasIsbn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().anyMatch(DcValue::isIsbnAndNotEmptyValue);
    }

    private static boolean hasVolume(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().anyMatch(DcValue::isVolume);
    }

    private static boolean hasPageNumber(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().anyMatch(DcValue::isPageNumber);
    }
}

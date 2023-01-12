package no.sikt.nva.validators;

import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DATE_NOT_PRESENT_ERROR;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DATE_ERROR;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_ISBN;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_ISSN;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_TYPE;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.LOCAL_DATE_MAX_LENGTH;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
import no.sikt.nva.scrapers.PageConverter;
import no.sikt.nva.scrapers.SubjectScraper;
import no.sikt.nva.scrapers.TypeMapper;
import nva.commons.core.StringUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.ISSNValidator;

@SuppressWarnings("PMD.GodClass")
public final class DublinCoreValidator {

    public static final String PUBLISHED_VERSION_STRING = "publishedVersion";
    public static final String ACCEPTED_VERSION_STRING = "acceptedVersion";
    public static final String DEHYPHENATION_REGEX = "(‐|·|-|\u00AD|&#x20;)";
    public static final String REGEX_BRACKETS_AND_DOT = "(\\.)|(\\[)|(\\])";
    private static final int ONE_DESCRIPTION = 1;

    public static List<ErrorDetails> getDublinCoreErrors(DublinCore dublinCore) {

        var errors = new ArrayList<ErrorDetails>();
        DoiValidator.getDoiErrorDetailsOffline(dublinCore).ifPresent(errors::addAll);
        getInvalidTypes(dublinCore).ifPresent(errors::add);
        getIssnErrors(dublinCore).ifPresent(errors::add);
        getIsbnErrors(dublinCore).ifPresent(errors::add);
        getDateError(dublinCore).ifPresent(errors::add);
        getNonContributorsError(dublinCore).ifPresent(errors::add);
        BrageNvaLanguageMapper.getLanguageError(dublinCore).ifPresent(errors::add);
        getMultipleUnmappableTypeError(dublinCore).ifPresent(errors::add);
        getMultipleValues(dublinCore).ifPresent(errors::addAll);
        return errors;
    }

    public static List<WarningDetails> getDublinCoreWarnings(DublinCore dublinCore) {
        var warnings = new ArrayList<WarningDetails>();
        SubjectScraper.getSubjectsWarnings(dublinCore).ifPresent(warnings::add);
        BrageNvaLanguageMapper.getLanguageWarning(dublinCore).ifPresent(warnings::add);
        getDescriptionsWarning(dublinCore).ifPresent(warnings::add);
        getVolumeWarning(dublinCore).ifPresent(warnings::add);
        getIssueWarning(dublinCore).ifPresent(warnings::add);
        getPageNumberWarning(dublinCore).ifPresent(warnings::add);
        return warnings;
    }

    public static boolean containsYearAndMonth(String date) {
        var yearAndMonthList = Arrays.asList(date.split("-"));
        return yearAndMonthList.size() == 2 && yearAndMonthList.get(yearAndMonthList.size() - 1).matches("\\d{2}");
    }

    public static boolean containsYearOnly(String date) {
        return date.matches("\\d{4}");
    }

    public static List<String> filterOutNullValues(List<String> values) {
        return values.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());
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
            duplicates.add(new ErrorDetails(Error.DUPLICATE_VALUE, titles));
        }
    }

    private static List<String> getTitles(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isMainTitle)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static void checkPublicationDateForMultipleValues(DublinCore dublinCore, List<ErrorDetails> duplicates) {
        var dates = getDates(dublinCore);
        if (hasManyValues(dates)) {
            duplicates.add(new ErrorDetails(Error.DUPLICATE_VALUE, dates));
        }
    }

    private static void checkIssuesForMultipleValues(DublinCore dublinCore, List<ErrorDetails> duplicates) {
        var issues = getIssues(dublinCore);
        if (hasManyValues(issues)) {
            duplicates.add(new ErrorDetails(Error.DUPLICATE_VALUE, issues));
        }
    }

    private static void checkCristinIdentifierForMultipleValues(DublinCore dublinCore, List<ErrorDetails> duplicates) {
        var cristinIds = getCristinIds(dublinCore);
        if (hasManyValues(cristinIds)) {
            duplicates.add(new ErrorDetails(Error.DUPLICATE_VALUE, cristinIds));
        }
    }

    private static List<String> getCristinIds(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isCristinDcValue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static boolean hasManyValues(List<String> issues) {
        return issues.size() > 1;
    }

    private static List<String> getDates(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isPublicationDate)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static List<String> getIssues(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isIssue)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static Optional<ErrorDetails> getNonContributorsError(DublinCore dublinCore) {
        if (hasContributors(dublinCore)) {
            return Optional.empty();
        } else {
            return Optional.of(new ErrorDetails(Error.NO_CONTRIBUTORS, Collections.emptyList()));
        }
    }

    private static boolean hasContributors(DublinCore dublinCore) {
        return !EntityDescriptionExtractor.extractContributors(dublinCore).isEmpty();
    }

    private static Optional<ErrorDetails> getIssnErrors(DublinCore dublinCore) {
        if (hasIssn(dublinCore)) {
            var invalidIssnList = DublinCoreScraper.extractIssn(dublinCore)
                                      .stream()
                                      .filter(issn -> !ISSNValidator.getInstance().isValid(issn))
                                      .collect(Collectors.toList());
            return !invalidIssnList.isEmpty()
                       ? Optional.of(new ErrorDetails(INVALID_ISSN, invalidIssnList))
                       : Optional.empty();
        }
        return Optional.empty();
    }

    private static Optional<ErrorDetails> getIsbnErrors(DublinCore dublinCore) {
        if (hasIsbn(dublinCore)) {
            var invalidIsbnList = DublinCoreScraper.extractIsbn(dublinCore)
                                      .stream()
                                      .map(isbn -> isbn.replaceAll(DEHYPHENATION_REGEX, StringUtils.EMPTY_STRING))
                                      .map(isbn -> isbn.replaceAll(StringUtils.WHITESPACES, StringUtils.EMPTY_STRING))
                                      .filter(isbn -> !ISBNValidator.getInstance().isValid(isbn))
                                      .collect(Collectors.toList());
            return !invalidIsbnList.isEmpty()
                       ? Optional.of(new ErrorDetails(INVALID_ISBN, invalidIsbnList))
                       : Optional.empty();
        }
        return Optional.empty();
    }

    private static Optional<WarningDetails> getDescriptionsWarning(DublinCore dublinCore) {
        var descriptions = dublinCore.getDcValues()
                               .stream()
                               .filter(DcValue::isDescription)
                               .map(DcValue::getValue)
                               .collect(
                                   Collectors.toList());
        if (descriptions.size() > ONE_DESCRIPTION) {
            return Optional.of(new WarningDetails(Warning.MULTIPLE_DESCRIPTION_PRESENT, descriptions));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<WarningDetails> getVolumeWarning(DublinCore dublinCore) {
        if (hasVolume(dublinCore)) {
            var volume = dublinCore.getDcValues().stream()
                             .filter(DcValue::isVolume)
                             .findAny()
                             .orElse(new DcValue()).getValue();
            try {
                Integer.parseInt(volume);
                return Optional.empty();
            } catch (Exception e) {
                return Optional.of(new WarningDetails(Warning.VOLUME_NOT_NUMBER_WARNING, volume));
            }
        }
        return Optional.empty();
    }

    private static Optional<WarningDetails> getIssueWarning(DublinCore dublinCore) {
        if (hasIssue(dublinCore)) {
            var issue = dublinCore.getDcValues().stream()
                            .filter(DcValue::isIssue)
                            .findAny().orElse(new DcValue()).getValue();
            try {
                Integer.parseInt(issue);
                return Optional.empty();
            } catch (Exception e) {
                return Optional.of(new WarningDetails(Warning.ISSUE_NOT_NUMBER_WARNING, issue));
            }
        }
        return Optional.empty();
    }

    private static Optional<WarningDetails> getPageNumberWarning(DublinCore dublinCore) {
        if (hasPageNumber(dublinCore)) {
            var pageNumber = dublinCore.getDcValues().stream()
                                 .filter(DcValue::isPageNumber)
                                 .findAny().map(DcValue::getValue)
                                 .orElse(StringUtils.EMPTY_STRING)
                                 .replaceAll(REGEX_BRACKETS_AND_DOT, StringUtils.EMPTY_STRING)
                                 .trim();
            return PageConverter.isValidPageNumber(pageNumber)
                       ? Optional.empty()
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
                           .map(DublinCoreValidator::modifyIfDateIsOfLocalDateTimeFormat)
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
            return Optional.of(new ErrorDetails(INVALID_DATE_ERROR, List.of(date)));
        }
        return Optional.of(new ErrorDetails(DATE_NOT_PRESENT_ERROR, List.of()));
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
    private static Optional<ErrorDetails> getInvalidTypes(DublinCore dublinCore) {
        var uniqueTypes = new ArrayList<>(new HashSet<>(DublinCoreScraper.extractType(dublinCore)));
        if (uniqueTypes.isEmpty()) {
            return Optional.of(new ErrorDetails(INVALID_TYPE, uniqueTypes));
        }
        if (uniqueTypes.size() >= 2) {
            return mapMultipleTypes(uniqueTypes);
        }
        if (TypeMapper.hasValidType(uniqueTypes.get(0))) {
            return Optional.empty();
        } else {
            return Optional.of(new ErrorDetails(INVALID_TYPE, uniqueTypes));
        }
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static Optional<ErrorDetails> getMultipleUnmappableTypeError(DublinCore dublinCore) {
        var types = new ArrayList<>(new HashSet<>(DublinCoreScraper.extractType(dublinCore)));
        if (types.size() >= 2
            && !getInvalidTypes(dublinCore).isPresent()
            && !types.contains(BrageType.PEER_REVIEWED.getType())) {
            return Optional.of(new ErrorDetails(Error.MULTIPLE_UNMAPPABLE_TYPES, types));
        }
        return Optional.empty();
    }

    private static Optional<ErrorDetails> mapMultipleTypes(List<String> types) {
        var firstTypeToMap = types.get(0);
        if (firstTypeToMap.equals(BrageType.PEER_REVIEWED.getValue())) {
            var nextTypeToMap = types.get(1);
            if (TypeMapper.hasValidType(nextTypeToMap)) {
                return Optional.empty();
            } else {
                return Optional.of(new ErrorDetails(INVALID_TYPE, types));
            }
        }
        if (TypeMapper.hasValidType(firstTypeToMap)) {
            return Optional.empty();
        }
        return Optional.of(new ErrorDetails(INVALID_TYPE, types));
    }

    private static boolean hasIssn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isIssnValue);
    }

    private static boolean hasIsbn(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isIsbnAndNotEmptyValue);
    }

    private static boolean hasVolume(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isVolume);
    }

    private static boolean hasIssue(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isIssue);
    }

    private static boolean hasPageNumber(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isPageNumber);
    }
}

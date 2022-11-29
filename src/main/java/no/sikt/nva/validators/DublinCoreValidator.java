package no.sikt.nva.validators;

import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DATE_NOT_PRESENT_ERROR;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DATE_ERROR;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_ISBN;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_ISSN;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_TYPE;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.JOURNAL_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.PUBLISHER_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.scrapers.DublinCoreScraper.channelRegister;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.scrapers.BrageNvaLanguageMapper;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.PageConverter;
import no.sikt.nva.scrapers.PublisherMapper;
import no.sikt.nva.scrapers.SubjectScraper;
import no.sikt.nva.scrapers.TypeMapper;
import nva.commons.core.StringUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.ISSNValidator;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PMD.GodClass")
public final class DublinCoreValidator {

    public static final String PUBLISHED_VERSION_STRING = "publishedVersion";
    public static final String ACCEPTED_VERSION_STRING = "acceptedVersion";
    public static final String DEHYPHENATION_REGEX = "(‐|·|-|\u00AD|&#x20;)";
    public static final String MISSING_ISSN_AND_TITLE = "Missing issn and title";
    public static final String MISSING_ISSN_AND_PUBLISHER = "Missing issn AND publisher";
    private static final int ONE_DESCRIPTION = 1;

    public static List<ErrorDetails> getDublinCoreErrors(DublinCore dublinCore, BrageLocation brageLocation) {

        var errors = new ArrayList<ErrorDetails>();
        DoiValidator.getDoiErrorDetailsOffline(dublinCore).ifPresent(errors::addAll);
        getInvalidTypes(dublinCore).ifPresent(errors::add);
        getIssnErrors(dublinCore, brageLocation).ifPresent(errors::add);
        getIsbnErrors(dublinCore, brageLocation).ifPresent(errors::add);
        getDateError(dublinCore).ifPresent(errors::add);
        getChannelRegisterErrors(dublinCore, brageLocation).ifPresent(errors::add);
        BrageNvaLanguageMapper.getLanguageError(dublinCore).ifPresent(errors::add);
        return errors;
    }

    public static List<WarningDetails> getDublinCoreWarnings(DublinCore dublinCore) {
        var warnings = new ArrayList<WarningDetails>();
        getVersionWarnings(dublinCore).ifPresent(warnings::add);
        SubjectScraper.getSubjectsWarnings(dublinCore).ifPresent(warnings::add);
        BrageNvaLanguageMapper.getLanguageWarning(dublinCore).ifPresent(warnings::add);
        getDescriptionsWarning(dublinCore).ifPresent(warnings::add);
        getVolumeWarning(dublinCore).ifPresent(warnings::add);
        getIssueWarning(dublinCore).ifPresent(warnings::add);
        getPageNumberWarning(dublinCore).ifPresent(warnings::add);
        getMultipleUnmappableTypeWarning(dublinCore).ifPresent(warnings::add);
        return warnings;
    }

    public static boolean containsYearAndMonth(String date) {
        var yearAndMonthList = Arrays.asList(date.split("-"));
        return yearAndMonthList.size() == 2 && yearAndMonthList.get(yearAndMonthList.size() - 1).matches("\\d{2}");
    }

    public static boolean containsYearOnly(String date) {
        return date.matches("\\d{4}");
    }

    public static List<String> filterOutNullValues(String... values) {
        var valuesList = Arrays.asList(values);
        return valuesList.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private static Optional<ErrorDetails> getChannelRegisterErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        if (typeIsPresentInDublinCore(dublinCore)) {
            if (isJournalArticle(dublinCore)) {
                return getErrorDetailsForJournalArticle(dublinCore, brageLocation);
            }
            if (hasPublisher(dublinCore) && isBook(dublinCore)) {
                return getErrorDetailsForBook(dublinCore, brageLocation);
            }
            if (isReport(dublinCore)) {
                return getErrorDetailsForReport(dublinCore);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private static Optional<ErrorDetails> getErrorDetailsForJournalArticle(DublinCore dublinCore,
                                                                           BrageLocation brageLocation) {
        var issn = DublinCoreScraper.extractIssn(dublinCore, brageLocation);
        var title = DublinCoreScraper.extractJournal(dublinCore);
        var possibleChannelRegisterIdentifierByIssn = channelRegister.lookUpInJournalByIssn(issn);
        var possibleChannelRegisterIdentifierByJournal = channelRegister.lookUpInJournalByTitle(title);
        if (nonNull(possibleChannelRegisterIdentifierByIssn)) {
            return Optional.empty();
        }
        if (nonNull(possibleChannelRegisterIdentifierByJournal)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForJournals(issn, title);
        }
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private static Optional<ErrorDetails> getErrorDetailsForBook(DublinCore dublinCore, BrageLocation brageLocation) {
        var publisher = DublinCoreScraper.extractPublisher(dublinCore);
        var issn = DublinCoreScraper.extractIssn(dublinCore, brageLocation);
        var title = DublinCoreScraper.extractJournal(dublinCore);
        var possibleChannelRegisterIdentifierByIssn = channelRegister.lookUpInJournalByIssn(issn);
        var possibleChannelRegisterIdentifierByJournal = channelRegister.lookUpInJournalByTitle(title);
        var possibleChannelRegisterIdentifierByPublisher =
            channelRegister.lookUpInPublisherByPublisher(PublisherMapper.getMappablePublisher(publisher));
        if (nonNull(possibleChannelRegisterIdentifierByIssn)
            || nonNull(possibleChannelRegisterIdentifierByJournal)
            || nonNull(possibleChannelRegisterIdentifierByPublisher)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForPublisher(issn, publisher);
        }
    }

    private static Optional<ErrorDetails> getErrorDetailsForReport(DublinCore dublinCore) {
        var publisher = DublinCoreScraper.extractPublisher(dublinCore);
        var title = DublinCoreScraper.extractJournal(dublinCore);
        var possibleChannelRegisterIdentifierByJournal = channelRegister.lookUpInJournalByTitle(title);
        var possibleChannelRegisterIdentifierByPublisher =
            channelRegister.lookUpInPublisherByPublisher(publisher);
        if (nonNull(possibleChannelRegisterIdentifierByJournal)
            || nonNull(possibleChannelRegisterIdentifierByPublisher)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForPublisher(publisher);
        }
    }

    @NotNull
    private static Optional<ErrorDetails> getChannelRegisterErrorDetailsWhenSearchingForJournals(String... values) {
        if (!filterOutNullValues(values).isEmpty()) {
            return Optional.of(new ErrorDetails(JOURNAL_NOT_IN_CHANNEL_REGISTER, filterOutNullValues(values)));
        } else {
            return Optional.of(
                new ErrorDetails(JOURNAL_NOT_IN_CHANNEL_REGISTER, Collections.singletonList(MISSING_ISSN_AND_TITLE)));
        }
    }

    private static Optional<ErrorDetails> getChannelRegisterErrorDetailsWhenSearchingForPublisher(String... values) {
        if (!filterOutNullValues(values).isEmpty()) {
            return Optional.of(
                new ErrorDetails(PUBLISHER_NOT_IN_CHANNEL_REGISTER, filterOutNullValues(values)));
        } else {
            return Optional.of(new ErrorDetails(PUBLISHER_NOT_IN_CHANNEL_REGISTER,
                                                Collections.singletonList(MISSING_ISSN_AND_PUBLISHER)));
        }
    }

    private static Optional<ErrorDetails> getIssnErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        if (hasIssn(dublinCore)) {
            var issn = DublinCoreScraper.extractIssn(dublinCore, brageLocation);
            ISSNValidator validator = new ISSNValidator();
            if (!validator.isValid(issn)) {
                return Optional.of(new ErrorDetails(INVALID_ISSN, List.of(issn)));
            }
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
                             .findAny().orElse(new DcValue()).getValue();
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
                                 .replaceAll("(\\.)|(\\[)|(\\])", "");
            return PageConverter.isValidPageNumber(pageNumber)
                       ? Optional.empty()
                       : Optional.of(new WarningDetails(Warning.PAGE_NUMBER_FORMAT_NOT_RECOGNIZED, pageNumber));
        }
        return Optional.empty();
    }

    private static Optional<WarningDetails> getVersionWarnings(DublinCore dublinCore) {
        if (!versionIsPresent(dublinCore)) {
            return Optional.empty();
        }
        return getVersionWarning(dublinCore);
    }

    private static Optional<ErrorDetails> getDateError(DublinCore dublinCore) {
        if (hasDate(dublinCore)) {
            var date = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isPublicationDate)
                           .findAny()
                           .orElse(new DcValue())
                           .getValue();

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

    private static boolean containsYearAndMonthAndDate(String date) {
        DateValidator validator = DateValidator.getInstance();
        return validator.isValid(date, Locale.CANADA);
    }

    private static boolean hasDate(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().anyMatch(DcValue::isPublicationDate);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static Optional<ErrorDetails> getInvalidTypes(DublinCore dublinCore) {
        var types = dublinCore.getDcValues().stream()
                        .filter(DcValue::isType)
                        .map(DcValue::getValue)
                        .collect(Collectors.toList());
        if (types.isEmpty()) {
            return Optional.of(new ErrorDetails(INVALID_TYPE, types));
        }
        if (types.size() >= 2) {
            return mapMultipleTypes(types);
        }
        if (TypeMapper.hasValidType(types.get(0))) {
            return Optional.empty();
        } else {
            return Optional.of(new ErrorDetails(INVALID_TYPE, types));
        }
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private static Optional<WarningDetails> getMultipleUnmappableTypeWarning(DublinCore dublinCore) {
        var types = DublinCoreScraper.extractType(dublinCore);
        if (types.size() >= 2
            && !getInvalidTypes(dublinCore).isPresent()
            && !types.contains(BrageType.PEER_REVIEWED.getType())) {
            return Optional.of(new WarningDetails(Warning.MULTIPLE_UNMAPPABLE_TYPES, types));
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

    private static boolean isJournalArticle(DublinCore dublinCore) {
        return DublinCoreScraper.extractType(dublinCore).contains(BrageType.JOURNAL_ARTICLE.getValue());
    }

    private static boolean isReport(DublinCore dublinCore) {
        return DublinCoreScraper.extractType(dublinCore).contains(BrageType.REPORT.getValue());
    }

    private static boolean hasPublisher(DublinCore dublinCore) {
        var publisher = DublinCoreScraper.extractPublisher(dublinCore);
        return nonNull(publisher) && publisher.equals(Element.PUBLISHER.getValue());
    }

    private static boolean isBook(DublinCore dublinCore) {
        return DublinCoreScraper.extractType(dublinCore).contains(BrageType.BOOK.getValue());
    }

    private static boolean typeIsPresentInDublinCore(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isType);
    }

    private static Optional<ErrorDetails> getIsbnErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        if (hasIsbn(dublinCore)) {
            var isbn = DublinCoreScraper.extractIsbn(dublinCore, brageLocation)
                           .replaceAll(DEHYPHENATION_REGEX, StringUtils.EMPTY_STRING)
                           .replaceAll(StringUtils.WHITESPACES, StringUtils.EMPTY_STRING);

            ISBNValidator validator = new ISBNValidator();
            if (!validator.isValid(isbn)) {
                return Optional.of(new ErrorDetails(INVALID_ISBN, List.of(isbn)));
            }
        }
        return Optional.empty();
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

    private static Optional<WarningDetails> getVersionWarning(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DublinCoreValidator::dcValueIsVersionAndVersionIsUnvalid)
                   .findAny()
                   .map(dcValue -> new WarningDetails(WarningDetails.Warning.VERSION_WARNING,
                                                      List.of(dcValue.getValue())));
    }

    private static boolean dcValueIsVersionAndVersionIsUnvalid(DcValue dcValue) {
        return dcValue.isVersion() && !isValidVersion(dcValue);
    }

    private static boolean versionIsPresent(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isVersion);
    }

    private static boolean isValidVersion(DcValue version) {
        return PUBLISHED_VERSION_STRING.equals(version.getValue())
               || ACCEPTED_VERSION_STRING.equals(version.getValue());
    }
}

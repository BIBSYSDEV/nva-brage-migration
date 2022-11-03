package no.sikt.nva.validators;

import static java.util.Objects.nonNull;
import static no.sikt.nva.scrapers.DublinCoreScraper.channelRegister;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.ErrorDetails.Error;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.SubjectScraper;
import no.sikt.nva.scrapers.TypeMapper;
import no.sikt.nva.scrapers.TypeMapper.BrageType;
import nva.commons.core.StringUtils;
import nva.commons.core.language.LanguageMapper;
import org.apache.commons.validator.routines.ISBNValidator;
import org.apache.commons.validator.routines.ISSNValidator;

@SuppressWarnings("PMD.GodClass")
public final class DublinCoreValidator {

    public static final String VERSION_STRING_NVE = "publishedVersion";
    public static final String DEHYPHENATION_REGEX = "(‐|·|-|\u00AD|&#x20;)";
    public static final URI LEXVO_URI_UNDEFINED = URI.create("http://lexvo.org/id/iso639-3/und");
    private static final int ONE_DESCRIPTION = 1;

    public static List<ErrorDetails> getDublinCoreErrors(DublinCore dublinCore, BrageLocation brageLocation) {

        var errors = new ArrayList<ErrorDetails>();
        DoiValidator.getDoiErrorDetailsOffline(dublinCore).ifPresent(errors::addAll);
        geCristinidErrorDetails(dublinCore).ifPresent(errors::add);
        getInvalidTypes(dublinCore).ifPresent(errors::add);
        getIssnErrors(dublinCore, brageLocation).ifPresent(errors::add);
        getIsbnErrors(dublinCore, brageLocation).ifPresent(errors::add);
        getChannelRegisterErrors(dublinCore, brageLocation).ifPresent(errors::add);
        return errors;
    }

    public static Optional<ErrorDetails> geCristinidErrorDetails(DublinCore dublinCore) {
        var errorList = getCristinIdentifierErrors(dublinCore);
        if (errorList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new ErrorDetails(Error.CRISTIN_ID_PRESENT, errorList));
        }
    }

    public static List<WarningDetails> getDublinCoreWarnings(DublinCore dublinCore) {
        var warnings = new ArrayList<WarningDetails>();
        getVersionWarnings(dublinCore).ifPresent(warnings::add);
        SubjectScraper.getSubjectsWarnings(dublinCore).ifPresent(warnings::add);
        getDateWarning(dublinCore).ifPresent(warnings::add);
        getLanguageWarning(dublinCore).ifPresent(warnings::add);
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

    public static Optional<ErrorDetails> getChannelRegisterErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        if (typeIsPresentInDublinCore(dublinCore) && isJournalArticle(dublinCore)) {
            var journalIssn = DublinCoreScraper.extractIssn(dublinCore, brageLocation);
            var possibleChannelRegisterIdentifierByIssn = channelRegister.lookUpInJournalByIssn(journalIssn);
            if (nonNull(journalIssn) && nonNull(possibleChannelRegisterIdentifierByIssn)) {
                return Optional.empty();
            }
            var journalTitle = DublinCoreScraper.extractJournal(dublinCore);
            var possibleChannelRegisterIdentifierByJournal = channelRegister.lookUpInJournalByTitle(journalTitle);
            if (nonNull(journalTitle) && nonNull(possibleChannelRegisterIdentifierByJournal)) {
                return Optional.empty();
            } else {
                return Optional.of(
                    new ErrorDetails(Error.NOT_IN_CHANNEL_REGISTER, List.of(brageLocation.getOriginInformation())));
            }
        }
        return Optional.empty();
    }

    private static Optional<ErrorDetails> getIssnErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        if (hasIssn(dublinCore)) {
            var issn = DublinCoreScraper.extractIssn(dublinCore, brageLocation);
            ISSNValidator validator = new ISSNValidator();
            if (!validator.isValid(issn)) {
                return Optional.of(new ErrorDetails(Error.INVALID_ISSN, List.of(issn)));
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
                                 .findAny().orElse(new DcValue()).getValue();
            try {
                Integer.parseInt(pageNumber);
                return Optional.empty();
            } catch (Exception e) {
                return Optional.of(new WarningDetails(Warning.PAGE_NUMBER_NOT_NUMBER_WARNING, pageNumber));
            }
        }
        return Optional.empty();
    }

    private static Optional<WarningDetails> getVersionWarnings(DublinCore dublinCore) {
        if (!versionIsPresent(dublinCore)) {
            return Optional.empty();
        }
        return getVersionWarning(dublinCore);
    }

    private static Optional<WarningDetails> getLanguageWarning(DublinCore dublinCore) {
        var language =
            dublinCore.getDcValues()
                .stream()
                .filter(DcValue::isLanguage).findAny().orElse(new DcValue()).getValue();
        var mappedToNvaLanguage = LanguageMapper.toUri(language);
        if (!LEXVO_URI_UNDEFINED.equals(mappedToNvaLanguage)) {
            return Optional.empty();
        } else {
            return Optional.of(new WarningDetails(Warning.LANGUAGE_MAPPED_TO_UNDEFINED,
                                                  List.of(String.valueOf(language))));
        }
    }

    private static Optional<WarningDetails> getDateWarning(DublinCore dublinCore) {
        if (hasDate(dublinCore)) {
            var date = dublinCore.getDcValues()
                           .stream()
                           .filter(DcValue::isDate)
                           .findAny()
                           .orElse(new DcValue())
                           .getValue();

            if (containsYearOnly(date)) {
                return Optional.empty();
            }
            if (containsYearAndMonth(date)) {
                return Optional.empty();
            }
            return Optional.of(new WarningDetails(Warning.INVALID_DATE_WARNING, List.of(date)));
        }
        return Optional.of(new WarningDetails(Warning.DATE_NOT_PRESENT_WARNING, List.of()));
    }

    private static boolean hasDate(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().anyMatch(DcValue::isDate);
    }

    private static List<String> getCristinIdentifierErrors(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isCristinDcValue).map(DcValue::toXmlString).collect(
                Collectors.toList());
    }

    private static Optional<ErrorDetails> getInvalidTypes(DublinCore dublinCore) {
        var types = dublinCore.getDcValues().stream()
                        .filter(DcValue::isType)
                        .map(DcValue::getValue)
                        .collect(Collectors.toList());

        if (TypeMapper.hasValidTypes(types)) {
            return Optional.empty();
        } else {
            return Optional.of(new ErrorDetails(Error.INVALID_TYPE, types));
        }
    }

    private static boolean isJournalArticle(DublinCore dublinCore) {
        return DublinCoreScraper.extractType(dublinCore).get(0).equals(BrageType.JOURNAL_ARTICLE.getValue());
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
                return Optional.of(new ErrorDetails(Error.INVALID_ISBN, List.of(isbn)));
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
                   .anyMatch(DcValue::isIsbnValue);
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
        return VERSION_STRING_NVE.equals(version.getValue());
    }
}

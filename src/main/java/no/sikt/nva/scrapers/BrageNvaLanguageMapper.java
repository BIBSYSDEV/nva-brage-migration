package no.sikt.nva.scrapers;

import static nva.commons.core.language.LanguageMapper.LEXVO_URI_UNDEFINED;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.record.Language;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.brage.migration.common.model.ErrorDetails.Error;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.StringUtils;
import nva.commons.core.language.LanguageMapper;

public class BrageNvaLanguageMapper {

    public static final int ONE_ELEMENT = 1;

    public static Language extractLanguage(DublinCore dublinCore) {
        var brageLanguages = dublinCore.getDcValues()
                                 .stream()
                                 .filter(DcValue::isLanguage)
                                 .map(DcValue::scrapeValueAndSetToScraped)
                                 .collect(
                                     Collectors.toList());
        if (!brageLanguages.isEmpty()) {
            var nvaLanguage = LanguageMapper.toUri(brageLanguages.get(0));
            return new Language(brageLanguages, nvaLanguage);
        } else {
            return new Language(brageLanguages, LEXVO_URI_UNDEFINED);
        }
    }

    public static Optional<WarningDetails> getLanguageWarning(DublinCore dublinCore) {
        var invalidLanguages =
            dublinCore.getDcValues()
                .stream()
                .filter(BrageNvaLanguageMapper::isLanguageAndLexVoUriUndefined)
                .collect(Collectors.toList());
        if (invalidLanguages.isEmpty()) {
            return Optional.empty();
        } else {
            var invalidLanguagesValues =
                invalidLanguages.stream().map(DcValue::getValue).collect(Collectors.toList());
            return Optional.of(new WarningDetails(Warning.LANGUAGE_MAPPED_TO_UNDEFINED, invalidLanguagesValues));
        }
    }

    public static Optional<ErrorDetails> getLanguageError(DublinCore dublinCore) {
        var languages =
            dublinCore.getDcValues()
                .stream()
                .filter(DcValue::isLanguage).map(DcValue::getValue).collect(Collectors.toList());
        if (languages.size() > ONE_ELEMENT) {
            return Optional.of(new ErrorDetails(Error.INVALID_LANGUAGE, languages));
        }
        if (languages.size() == ONE_ELEMENT) {
            var mappedToNvaLanguage = LanguageMapper.toUri(languages.get(0));
            if (LEXVO_URI_UNDEFINED.equals(mappedToNvaLanguage) && StringUtils.isNotEmpty(languages.get(0))) {
                return Optional.of(new ErrorDetails(Error.INVALID_LANGUAGE, languages));
            }
        }
        return Optional.empty();
    }

    private static boolean isLanguageAndLexVoUriUndefined(DcValue dcValue) {
        return dcValue.isLanguage() && LEXVO_URI_UNDEFINED.equals(LanguageMapper.toUri(dcValue.getValue()));
    }
}

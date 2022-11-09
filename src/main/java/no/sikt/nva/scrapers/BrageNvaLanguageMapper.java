package no.sikt.nva.scrapers;

import static nva.commons.core.language.LanguageMapper.LEXVO_URI_UNDEFINED;
import java.util.List;
import java.util.Optional;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.ErrorDetails.Error;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.record.Language;
import nva.commons.core.StringUtils;
import nva.commons.core.language.LanguageMapper;

public class BrageNvaLanguageMapper {

    public static Language extractLanguage(DublinCore dublinCore) {
        var brageLanguage = dublinCore.getDcValues()
                                .stream()
                                .filter(DcValue::isLanguage)
                                .findAny()
                                .orElse(new DcValue())
                                .scrapeValueAndSetToScraped();
        var nvaLanguage = LanguageMapper.toUri(brageLanguage);
        return new Language(brageLanguage, nvaLanguage);
    }

    public static Optional<WarningDetails> getLanguageWarning(DublinCore dublinCore) {
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

    public static Optional<ErrorDetails> getLanguageError(DublinCore dublinCore) {
        var language =
            dublinCore.getDcValues()
                .stream()
                .filter(DcValue::isLanguage).findAny().orElse(new DcValue()).getValue();
        var mappedToNvaLanguage = LanguageMapper.toUri(language);
        if (LEXVO_URI_UNDEFINED.equals(mappedToNvaLanguage) && StringUtils.isNotEmpty(language)) {
            return Optional.of(new ErrorDetails(Error.INVALID_LANGUAGE, List.of(language)));
        } else {
            return Optional.empty();
        }
    }
}

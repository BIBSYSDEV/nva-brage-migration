package no.sikt.nva.scrapers;

import static nva.commons.core.language.LanguageMapper.LEXVO_URI_UNDEFINED;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.ErrorDetails.Error;
import no.sikt.nva.brage.migration.common.model.record.Language;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.StringUtils;
import nva.commons.core.language.LanguageMapper;

public class BrageNvaLanguageMapper {

    public static final int ONE_ELEMENT = 1;

    public static Language extractLanguage(DublinCore dublinCore) {
        var languagesDcValues = getLanguagesAsDcValues(dublinCore);
        if (!languagesDcValues.isEmpty()) {
            return new Language(getLanguageValues(languagesDcValues),
                                LanguageMapper.toUri(getLanguagePrioritizingIsoLanguage(languagesDcValues)));
        } else {
            return new Language(getLanguageValues(languagesDcValues), LEXVO_URI_UNDEFINED);
        }
    }

    public static Optional<WarningDetails> getLanguageWarning(DublinCore dublinCore) {
        var invalidLanguages = dublinCore.getDcValues().stream()
                                   .filter(BrageNvaLanguageMapper::isLanguageAndLexVoUriUndefined)
                                   .collect(Collectors.toSet());
        if (invalidLanguages.isEmpty()) {
            return Optional.empty();
        } else {
            var invalidLanguagesValues =
                invalidLanguages.stream().map(DcValue::getValue).collect(Collectors.toSet());
            return Optional.of(new WarningDetails(Warning.LANGUAGE_MAPPED_TO_UNDEFINED, invalidLanguagesValues));
        }
    }

    public static Optional<ErrorDetails> getLanguageError(DublinCore dublinCore) {
        var languages = dublinCore.getDcValues().stream()
                            .filter(DcValue::isLanguage)
                            .map(DcValue::getValue)
                            .collect(Collectors.toSet());
        if (languages.size() > ONE_ELEMENT) {
            return getMultipleLanguageError(languages);
        }
        if (languages.size() == ONE_ELEMENT) {
            var mappedToNvaLanguage = LanguageMapper.toUri(languages.iterator().next());
            if (LEXVO_URI_UNDEFINED.equals(mappedToNvaLanguage) && StringUtils.isNotEmpty(
                languages.iterator().next())) {
                return Optional.of(new ErrorDetails(Error.INVALID_DC_LANGUAGE, languages));
            }
        }
        return Optional.empty();
    }

    private static Optional<ErrorDetails> getMultipleLanguageError(Set<String> languages) {
        return getIsoLanguages(languages).isEmpty()
                   ? Optional.of(new ErrorDetails(Error.MULTIPLE_DC_LANGUAGES_PRESENT, languages))
                   : Optional.empty();
    }

    private static Set<URI> getIsoLanguages(Set<String> languages) {
        var isoLanguages = new HashSet<URI>();
        for (String language : languages) {
            var isoLanguageUri = LanguageMapper.toUri(language);
            if (!isoLanguageUri.equals(LEXVO_URI_UNDEFINED)) {
                isoLanguages.add(isoLanguageUri);
            }
        }
        return isoLanguages;
    }

    private static Set<String> getLanguageValues(Set<DcValue> languagesDcValues) {
        return languagesDcValues.stream()
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toSet());
    }

    private static Set<DcValue> getLanguagesAsDcValues(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isLanguage)
                   .collect(Collectors.toSet());
    }

    private static String getLanguagePrioritizingIsoLanguage(Set<DcValue> brageLanguages) {
        var isoLanguages = brageLanguages.stream()
                               .filter(DcValue::isIsoLanguage)
                               .map(DcValue::getValue)
                               .collect(Collectors.toSet());
        return isoLanguages.iterator().hasNext()
                   ? isoLanguages.iterator().next()
                   : brageLanguages.stream()
                         .map(DcValue::getValue)
                         .iterator()
                         .next();
    }

    private static boolean isLanguageAndLexVoUriUndefined(DcValue dcValue) {
        return dcValue.isLanguage() && LEXVO_URI_UNDEFINED.equals(LanguageMapper.toUri(dcValue.getValue()));
    }
}

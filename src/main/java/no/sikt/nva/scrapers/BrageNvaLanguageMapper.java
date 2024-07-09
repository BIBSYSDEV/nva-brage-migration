package no.sikt.nva.scrapers;

import static nva.commons.core.language.LanguageMapper.LEXVO_URI_UNDEFINED;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Language;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.language.LanguageMapper;

public class BrageNvaLanguageMapper {

    public static final String SAMI_LANGUAGE_GROUP_ISO_CODE = "smi";
    public static final String NORTHERN_SAMI_ISO_CODE = "sme";

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
                               .map(BrageNvaLanguageMapper::convertSmiCode)
                               .collect(Collectors.toSet());
        return isoLanguages.iterator().hasNext()
                   ? isoLanguages.iterator().next()
                   : brageLanguages.stream()
                         .map(DcValue::getValue)
                         .iterator()
                         .next();
    }

    private static String convertSmiCode(String value) {
        return SAMI_LANGUAGE_GROUP_ISO_CODE.equalsIgnoreCase(value)
                   ? NORTHERN_SAMI_ISO_CODE
                   : value;
    }

    private static boolean isLanguageAndLexVoUriUndefined(DcValue dcValue) {
        return dcValue.isLanguage() && LEXVO_URI_UNDEFINED.equals(LanguageMapper.toUri(dcValue.getValue()));
    }
}

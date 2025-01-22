package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.BrageType;

public final class TypeTranslator {

    private static final Map<String, BrageType> NOR_ENG_TYPE_MAPPER = Map.ofEntries(
        entry(NorwegianType.JOURNAL_ARTICLE.getValue(), BrageType.JOURNAL_ARTICLE),
        entry(NorwegianType.JOURNAL_ARTICLE_V2.getValue(), BrageType.JOURNAL_ARTICLE),
        entry(NorwegianType.BOOK.getValue(), BrageType.BOOK),
        entry(NorwegianType.CHAPTER.getValue(), BrageType.CHAPTER),
        entry(NorwegianType.MASTER_THESIS.getValue(), BrageType.MASTER_THESIS),
        entry(NorwegianType.LECTURE.getValue(), BrageType.LECTURE),
        entry(NorwegianType.DOCTORAL_THESIS.getValue(), BrageType.DOCTORAL_THESIS),
        entry(NorwegianType.DOCTORAL_THESIS_V2.getValue(), BrageType.DOCTORAL_THESIS),
        entry(NorwegianType.RESEARCH_REPORT.getValue(), BrageType.RESEARCH_REPORT),
        entry(NorwegianType.OTHERS.getValue(), BrageType.OTHERS),
        entry(NorwegianType.WORKING_PAPER.getValue(), BrageType.WORKING_PAPER),
        entry(NorwegianType.CHRONICLE.getValue(), BrageType.CHRONICLE),
        entry(NorwegianType.CONFERENCE_OBJECT.getValue(), BrageType.CONFERENCE_OBJECT),
        entry(NorwegianType.REPORT.getValue(), BrageType.REPORT),
        entry(NorwegianType.REPORT_V2.getValue(), BrageType.REPORT)
    );

    public static String translateToEnglish(String value) {
        if (NOR_ENG_TYPE_MAPPER.containsKey(value)) {
            return NOR_ENG_TYPE_MAPPER.get(value).getValue();
        } else {
            return value;
        }
    }

    public enum NorwegianType {

        JOURNAL_ARTICLE("Tidsskriftartikkel"),
        JOURNAL_ARTICLE_V2("Tidsskriftsartikkel"),
        BOOK("Bok"),
        CHAPTER("Bokkapittel"),
        MASTER_THESIS("Mastergradsoppgave"),
        LECTURE("Forelesning"),
        DOCTORAL_THESIS("Doktorgradsavhandling"),
        RESEARCH_REPORT("Forskningsrapport"),
        OTHERS("Andre"),
        WORKING_PAPER("Arbeidsnotat"),
        CHRONICLE("Kronikk"),
        CONFERENCE_OBJECT("Konferansebidrag"),
        DOCTORAL_THESIS_V2("Doktoravhandling"),
        REPORT("Rapport"),
        REPORT_V2("rapport");;

        private final String value;

        NorwegianType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

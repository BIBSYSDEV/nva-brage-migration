package no.sikt.nva;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import org.jetbrains.annotations.NotNull;

public final class TypeMapper {

    private static final Map<Set<BrageType>, NvaType> TYPE_MAP = Map.of(
        Set.of(BrageType.BOOK, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_MONOGRAPH,
        Set.of(BrageType.CHAPTER, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_CHAPTER,
        Set.of(BrageType.JOURNAL_ARTICLE, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_ARTICLE,
        Set.of(BrageType.BOOK), NvaType.BOOK,
        Set.of(BrageType.CHAPTER), NvaType.CHAPTER,
        Set.of(BrageType.JOURNAL_ARTICLE), NvaType.JOURNAL_ARTICLE,
        Set.of(BrageType.DATASET), NvaType.DATASET,
        Set.of(BrageType.OTHERS), NvaType.OTHERS,
        Set.of(BrageType.REPORT), NvaType.REPORT,
        Set.of(BrageType.RESEARCH_REPORT), NvaType.RESEARCH_REPORT
    );
    public static final String COULD_NOT_CONVERT_TO_TYPE = "Could not convert types: ";

    public static String convertBrageTypeToNvaType(List<String> brageTypesAsString) {
        var brageTypes = convertToBrageType(brageTypesAsString);
        var nvaType = TYPE_MAP.get(brageTypes);
        if (Objects.nonNull(nvaType)) {
            return nvaType.getValue();
        } else {
            throw new DublinCoreException(COULD_NOT_CONVERT_TO_TYPE + String.join(", ", brageTypesAsString));
        }
    }

    public static boolean hasValidTypes(List<String> brageTypesAsStrings) {
        var brageTypes = convertToBrageType(brageTypesAsStrings);
        return TYPE_MAP.containsKey(brageTypes);
    }

    @NotNull
    private static Set<BrageType> convertToBrageType(List<String> brageTypesAsStrings) {
        return brageTypesAsStrings
                   .stream().map(BrageType::fromValue).collect(
                Collectors.toSet());
    }

    public enum BrageType {

        BOOK("Book"),
        CHAPTER("Chapter"),
        DATASET("Dataset"),
        JOURNAL_ARTICLE("Journal article"),
        OTHERS("Others"),
        REPORT("Report"),
        RESEARCH_REPORT("Research report"),
        PEER_REVIEWED("Peer Reviewed");

        private final String value;

        BrageType(String type) {
            this.value = type;
        }

        public static BrageType fromValue(String v) {
            for (BrageType c : BrageType.values()) {
                if (c.getValue().equalsIgnoreCase(v)) {
                    return c;
                }
            }
            return null;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return value;
        }
    }

    public enum NvaType {
        BOOK("Faglig monografi"),
        CHAPTER("Faglig kapittel"),
        DATASET("Datasett"),
        JOURNAL_ARTICLE("Fagartikkel"),
        OTHERS("Annen rapport"),
        REPORT("Rapport"),
        RESEARCH_REPORT("Forskningsrapport"),
        SCIENTIFIC_MONOGRAPH("Vitenskapelig monografi"),
        SCIENTIFIC_CHAPTER("Vitenskapelig kapittel"),
        SCIENTIFIC_ARTICLE("Vitenskapelig artikkel");

        private final String value;

        NvaType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

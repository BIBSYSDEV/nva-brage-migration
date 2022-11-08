package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import static java.util.Objects.isNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;

public final class TypeMapper {

    private static final String COULD_NOT_CONVERT_TO_TYPE = "Could not convert types: ";
    private static final Map<Set<BrageType>, NvaType> TYPE_MAP = Map.ofEntries(
        entry(Set.of(BrageType.BOOK, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_MONOGRAPH),
        entry(Set.of(BrageType.CHAPTER, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_CHAPTER),
        entry(Set.of(BrageType.JOURNAL_ARTICLE, BrageType.PEER_REVIEWED), NvaType.SCIENTIFIC_ARTICLE),
        entry(Set.of(BrageType.BOOK), NvaType.BOOK),
        entry(Set.of(BrageType.CHAPTER), NvaType.CHAPTER),
        entry(Set.of(BrageType.JOURNAL_ARTICLE), NvaType.JOURNAL_ARTICLE),
        entry(Set.of(BrageType.DATASET), NvaType.DATASET),
        entry(Set.of(BrageType.OTHERS), NvaType.OTHERS),
        entry(Set.of(BrageType.REPORT), NvaType.REPORT),
        entry(Set.of(BrageType.RESEARCH_REPORT), NvaType.RESEARCH_REPORT),
        entry(Set.of(BrageType.BACHELOR_THESIS), NvaType.BACHELOR_THESIS),
        entry(Set.of(BrageType.MASTER_THESIS), NvaType.MASTER_THESIS),
        entry(Set.of(BrageType.DOCTORAL_THESIS), NvaType.DOCTORAL_THESIS),
        entry(Set.of(BrageType.STUDENT_PAPER), NvaType.STUDENT_PAPER),
        entry(Set.of(BrageType.WORKING_PAPER), NvaType.WORKING_PAPER),
        entry(Set.of(BrageType.STUDENT_PAPER_OTHERS), NvaType.STUDENT_PAPER_OTHERS),
        entry(Set.of(BrageType.DESIGN_PRODUCT), NvaType.DESIGN_PRODUCT),
        entry(Set.of(BrageType.CHRONICLE), NvaType.CHRONICLE),
        entry(Set.of(BrageType.SOFTWARE), NvaType.SOFTWARE),
        entry(Set.of(BrageType.LECTURE), NvaType.LECTURE),
        entry(Set.of(BrageType.RECORDING_MUSICAL), NvaType.RECORDING_MUSICAL),
        entry(Set.of(BrageType.RECORDING_ORAL), NvaType.RECORDING_ORAL),
        entry(Set.of(BrageType.PLAN_OR_BLUEPRINT), NvaType.PLAN_OR_BLUEPRINT),
        entry(Set.of(BrageType.MAP), NvaType.MAP)
    );

    public static String convertBrageTypeToNvaType(List<String> brageTypesAsString) {
        var brageTypes = brageTypesAsString
                             .stream()
                             .map(BrageType::fromValue)
                             .collect(Collectors.toList());
        var nvaType = TYPE_MAP.get(Set.copyOf(brageTypes));

        if (isNull(nvaType) && brageTypes.size() >= 2) {
            for (TypeMapper.BrageType type : brageTypes) {
                if (hasValidType(type.toString())) {
                    return TYPE_MAP.get(Collections.singleton(type)).getValue();
                }
            }
        }
        if (Objects.nonNull(nvaType)) {
            return nvaType.getValue();
        } else {
            throw new DublinCoreException(COULD_NOT_CONVERT_TO_TYPE + String.join(", ", brageTypesAsString));
        }
    }

    public static boolean hasValidType(String brageType) {
        var typeFromMap = convertToBrageType(brageType);
        return TYPE_MAP.containsKey(Collections.singleton(typeFromMap));
    }

    private static BrageType convertToBrageType(String brageType) {
        return BrageType.fromValue(brageType);
    }

    public enum BrageType {

        BOOK("Book"),
        CHAPTER("Chapter"),
        DATASET("Dataset"),
        JOURNAL_ARTICLE("Journal article"),
        OTHERS("Others"),
        REPORT("Report"),
        RESEARCH_REPORT("Research report"),
        BACHELOR_THESIS("Bachelor thesis"),
        MASTER_THESIS("Master thesis"),
        DOCTORAL_THESIS("Doctoral thesis"),
        WORKING_PAPER("Working paper"),
        STUDENT_PAPER("Student paper"),
        STUDENT_PAPER_OTHERS("Student paper, others"),
        DESIGN_PRODUCT("Design product"),
        CHRONICLE("Chronicle"),
        SOFTWARE("Software"),
        LECTURE("Lecture"),
        RECORDING_MUSICAL("Recording, musical"),
        RECORDING_ORAL("Recording, oral"),
        PLAN_OR_BLUEPRINT("Plan or blueprint"),
        MAP("Map"),
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
        BACHELOR_THESIS("DegreeBachelor"),
        MASTER_THESIS("DegreeMaster"),
        DOCTORAL_THESIS("Doctoral thesis"),
        WORKING_PAPER("ReportWorkingPaper"),
        STUDENT_PAPER("OtherStudentWork"),
        STUDENT_PAPER_OTHERS("Other student thesis"),
        RESEARCH_REPORT("Forskningsrapport"),
        DESIGN_PRODUCT("Design"),
        CHRONICLE("Feature article"),
        SOFTWARE("Programvare"),
        LECTURE("Lecture"),
        RECORDING_MUSICAL("Music"),
        RECORDING_ORAL("Lydopptak, verbalt"),
        PLAN_OR_BLUEPRINT("Architecture"),
        MAP("Map"),
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

package no.sikt.nva.brage.migration.common.model;

public enum BrageType {

    BOOK("Book"),
    CHAPTER("Chapter"),
    DATASET("Dataset"),
    JOURNAL_ARTICLE("Journal article"),
    JOURNAL_ISSUE("Journal issue"),
    OTHERS("Others"),
    OTHER("Other"),
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
    NOTES("Notat"),
    POSTER("Poster"),
    PRESENTATION("Presentasjon"),
    PEER_REVIEWED("Peer reviewed");

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

}

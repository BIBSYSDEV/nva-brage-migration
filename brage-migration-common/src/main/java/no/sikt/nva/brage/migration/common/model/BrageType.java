package no.sikt.nva.brage.migration.common.model;

public enum BrageType {

    BOOK("Book"),
    BOOK_OF_ABSTRACTS("Book of abstracts"),
    CHAPTER("Chapter"),
    DATASET("Dataset"),
    DATA_SET("Data set"),
    JOURNAL_ARTICLE("Journal article"),
    ARTICLE("Article"),
    JOURNAL_ISSUE("Journal issue"),
    OTHERS("Others"),
    OTHER("Other"),
    REPORT("Report"),
    OTHER_TYPE_OF_REPORT("Other type of report"),
    RESEARCH_REPORT("Research report"),
    BACHELOR_THESIS("Bachelor thesis"),
    MASTER_THESIS("Master thesis"),
    SPECIAL_THESIS("Spesialavhandling"),
    DOCTORAL_THESIS("Doctoral thesis"),
    WORKING_PAPER("Working paper"),
    STUDENT_PAPER("Student paper"),
    STUDENT_PAPER_OTHERS("Student paper, others"),
    STUDENT_THESIS_OTHER("Other student thesis"),
    DESIGN_PRODUCT("Design product"),
    CHRONICLE("Chronicle"),
    FEATURE_ARTICLE("Feature article"),
    SOFTWARE("Software"),
    LECTURE("Lecture"),
    RECORDING_MUSICAL("Recording, musical"),
    RECORDING_ORAL("Recording, oral"),
    PLAN_OR_BLUEPRINT("Plan or blueprint"),
    MAP("Map"),
    NOTES("Notat"),
    POSTER("Poster"),
    PRESENTATION("Presentasjon"),
    CONFERENCE_OBJECT("Conference object"),
    CONFERENCE_POSTER("Conference poster"),
    CONFERENCE_LECTURE("Conference lecture"),
    PRESENTATION_OTHER("Other presentation"),
    INTERVIEW("Interview"),
    ANTHOLOGY("Anthology"),
    PERFORMING_ARTS("Performing arts"),
    PROFESSIONAL_ARTICLE("Professional article"),
    READER_OPINION("Reader opinion"),
    VISUAL_ARTS("Visual arts"),
    ARCHITECTURE("Architecture"),
    ACADEMIC_ARTICLE("Academic article"),
    ACADEMIC_CHAPTER("Academic chapter"),
    ACADEMIC_MONOGRAPH("Academic monograph"),
    NON_FICTION_MONOGRAPH("Non-fiction monograph"),
    NON_FICTION_CHAPTER("Non-fiction chapter"),
    REPORT_CHAPTER("Report chapter"),
    PEER_REVIEWED("Peer reviewed"),
    TEXTBOOK("Textbook");

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

package no.sikt.nva.model;

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

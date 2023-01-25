package no.sikt.nva.model.dublincore;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import nva.commons.core.JacocoGenerated;

@XmlType(name = "qualifier")
@XmlEnum
public enum Qualifier {

    //elements collected from this specification: https://dok.unit.no/brage/veiledninger/metadatafelter

    @XmlEnumValue("advisor")
    ADVISOR("advisor"),
    @XmlEnumValue("author")
    AUTHOR("author"),

    @XmlEnumValue("abstract")
    ABSTRACT("abstract"),

    @XmlEnumValue("department")
    DEPARTMENT("department"),

    @XmlEnumValue("editor")
    EDITOR("editor"),

    @XmlEnumValue("illustrator")
    ILLUSTRATOR("illustrator"),

    @XmlEnumValue("orcid")
    ORCID("orcid"),

    @XmlEnumValue("other")
    OTHER("other"),

    @XmlEnumValue("spatial")
    SPATIAL("spatial"),

    @XmlEnumValue("temporal")
    TEMPORAL("temporal"),

    @XmlEnumValue("accessioned")
    ACCESSIONED("accessioned"),

    @XmlEnumValue("available")
    AVAILABLE("available"),

    @XmlEnumValue("copyright")
    COPYRIGHT("copyright"),

    @XmlEnumValue("created")
    CREATED("created"),

    @XmlEnumValue("embargoenddate")
    EMBARGO_DATE("embargoenddate"),

    @XmlEnumValue("issued")
    ISSUED("issued"),

    @XmlEnumValue("submitted")
    SUBMITTED("submitted"),

    @XmlEnumValue("updated")
    UPDATED("updated"),

    @XmlEnumValue("degree")
    DEGREE("degree"),

    @XmlEnumValue("embargo")
    EMBARGO("embargo"),

    @XmlEnumValue("localcode")
    LOCAL_CODE("localcode"),

    @XmlEnumValue("provenance")
    PROVENANCE("provenance"),

    @XmlEnumValue("sponsorship")
    SPONSORSHIP("sponsorship"),

    @XmlEnumValue("tableofcontents")
    TABLE_OF_CONTENTS("tableofcontents"),

    @XmlEnumValue("version")
    VERSION("version"),

    @XmlEnumValue("extent")
    EXTENT("extent"),

    @XmlEnumValue("medium")
    MEDIUM("medium"),

    @XmlEnumValue("mimetype")
    MIME_TYPE("mimetype"),

    @XmlEnumValue("citation")
    CITATION("citation"),

    @XmlEnumValue("doi")
    DOI("doi"),

    @XmlEnumValue("isbn")
    ISBN("isbn"),

    @XmlEnumValue("ismn")
    ISMN("ismn"),

    @XmlEnumValue("issn")
    ISSN("issn"),

    @XmlEnumValue("pmid")
    PMID("pmid"),

    @XmlEnumValue("slug")
    SLUG("slug"),

    @XmlEnumValue("urn")
    URN("urn"),

    @XmlEnumValue("iso")
    ISO("iso"),

    @XmlEnumValue("haspart")
    HAS_PART("haspart"),

    @XmlEnumValue("ispartof")
    IS_PART_OF("ispartof"),

    @XmlEnumValue("ispartofseries")
    IS_PART_OF_SERIES("ispartofseries"),

    @XmlEnumValue("project")
    PROJECT("project"),

    @XmlEnumValue("holder")
    HOLDER("holder"),

    @XmlEnumValue("license")
    LICENSE("license"),

    @XmlEnumValue("articlenumber")
    ARTICLE_NUMBER("articlenumber"),

    @XmlEnumValue("issue")
    ISSUE("issue"),

    @XmlEnumValue("journal")
    JOURNAL("journal"),

    @XmlEnumValue("pagenumber")
    PAGE_NUMBER("pagenumber"),

    @XmlEnumValue("volume")
    VOLUME("volume"),

    @XmlEnumValue("agrovoc")
    AGROVOC("agrovoc"),

    @XmlEnumValue("classification")
    CLASSIFICATION("classification"),

    @XmlEnumValue("ddc")
    DEWEY_DECIMAL_CLASSIFICATION_NUMBER("ddc"),

    @XmlEnumValue("hrcs")
    HEALTH_RESEARCH_CLASSIFICATION_SYSTEM("hrcs"),

    @XmlEnumValue("humord")
    HUM_ORD("humord"),

    @XmlEnumValue("jel")
    JEL("jel"),

    @XmlEnumValue("keyword")
    KEYWORD("keyword"),

    @XmlEnumValue("lcc")
    LIBRARY_OF_CONGRESS_CLASSIFICATION("lcc"),

    @XmlEnumValue("mesh")
    MEDICAL_SUBJECT_HEADINGS("mesh"),

    @XmlEnumValue("nsi")
    NORWEGIAN_SCIENCE_INDEX("nsi"),

    @XmlEnumValue("nus")
    NORSK_STANDARD_FOR_UTDANNINGSGRUPPERING("nus"),

    @XmlEnumValue("realfagstermer")
    REALFAGS_TERMER("realfagstermer"),

    @XmlEnumValue("alternative")
    ALTERNATIVE("alternative"),

    @XmlEnumValue("uri")
    URI("uri"),

    @XmlEnumValue("cristin")
    CRISTIN("cristin"),

    @XmlEnumValue("cristinID")
    CRISTIN_ID_MUNIN("cristinID"),

    @XmlEnumValue("none")
    NONE("none");

    private final String value;

    Qualifier(String v) {
        value = v;
    }

    @JacocoGenerated
    public static Qualifier fromValue(String v) {
        for (Qualifier c : Qualifier.values()) {
            if (c.getValue().equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String getValue() {
        return value;
    }
}

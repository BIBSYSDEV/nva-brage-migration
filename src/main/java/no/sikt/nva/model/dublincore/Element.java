package no.sikt.nva.model.dublincore;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import nva.commons.core.JacocoGenerated;

@XmlType(name = "element")
@XmlEnum
public enum Element {

    //elements collected from this specification: https://dok.unit.no/brage/veiledninger/metadatafelter

    @XmlEnumValue("contributor")
    CONTRIBUTOR("contributor"),

    @XmlEnumValue("coverage")
    COVERAGE("coverage"),

    @XmlEnumValue("creator")
    creator("creator"),

    @XmlEnumValue("date")
    DATE("date"),

    @XmlEnumValue("description")
    DESCRIPTION("description"),

    @XmlEnumValue("format")
    FORMAT("format"),

    @XmlEnumValue("identifier")
    IDENTIFIER("identifier"),

    @XmlEnumValue("language")
    LANGUAGE("language"),

    @XmlEnumValue("provenance")
    PROVENANCE("provenance"),

    @XmlEnumValue("publisher")
    PUBLISHER("publisher"),

    @XmlEnumValue("relation")
    RELATION("relation"),

    @XmlEnumValue("rights")
    RIGHTS("rights"),

    @XmlEnumValue("source")
    SOURCE("source"),

    @XmlEnumValue("subject")
    SUBJECT("subject"),

    @XmlEnumValue("title")
    TITLE("title"),

    @XmlEnumValue("type")
    TYPE("type");

    private final String value;

    Element(String v) {
        value = v;
    }

    @JacocoGenerated
    public static Element fromValue(String v) {
        for (Element c : Element.values()) {
            if (c.getValue().equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    @JacocoGenerated
    public String getValue() {
        return value;
    }
}

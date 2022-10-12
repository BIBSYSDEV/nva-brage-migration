package no.sikt.nva.model.dublincore;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import nva.commons.core.JacocoGenerated;

@XmlType(name = "qualifier")
@XmlEnum
public enum Qualifier {

    @XmlEnumValue("author")
    AUTHOR("author"),

    @XmlEnumValue("abstract")
    ABSTRACT("abstract"),

    @XmlEnumValue("uri")
    URI("uri"),

    @XmlEnumValue("cristin")
    CRISTIN("cristin"),

    @XmlEnumValue("issn")
    ISSN("issn"),

    @XmlEnumValue("journal")
    JOURNAL("journal"),

    @XmlEnumValue("none")
    NONE("none"),

    ACCESSIONED("accessioned");

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

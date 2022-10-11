package no.sikt.nva.model.dublincore;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import nva.commons.core.JacocoGenerated;

@XmlType(name = "element")
@XmlEnum
public enum Element {

    @XmlEnumValue("contributor")
    CONTRIBUTOR("contributor"),

    @XmlEnumValue("title")
    TITLE("title"),

    @XmlEnumValue("identifier")
    IDENTIFIER("identifier"),

    @XmlEnumValue("type")
    TYPE("type"),

    @XmlEnumValue("language")
    LANGUAGE("language"),

    @XmlEnumValue("publisher")
    PUBLISHER("publisher"),

    @XmlEnumValue("source")
    SOURCE("source");

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

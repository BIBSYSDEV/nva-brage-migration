package no.sikt.nva.model.dublincore;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import nva.commons.core.JacocoGenerated;

public class DcValue {


    @XmlAttribute
    private Element element;

    @XmlAttribute
    private Qualifier qualifier;

    @XmlValue
    private String value;

    public DcValue() {
        
    }

    public DcValue(Element element, Qualifier qualifier, String value) {
        this.element = element;
        this.qualifier = qualifier;
        this.value = value;
    }

    @JacocoGenerated
    public Element getElement() {
        return element;
    }

    @JacocoGenerated
    public Qualifier getQualifier() {
        return qualifier;
    }

    @JacocoGenerated
    public String getValue() {
        return value;
    }

}

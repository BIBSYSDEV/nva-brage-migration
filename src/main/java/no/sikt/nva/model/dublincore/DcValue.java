package no.sikt.nva.model.dublincore;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import java.io.StringWriter;

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

    public Element getElement() {
        return element;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public String getValue() {
        return value;
    }

    public boolean isPublisher() {
        return Element.PUBLISHER.equals(this.element);
    }

    public boolean isLanguage() {
        return Element.LANGUAGE.equals(this.element);
    }

    public boolean isTitle() {
        return Element.TITLE.equals(this.element);
    }

    public boolean isType() {
        return Element.TYPE.equals(this.element);
    }

    public boolean isJournal() {
        return Element.SOURCE.equals(this.element) && Qualifier.JOURNAL.equals(this.qualifier);
    }

    public boolean isIssnValue() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.ISSN.equals(this.qualifier);
    }

    public boolean isCristinDcValue() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.CRISTIN.equals(this.qualifier);
    }

    public boolean isAuthor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.AUTHOR.equals(this.qualifier);
    }

    public boolean isVersion() {
        return Element.DESCRIPTION.equals(this.element) && Qualifier.VERSION.equals(this.qualifier);
    }


    public String toXmlString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString();
    }
}

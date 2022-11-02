package no.sikt.nva.model.dublincore;

import static no.sikt.nva.scrapers.HandleScraper.HANDLE_DOMAIN;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import java.io.StringWriter;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

public class DcValue {

    public static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    @XmlAttribute
    private Element element;

    @XmlAttribute
    private Qualifier qualifier;

    @XmlValue
    private String value;

    private boolean scraped;

    public DcValue() {
        this.scraped = false;
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

    public boolean isDoi() {
        return Element.IDENTIFIER.equals(this.element)
               && Qualifier.DOI.equals(this.qualifier)
               || Element.IDENTIFIER.equals(this.element)
                  && Qualifier.URI.equals(this.qualifier)
                  && value.contains("doi.org");
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public boolean isSubject() {
        return Element.SUBJECT.equals(this.element);
    }

    public boolean isScraped() {
        return scraped;
    }

    public String scrapeValueAndSetToScraped() {
        scraped = true;
        return value;
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

    public boolean isMainTitle() {
        return Element.TITLE.equals(this.element) && Qualifier.NONE.equals(this.qualifier);
    }

    public boolean isAlternativeTitle() {
        return Element.TITLE.equals(this.element) && Qualifier.ALTERNATIVE.equals(this.qualifier);
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

    public boolean isIsbnValue() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.ISBN.equals(this.qualifier);
    }

    public boolean isCristinDcValue() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.CRISTIN.equals(this.qualifier);
    }

    public boolean isVersion() {
        return Element.DESCRIPTION.equals(this.element) && Qualifier.VERSION.equals(this.qualifier);
    }

    public boolean isRightsholder() {
        return Element.RIGHTS.equals(this.element) && Qualifier.HOLDER.equals(this.qualifier);
    }

    public boolean isDate() {
        return Element.DATE.equals(this.element) && Qualifier.ISSUED.equals(this.qualifier);
    }

    public boolean isContributor() {
        return Element.CONTRIBUTOR.equals(this.element);
    }

    public boolean isAdvisor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.ADVISOR.equals(this.qualifier);
    }

    public boolean isAuthor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.AUTHOR.equals(this.qualifier);
    }

    public boolean isEditor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.EDITOR.equals(this.qualifier);
    }

    public boolean isIllustrator() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.ILLUSTRATOR.equals(this.qualifier);
    }

    public boolean isOtherContributor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.OTHER.equals(this.qualifier);
    }

    public boolean isPartOfSeries() {
        return Element.RELATION.equals(this.element) && Qualifier.IS_PART_OF_SERIES.equals(this.qualifier);
    }

    public boolean isHandle() {
        if (StringUtils.isNotEmpty(value)) {
            return Element.IDENTIFIER.equals(this.element)
                   && Qualifier.URI.equals(this.qualifier)
                   && value.contains(HANDLE_DOMAIN.getHost());
        } else {
            return false;
        }
    }

    public boolean isLicenseInformation() {
        return Element.RIGHTS.equals(this.element)
               && (Qualifier.NONE.equals(this.qualifier)
                   || Qualifier.URI.equals(this.qualifier));
    }

    public boolean isAbstract() {
        return Element.DESCRIPTION.equals(this.element)
               && Qualifier.ABSTRACT.equals(this.qualifier);
    }

    public boolean isDescription() {
        return Element.DESCRIPTION.equals(this.element)
               && Qualifier.NONE.equals(this.qualifier);
    }

    public boolean isSpatialCoverage() {
        return Element.COVERAGE.equals(this.element) && Qualifier.SPATIAL.equals(this.qualifier);
    }

    public boolean isCopyrightDate() {
        return Element.DATE.equals(this.element) && Qualifier.COPYRIGHT.equals(this.qualifier);
    }

    public boolean isVolume() {
        return Element.SOURCE.equals(this.element) && Qualifier.VOLUME.equals(this.qualifier);
    }

    public boolean isIssue() {
        return Element.SOURCE.equals(this.element) && Qualifier.ISSUE.equals(this.qualifier);
    }

    public boolean isPageNumber() {
        return Element.SOURCE.equals(this.element) && Qualifier.PAGE_NUMBER.equals(this.qualifier);
    }

    public boolean isProjectRelation() {
        return Element.RELATION.equals(this.element) && Qualifier.PROJECT.equals(this.qualifier);
    }

    public String toXmlString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString().replace(XML_PREFIX, StringUtils.EMPTY_STRING);
    }
}

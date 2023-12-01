package no.sikt.nva.model.dublincore;

import static java.util.Objects.nonNull;
import static no.sikt.nva.scrapers.HandleScraper.HANDLE_DOMAIN;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import java.io.StringWriter;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

@SuppressWarnings({"PMD.GodClass", "PMD.ExcessivePublicCount"})
public class DcValue {

    public static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    private static final String DOI_PREFIX = "10.";
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
                  && hasDoiPrefix();
    }

    public boolean hasDoiPrefix() {
        return nonNull(value)
               && value.contains(DOI_PREFIX);
    }

    public boolean isLink() {
        return Element.IDENTIFIER.equals(this.element)
               && Qualifier.DOI.equals(this.qualifier)
               && isNotADoi()
               || Element.IDENTIFIER.equals(this.element)
                  && Qualifier.URI.equals(this.qualifier)
                  && isNotADoi()
                  && isNotAHandle();
    }

    public boolean isCreator() {
        return Element.CREATOR.equals(this.getElement());
    }

    private boolean isNotAHandle() {
        return !value.contains(HANDLE_DOMAIN.getHost());
    }

    private boolean isNotADoi() {
        return !value.contains("doi");
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public boolean isSubject() {
        return Element.SUBJECT.equals(this.element);
    }

    public boolean isSubjectCode() {
        return Element.SUBJECT_CODE.equals(this.element);
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

    public boolean isRelationUri() {
        return Element.RELATION.equals(this.element) && Qualifier.URI.equals(this.qualifier);
    }

    public boolean isLanguage() {
        return Element.LANGUAGE.equals(this.element);
    }

    public boolean isIsoLanguage() {
        return Element.LANGUAGE.equals(this.element) && Qualifier.ISO.equals(this.qualifier);
    }

    public boolean isMainTitle() {
        return Element.TITLE.equals(this.element) && Qualifier.NONE.equals(this.qualifier);
    }

    public boolean isAlternativeTitle() {
        return Element.TITLE.equals(this.element) && Qualifier.ALTERNATIVE.equals(this.qualifier);
    }

    public boolean isType() {
        return Element.TYPE.equals(this.element) && !isTypeAndVersion();
    }

    public boolean isTypeAndVersion() {
        return Element.TYPE.equals(this.element) && Qualifier.VERSION.equals(this.qualifier);
    }

    public boolean isJournal() {
        return Element.SOURCE.equals(this.element) && Qualifier.JOURNAL.equals(this.qualifier);
    }

    public boolean isIssnValue() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.ISSN.equals(this.qualifier);
    }

    public boolean isIsbnAndNotEmptyValue() {
        if (StringUtils.isEmpty(value)) {
            scraped = true;
        }
        return isIsbnValue() && StringUtils.isNotEmpty(value);
    }

    public boolean isIsbnValue() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.ISBN.equals(this.qualifier);
    }

    public boolean isCristinDcValue() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.CRISTIN.equals(this.qualifier)
               || Element.IDENTIFIER.equals(this.element) && Qualifier.CRISTIN_ID_MUNIN.equals(this.qualifier);
    }

    public boolean isOtherIdentifier() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.OTHER.equals(this.qualifier);
    }

    public boolean isVersion() {
        return Element.DESCRIPTION.equals(this.element) && Qualifier.VERSION.equals(this.qualifier);
    }

    public boolean isOneOfTwoPossibleVersions() {
        return isVersion() || isTypeAndVersion();
    }

    public boolean isRightsholder() {
        return Element.RIGHTS.equals(this.element) && Qualifier.HOLDER.equals(this.qualifier);
    }

    public boolean isPublicationDate() {
        return Element.DATE.equals(this.element) && Qualifier.ISSUED.equals(this.qualifier);
    }

    public boolean isSourceNone() {
        return Element.SOURCE.equals(this.element) && Qualifier.NONE.equals(this.qualifier);
    }

    public boolean isCreatorNone() {
        return Element.CREATOR.equals(this.element) && Qualifier.NONE.equals(this.qualifier);
    }

    public boolean isFormatExtent() {
        return Element.FORMAT.equals(this.element) && Qualifier.EXTENT.equals(this.qualifier);
    }

    public boolean isFormatMimeType() {
        return Element.FORMAT.equals(this.element) && Qualifier.MIME_TYPE.equals(this.qualifier);
    }

    public boolean isIdentifierNone() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.NONE.equals(this.qualifier);
    }

    public boolean isAvailableDate() {
        return Element.DATE.equals(this.element) && Qualifier.AVAILABLE.equals(this.qualifier);
    }

    public boolean isAccessionedDate() {
        return Element.DATE.equals(this.element) && Qualifier.ACCESSIONED.equals(this.qualifier);
    }

    public boolean isCreatedDate() {
        return Element.DATE.equals(this.element) && Qualifier.CREATED.equals(this.qualifier);
    }

    public boolean isUpdatedDate() {
        return Element.DATE.equals(this.element) && Qualifier.UPDATED.equals(this.qualifier);
    }

    public boolean isContributor() {
        return Element.CONTRIBUTOR.equals(this.element);
    }

    public boolean isAdvisor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.ADVISOR.equals(this.qualifier)
            || Element.CREATOR.equals(this.element) && Qualifier.ADVISOR.equals(this.qualifier);
    }

    public boolean isAuthor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.AUTHOR.equals(this.qualifier)
            || Element.CREATOR.equals(this.element) && Qualifier.AUTHOR.equals(this.qualifier);
    }

    public boolean isEditor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.EDITOR.equals(this.qualifier)
               || Element.CREATOR.equals(this.element) && Qualifier.EDITOR.equals(this.qualifier);
    }

    public boolean isIllustrator() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.ILLUSTRATOR.equals(this.qualifier)
               || Element.CREATOR.equals(this.element) && Qualifier.ILLUSTRATOR.equals(this.qualifier);
    }

    public boolean isOtherContributor() {
        return Element.CONTRIBUTOR.equals(this.element) && Qualifier.OTHER.equals(this.qualifier)
               || Element.CREATOR.equals(this.element) && Qualifier.OTHER.equals(this.qualifier);
    }

    public boolean isPartOfSeries() {
        return Element.RELATION.equals(this.element) && Qualifier.IS_PART_OF_SERIES.equals(this.qualifier);
    }

    public boolean isPartOf() {
        return Element.RELATION.equals(this.element) && Qualifier.IS_PART_OF.equals(this.qualifier);
    }

    public boolean isHasPart() {
        return Element.RELATION.equals(this.element) && Qualifier.HAS_PART.equals(this.qualifier);
    }

    public boolean isLocalCode() {
        return Element.DESCRIPTION.equals(this.element) && Qualifier.LOCAL_CODE.equals(this.qualifier);
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

    public boolean isLicense() {
        return Element.RIGHTS.equals(this.element) && Qualifier.URI.equals(this.qualifier);
    }

    public boolean isAbstract() {
        return Element.DESCRIPTION.equals(this.element)
               && Qualifier.ABSTRACT.equals(this.qualifier);
    }

    public boolean isDescription() {
        return Element.DESCRIPTION.equals(this.element)
               && (Qualifier.NONE.equals(this.qualifier) || Qualifier.LOCAL_CODE.equals(this.qualifier));
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

    public boolean isArticleNumber() {
        return Element.SOURCE.equals(this.element) && Qualifier.ARTICLE_NUMBER.equals(this.qualifier);
    }

    public boolean isProjectRelation() {
        return Element.RELATION.equals(this.element) && Qualifier.PROJECT.equals(this.qualifier);
    }

    public boolean isProvenanceDescription() {
        return Element.DESCRIPTION.equals(this.element) && Qualifier.PROVENANCE.equals(this.qualifier);
    }

    public boolean isSponsorShipDescription() {
        return Element.DESCRIPTION.equals(this.element) && Qualifier.SPONSORSHIP.equals(this.qualifier);
    }

    public boolean isCitationIdentifier() {
        return Element.IDENTIFIER.equals(this.element) && Qualifier.CITATION.equals(this.qualifier);
    }

    public boolean isNsiSubject() {
        return Element.SUBJECT.equals(this.element) && Qualifier.NORWEGIAN_SCIENCE_INDEX.equals(this.qualifier);
    }

    public boolean isNoneDate() {
        return Element.DATE.equals(this.element) && Qualifier.NONE.equals(this.qualifier);
    }

    public boolean isEmbargo() {
        return Element.DESCRIPTION.equals(this.element) && Qualifier.EMBARGO.equals(this.qualifier);
    }

    public boolean isEmbargoEndDate() {
        return Element.DATE.equals(this.element) && Qualifier.EMBARGO_DATE.equals(this.qualifier);
    }

    public String toXmlString() {
        StringWriter sw = new StringWriter();
        JAXB.marshal(this, sw);
        return sw.toString()
                   .replace(XML_PREFIX, StringUtils.EMPTY_STRING)
                   .replace("\n", StringUtils.EMPTY_STRING);
    }
}

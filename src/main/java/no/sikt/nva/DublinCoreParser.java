package no.sikt.nva;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.publisher.Publication;
import no.sikt.nva.model.record.Record;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JacocoGenerated
@SuppressWarnings({"PMD.CollapsibleIfStatements", "PMD.CognitiveComplexity"})
public class DublinCoreParser {

    public static final String HAS_CRISTIN_ID_MESSAGE_TEMPLATE =
        "Following resource has Cristin identifier: %s, this error "
        + "occurred in: %s";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreParser.class);

    public Record convertDublinCoreToRecord(DublinCore dublinCore, Record record) {
        ArrayList<String> authors = new ArrayList<>();
        Publication publication = new Publication();

        for (DcValue dcValue : dublinCore.getDcValues()) {
            checkIfHasCristinIdentifier(record, dcValue);
            setRecordValuesByElement(record, dcValue);
            setAuthors(authors, dcValue);
            setPublication(publication, dcValue);
        }

        record.setAuthors(authors);

        return record;
    }

    private void setPublication(Publication publication, DcValue dcValue) {

    }

    private void setPublishers(List<String> publishers, DcValue dcValue) {
        var element = dcValue.getElement();

        if (elementIsNotNull(element) && elementIsPublisher(element)) {
            publishers.add(dcValue.getValue());
        }
    }

    private void setJournals(List<String> publishers, DcValue dcValue) {
        var element = dcValue.getElement();
        var qualifier = dcValue.getQualifier();

        if (elementIsNotNull(element) && elementIsSource(element)) {
            if (qualifierIsJournal(qualifier)) {
                publishers.add(dcValue.getValue());
            }
        }
    }


    private boolean qualifierIsJournal(Qualifier qualifier) {
        return qualifier == Qualifier.JOURNAL;
    }

    private boolean qualifierIsIssn(Qualifier qualifier) {
        return qualifier == Qualifier.ISSN;
    }

    private boolean elementIsSource(Element element) {
        return element == Element.SOURCE;
    }

    private boolean elementIsPublisher(Element element) {
        return element == Element.PUBLISHER;
    }

    private void setAuthors(List<String> authors, DcValue dcValue) {
        var element = dcValue.getElement();
        if (elementIsNotNull(element) && elementIsContributor(element)) {
            authors.add(dcValue.getValue());
        }
    }

    private boolean elementIsContributor(Element element) {
        return element == Element.CONTRIBUTOR;
    }

    private boolean elementIsIdentifier(Element element) {
        return element == Element.IDENTIFIER;
    }

    private void setRecordValuesByElement(Record record, DcValue dcValue) {
        var element = dcValue.getElement();

        if (elementIsNotNull(element)) {
            switch (element) {
                case TITLE:
                    record.setTitle(dcValue.getValue());
                    break;
                case LANGUAGE:
                    record.setLanguage(dcValue.getValue());
                    break;
                case TYPE:
                    record.setType(dcValue.getValue());
                    break;
                default:
                    break;
            }
        }
    }

    private void setIdentifier(Publication publication, DcValue dcValue) {
        var element = dcValue.getElement();
        var qualifier = dcValue.getQualifier();

        if (elementIsNotNull(element) && elementIsIdentifier(element)) {
            switch (qualifier) {
                case ISSN:
                    publication.setIssn(dcValue.getValue());
                    break;
            }
        }
    }

    private boolean elementIsNotNull(Element element) {
        return Objects.nonNull(element);
    }

    private void checkIfHasCristinIdentifier(Record record, DcValue dcValue) {
        if (dcValue.hasCristinId()) {
            throw new DublinCoreException(String.format(HAS_CRISTIN_ID_MESSAGE_TEMPLATE, dcValue.getValue(),
                                                        record.getOriginInformation()));
        }
    }
}

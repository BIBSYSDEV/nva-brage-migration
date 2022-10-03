package no.sikt.nva;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import no.sikt.nva.exceptions.CristinException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DublinCoreParser {

    public static final String SLASH = "/";
    public static final String HAS_CRISTIN_ID_MESSAGE = "Following resource has Cristin identifier: ";
    private static final String AUTHOR_QUALIFIER = "author";
    private static final String IDENTIFIER_ELEMENT = "identifier";
    private static final String CONTRIBUTOR_ELEMENT = "contributor";
    private static final String TITLE_ELEMENT = "title";
    private static final String LANGUAGE_ELEMENT = "language";
    private static final String TYPE_ELEMENT = "type";
    private static final String URI_QUALIFIER = "uri";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreParser.class);


    public Record parseDublinCore(File file) throws CristinException, JAXBException {
        var unmarshaller = getUnmarshaller();
        try {
            DublinCore dublinCore = (DublinCore) unmarshaller.unmarshal(file);
            return convertDublinCoreToRecord(dublinCore);
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new CristinException(e.getMessage());
        }
    }

    private Record convertDublinCoreToRecord(DublinCore dublinCore) throws CristinException {
        Record record = new Record();
        ArrayList<String> authors = new ArrayList<>();
        for (DcValue dcValue : dublinCore.getDcValues()) {

            var element = dcValue.getElement();
            var qualifier = dcValue.getQualifier();

            if (element != null) {
                if (CONTRIBUTOR_ELEMENT.equals(element.getValue())) {
                    extractAuthor(authors, dcValue);
                    continue;
                }
                if (TITLE_ELEMENT.equals(element.getValue())) {
                    extractTitle(record, dcValue);
                    continue;
                }
                if (LANGUAGE_ELEMENT.equals(element.getValue())) {
                    extractLanguage(record, dcValue);
                    continue;
                }
                if (TYPE_ELEMENT.equals(element.getValue())) {
                    extractType(record, dcValue);
                    continue;
                }
                if (isValidUriIdentifier(element, qualifier)) {
                    extractId(record, dcValue);
                }
            }
            if (dcValue.hasCristinId()) {
                throw new CristinException(HAS_CRISTIN_ID_MESSAGE + dcValue.getValue());
            }
        }
        record.setAuthors(authors);
        return record;
    }

    private boolean isValidUriIdentifier(Element element, Qualifier qualifier) {
        return element != null && qualifier != null &&
               IDENTIFIER_ELEMENT.equals(element.getValue()) &&
               URI_QUALIFIER.equals(qualifier.getValue());
    }

    private void extractTitle(Record record, DcValue dcValue) {
        var element = dcValue.getElement();
        if (element != null) {
            if (TITLE_ELEMENT.equals(element.getValue())) {
                record.setTitle(dcValue.getValue());
            }
        }
    }

    private void extractAuthor(ArrayList<String> authors, DcValue dcValue) {
        var qualifier = dcValue.getQualifier();
        var element = dcValue.getElement();
        if (qualifier != null && element != null) {
            if (AUTHOR_QUALIFIER.equals(qualifier.getValue()) && CONTRIBUTOR_ELEMENT.equals(element.getValue())) {
                authors.add(dcValue.getValue());
            }
        }
    }

    private void extractLanguage(Record record, DcValue dcValue) {
        var element = dcValue.getElement();
        if (element != null) {
            if (LANGUAGE_ELEMENT.equals(element.getValue())) {
                record.setLanguage(dcValue.getValue());
            }
        }
    }

    private void extractType(Record record, DcValue dcValue) {
        var element = dcValue.getElement();
        if (element != null) {
            if (TYPE_ELEMENT.equals(element.getValue())) {
                record.setType(dcValue.getValue());
            }
        }
    }

    private void extractId(Record record, DcValue dcValue) {
        var element = dcValue.getElement();
        var qualifier = dcValue.getQualifier();
        if (element != null && qualifier != null) {
            if (IDENTIFIER_ELEMENT.equals(element.getValue()) && URI_QUALIFIER.equals(qualifier.getValue())) {
                var id = convertUriToHandle(dcValue.getValue());
                record.setId(id);
            }
        }
    }

    private String convertUriToHandle(String uri) {
        var list = uri.split(SLASH);
        var firstPartOfId = list[list.length - 2];
        var secondPartOfId = list[list.length - 1];
        return firstPartOfId + secondPartOfId;
    }

    private Unmarshaller getUnmarshaller() throws JAXBException {
        return JAXBContext.newInstance(DublinCore.class).createUnmarshaller();
    }

}

package no.sikt.nva;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.CollapsibleIfStatements", "PMD.CognitiveComplexity"})
public class DublinCoreParser {

    public static final String SLASH = "/";
    public static final String HAS_CRISTIN_ID_MESSAGE = "Following resource has Cristin identifier: ";
    private static final String IDENTIFIER_ELEMENT = "identifier";
    private static final String CONTRIBUTOR_ELEMENT = "contributor";
    private static final String TITLE_ELEMENT = "title";
    private static final String LANGUAGE_ELEMENT = "language";
    private static final String TYPE_ELEMENT = "type";
    private static final String URI_QUALIFIER = "uri";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreParser.class);

    public Record parseDublinCoreToRecord(File file) throws DublinCoreException {
        try {
            var dublinCore = parseDublinCore(file);
            return convertDublinCoreToRecord(dublinCore);
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new DublinCoreException(e.getMessage());
        }
    }

    private Record convertDublinCoreToRecord(DublinCore dublinCore) throws DublinCoreException {
        Record record = new Record();
        ArrayList<String> authors = new ArrayList<>();
        for (DcValue dcValue : dublinCore.getDcValues()) {

            var element = dcValue.getElement();
            var qualifier = dcValue.getQualifier();

            if (element != null) {
                if (isValidDcElement(dcValue, CONTRIBUTOR_ELEMENT)) {
                    authors.add(dcValue.getValue());
                    continue;
                }
                if (isValidDcElement(dcValue, TITLE_ELEMENT)) {
                    record.setTitle(dcValue.getValue());
                    continue;
                }
                if (isValidDcElement(dcValue, LANGUAGE_ELEMENT)) {
                    record.setLanguage(dcValue.getValue());
                    continue;
                }
                if (isValidDcElement(dcValue, TYPE_ELEMENT)) {
                    record.setType(dcValue.getValue());
                    continue;
                }
                if (isValidUriIdentifier(element, qualifier)) {
                    extractId(record, dcValue);
                }
            }
            if (dcValue.hasCristinId()) {
                throw new DublinCoreException(HAS_CRISTIN_ID_MESSAGE + dcValue.getValue());
            }
        }
        record.setAuthors(authors);
        return record;
    }

    private boolean isValidDcElement(DcValue dcValue, String elementIdentifier) throws DublinCoreException {
        var element = dcValue.getElement();
        if (element != null) {
            return elementIdentifier.equals(element.getValue());
        }
        throw new DublinCoreException("Invalid element for the resource");
    }

    private boolean isValidUriIdentifier(Element element, Qualifier qualifier) {
        return element != null && qualifier != null
               && IDENTIFIER_ELEMENT.equals(element.getValue())
               && URI_QUALIFIER.equals(qualifier.getValue());
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

    private DublinCore parseDublinCore(File file) throws JAXBException {

        var unmarshaller = JAXBContext.newInstance(DublinCore.class).createUnmarshaller();
        return (DublinCore) unmarshaller.unmarshal(file);
    }
}

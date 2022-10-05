package no.sikt.nva;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.CollapsibleIfStatements", "PMD.CognitiveComplexity"})
public class DublinCoreParser {

    public static final String HAS_CRISTIN_ID_MESSAGE = "Following resource has Cristin identifier: ";
    private static final String UNABLE_TO_UNMARSHALL_DUBLIN_CORE_XML = "Unable to unmarshall dublin_core.xml";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreParser.class);

    public static DublinCore unmarshallDublinCore(File file) {
        try {
            var unmarshaller = JAXBContext.newInstance(DublinCore.class).createUnmarshaller();
            return (DublinCore) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new DublinCoreException(UNABLE_TO_UNMARSHALL_DUBLIN_CORE_XML, e.getCause());
        }
    }

    public Record parseDublinCoreToRecord(File file, Record record) {
        try {
            var dublinCore = unmarshallDublinCore(file);
            return convertDublinCoreToRecord(dublinCore, record);
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new DublinCoreException(e.getMessage());
        }
    }

    private Record convertDublinCoreToRecord(DublinCore dublinCore, Record record) {
        ArrayList<String> authors = new ArrayList<>();

        for (DcValue dcValue : dublinCore.getDcValues()) {

            if (dcValue.hasCristinId()) {
                throw new DublinCoreException(HAS_CRISTIN_ID_MESSAGE + dcValue.getValue());
            }

            var element = dcValue.getElement();

            if (Objects.nonNull(element)) {

                switch (element) {
                    case CONTRIBUTOR:
                        authors.add(dcValue.getValue());
                        break;
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
        record.setAuthors(authors);
        return record;
    }
}

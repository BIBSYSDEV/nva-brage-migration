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

    public static final String HAS_CRISTIN_ID_MESSAGE_TEMPLATE =
        "Following resource has Cristin identifier: %s, this error "
        + "occurred in: %s";
    private static final String UNABLE_TO_UNMARSHALL_DUBLIN_CORE_XML_TEMPLATE = "Unable to unmarshall dublin_core"
                                                                                + ".xml, This occurred in %s";
    private static final Logger logger = LoggerFactory.getLogger(DublinCoreParser.class);

    public static DublinCore unmarshallDublinCore(File file, String location) {
        try {
            var unmarshaller = JAXBContext.newInstance(DublinCore.class).createUnmarshaller();
            return (DublinCore) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new DublinCoreException(
                String.format(UNABLE_TO_UNMARSHALL_DUBLIN_CORE_XML_TEMPLATE, location),
                e.getCause());
        }
    }

    public Record parseDublinCoreToRecord(File file, Record record) {
        try {
            var dublinCore = unmarshallDublinCore(file, record.getOriginInformation());
            return convertDublinCoreToRecord(dublinCore, record);
        } catch (Exception e) {
            logger.info(e.getMessage(), record.getOriginInformation());
            throw new DublinCoreException(record.getOriginInformation(), e);
        }
    }

    private Record convertDublinCoreToRecord(DublinCore dublinCore, Record record) {
        ArrayList<String> authors = new ArrayList<>();

        for (DcValue dcValue : dublinCore.getDcValues()) {

            if (dcValue.hasCristinId()) {
                throw new DublinCoreException(String.format(HAS_CRISTIN_ID_MESSAGE_TEMPLATE, dcValue.getValue(),
                                                            record.getOriginInformation()));
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

package no.sikt.nva;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.util.List;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;

public final class DublinCoreFactory {

    private static final String UNABLE_TO_UNMARSHALL_DUBLIN_CORE_XML_TEMPLATE =
        "Unable to unmarshall dublin_core.xml";

    private DublinCoreFactory() {
    }

    public static DublinCore createDublinCoreFromXml(File xml) {
        try {
            var unmarshaller = JAXBContext.newInstance(DublinCore.class).createUnmarshaller();
            return (DublinCore) unmarshaller.unmarshal(xml);
        } catch (JAXBException e) {
            throw new DublinCoreException(
                UNABLE_TO_UNMARSHALL_DUBLIN_CORE_XML_TEMPLATE, e);
        }
    }

    public static DublinCore createDublinCoreWithDcValues(List<DcValue> dcValues) {
        return new DublinCore(dcValues);
    }
}

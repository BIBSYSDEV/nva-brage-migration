package no.sikt.nva;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.util.List;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;

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

    public static DublinCore createDublinCoreWithDcValue(Element element, Qualifier qualifier, String value) {
        var dcValue = new DcValue(element, qualifier, value);
        return new DublinCore(List.of(dcValue));
    }
}

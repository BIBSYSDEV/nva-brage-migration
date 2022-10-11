package no.sikt.nva.model.dublincore;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.List;
import no.sikt.nva.exceptions.DublinCoreException;

@XmlRootElement(name = "dublin_core")
public class  DublinCore {

    @XmlElement(name = "dcvalue")
    private List<DcValue> dcValues;

    public DublinCore() {

    }

    public DublinCore(List<DcValue> dcValues) {
        this.dcValues = dcValues;
    }

    public List<DcValue> getDcValues() {
        return dcValues;
    }
}

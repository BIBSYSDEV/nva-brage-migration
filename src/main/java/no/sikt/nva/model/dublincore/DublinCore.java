package no.sikt.nva.model.dublincore;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "dublin_core")
public class DublinCore {

    @XmlElement(name = "dcvalue")
    private List<DcValue> dcValues;

    /**
     * Constructor is necessary to parse dublin_core
     */
    public DublinCore() {

    }

    public DublinCore(List<DcValue> dcValues) {
        this.dcValues = dcValues;
    }

    public List<DcValue> getDcValues() {
        return dcValues;
    }
}

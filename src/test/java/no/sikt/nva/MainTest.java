package no.sikt.nva;

import static org.junit.jupiter.api.Assertions.assertEquals;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Objects;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainTest {

    private Main main;
    private String[] arguments;

    @BeforeEach
    void init() {
        this.main = new Main();
        this.arguments = new String[]{};
    }

    @Test
    void runningMain() throws JAXBException, IOException {
        Main.main(arguments);
    }

    @Test
    void shouldBeAbleToConvertDublinCoreXml() throws JAXBException {
        var dublinCore = unmarshallXmlToDublinCore();
        var expectedTitle = "Studie av friluftsliv blant barn og unge i Oslo: Sosial ulikhet og sosial utjevning";
        var actualTitle = getDcValueContainingTitle(dublinCore);
        assertEquals(expectedTitle, actualTitle.getValue());
    }

    private DublinCore unmarshallXmlToDublinCore() throws JAXBException {
        var dublinCoreXML = IoUtils.inputStreamFromResources("dublin_core.xml");
        JAXBContext context = JAXBContext.newInstance(DublinCore.class);
        return (DublinCore) context.createUnmarshaller().unmarshal(dublinCoreXML);
    }

    private DcValue getDcValueContainingTitle(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream().filter(dcValue -> isTitle(dcValue)).findFirst().orElseThrow();
    }

    private boolean isTitle(DcValue dcValue) {
        if (Objects.isNull(dcValue.getElement())) {
            return false;
        } else {
            return (dcValue.getElement().equals(Element.TITLE));
        }
    }
}

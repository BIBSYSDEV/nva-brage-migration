import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.nio.file.Path;
import java.util.Objects;
import no.sikt.nva.Main;
import no.sikt.nva.model.DcValue;
import no.sikt.nva.model.DublinCore;
import no.sikt.nva.model.Element;
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
    void runningMain() {
        Main.main(arguments);
    }

    @Test
    void shouldBeAbleToConvertDublinCoreXml() throws JAXBException {
        var dublinCoreXML = IoUtils.inputStreamFromResources(Path.of("dublin_core.xml"));
        JAXBContext context = JAXBContext.newInstance(DublinCore.class);
        var dublinCore = (DublinCore) context.createUnmarshaller().unmarshal(dublinCoreXML);
        var expectedTitle = "Studie av friluftsliv blant barn og unge i Oslo: Sosial ulikhet og sosial utjevning";
        var actualTitle =
            dublinCore.getDcValues().stream().filter(dcValue -> isTitle(dcValue)).findFirst().orElseThrow();
        assertTrue(dublinCore instanceof DublinCore);
        assertEquals(expectedTitle, actualTitle.getValue());
    }

    private boolean isTitle(DcValue dcValue) {
        if (Objects.isNull(dcValue.getElement())) {
            return false;
        } else {
            return (dcValue.getElement().equals(Element.TITLE));
        }
    }
}

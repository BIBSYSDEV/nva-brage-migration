import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.nio.file.Path;
import java.util.List;
import no.sikt.nva.HandleScraper;
import no.sikt.nva.model.DcValue;
import no.sikt.nva.model.DublinCore;
import no.sikt.nva.model.Element;
import no.sikt.nva.model.Qualifier;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;

public class HandleScraperTest {

    private final Path dublin_core_xml_with_handle = Path.of("dublin_core.xml");
    private final Path handle_file = Path.of("handle");
    private final Path not_a_dublin_core_file = Path.of("handle");
    private final Path dublin_core_xml_without_handle = Path.of("dublin_core_without_handle.xml");
    private HandleScraper handleScraper;

    @Test
    void shouldReturnHandleFromDublinCoreIfDublinCoreHasHandle() {
        var uriString = "https://hdl.handle.net/11250/2684299";
        var expectedHandle = UriWrapper.fromUri(uriString).getUri();
        var dublinCoreWithHandle = generateDublinCoreWithhandle(uriString);
        var actualHandle = HandleScraper.extractHandleFromDublinCore(dublinCoreWithHandle);
        assertThat(actualHandle, is(equalTo(expectedHandle)));
    }

    private DublinCore generateDublinCoreWithhandle(String handleURI){
        var dcValue = new DcValue(Element.IDENTIFIER, Qualifier.URI, handleURI);
        return new DublinCore(List.of(dcValue));
    }
}

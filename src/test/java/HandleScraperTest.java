import static no.sikt.nva.HandleScraper.ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED;
import static no.sikt.nva.HandleScraper.ERROR_MESSAGE_NO_HANDLE_IN_DUBLIN_CORE;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.file.Path;
import java.util.List;
import no.sikt.nva.HandleScraper;
import no.sikt.nva.exceptions.HandleException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HandleScraperTest {

    private final Path handleFile = Path.of("handle");

    @Test
    void shouldReturnHandleFromDublinCoreIfDublinCoreHasHandle() {
        var uriString = "https://hdl.handle.net/11250/2684299";
        var expectedHandle = UriWrapper.fromUri(uriString).getUri();
        var dublinCoreWithHandle = generateDublinCoreWithhandle(uriString);
        var actualHandle = HandleScraper.extractHandleFromDublinCore(dublinCoreWithHandle);
        assertThat(actualHandle, is(equalTo(expectedHandle)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com/11250/2684299", "https://hdl.handle.net"})
    void shouldThrowExceptionWhenhandleInDublinCoreIsMalformed(String malformedHandle) {
        var dublinCoreWithHandle = generateDublinCoreWithhandle(malformedHandle);
        var exception = assertThrows(HandleException.class,
                                     () -> HandleScraper.extractHandleFromDublinCore(dublinCoreWithHandle));
        assertThat(exception.getMessage(),
                   containsString(String.format(ERROR_MESSAGE_HANDLE_IN_DUBLIN_CORE_IS_MALFORMED, malformedHandle)));
    }

    @Test
    void shouldThrowExceptionWhenDublinCoreIsMissingHandle() {
        var dublinCoreWithouthHandle = generateDublinCoreWithoutHandle();
        var exception = assertThrows(HandleException.class,
                                     () -> HandleScraper.extractHandleFromDublinCore(dublinCoreWithouthHandle));
        assertThat(exception.getMessage(),
                   containsString(ERROR_MESSAGE_NO_HANDLE_IN_DUBLIN_CORE));
    }

    @Test
    void shouldBeAbleToExtractHandleFromHandleFile() {
        var expectedHandle = UriWrapper.fromUri("https://hdl.handle.net/11250/2684299").getUri();
        var actualHandle = HandleScraper.extractHandleFromHandlePath(handleFile);
        assertThat(actualHandle, is(equalTo(expectedHandle)));
    }

    private DublinCore generateDublinCoreWithoutHandle() {
        var dcValue = new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, randomString());
        return new DublinCore(List.of(dcValue));
    }

    private DublinCore generateDublinCoreWithhandle(String handleURI) {
        var dcValue = new DcValue(Element.IDENTIFIER, Qualifier.URI, handleURI);
        return new DublinCore(List.of(dcValue));
    }
}

package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.HANDLE_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.scrapers.HandleScraper.COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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

    public static final String TEST_FILE_LOCATION = "src/test/resources/handles.csv";
    private final Path handleFile = Path.of(TEST_RESOURCE_PATH + HANDLE_FILE_NAME);

    @Test
    void shouldReturnHandleFromDublinCoreIfDublinCoreHasHandle() throws HandleException {
        var rescueMapNotContainingTitle = Map.of("Some title", "https://hdl.handle.net/11250/daffdf");
        var incorrectHandlePath = Path.of("does/not/exists");
        var handleScraper = new HandleScraper(rescueMapNotContainingTitle);
        var uriString = "https://hdl.handle.net/11250/2684299";
        var expectedHandle = UriWrapper.fromUri(uriString).getUri();
        var dublinCoreWithHandle = generateDublinCoreWithhandle(uriString);
        var actualHandle = handleScraper.scrapeHandle(incorrectHandlePath, dublinCoreWithHandle);
        assertThat(actualHandle, is(equalTo(expectedHandle)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com/11250/2684299", "https://hdl.handle.net"})
    void shouldThrowExceptionWhenHandleInDublinCoreIsMalformed(String malformedHandle) {
        var rescueMapNotContainingTitle = Map.of("Some title", "https://hdl.handle.net/11250/daffdf");
        var incorrectHandlePath = Path.of("does/not/exists");
        var handleScraper = new HandleScraper(rescueMapNotContainingTitle);
        var dublinCoreWithHandle = generateDublinCoreWithhandle(malformedHandle);
        var exception = assertThrows(HandleException.class,
                                     () -> handleScraper.scrapeHandle(incorrectHandlePath, dublinCoreWithHandle));
        assertThat(exception.getMessage(),
                   containsString(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV));
    }

    @Test
    void shouldThrowExceptionWhenThereIsNoHandleAnyWhere() {
        var rescueMapNotContainingTitle = Map.of("Some title", "https://hdl.handle.net/11250/daffdf");
        var incorrectHandlePath = Path.of("does/not/exists");
        var dublinCoreWithouthHandle = generateDublinCoreWithoutHandle();
        var handleScraper = new HandleScraper(rescueMapNotContainingTitle);
        var exception = assertThrows(HandleException.class,
                                     () -> handleScraper.scrapeHandle(incorrectHandlePath, dublinCoreWithouthHandle));
        assertThat(exception.getMessage(),
                   containsString(COULD_NOT_FIND_HANDLE_IN_HANDLE_FILE_NOR_DUBLIN_CORE_OR_IN_SUPPLIED_CSV));
    }

    @Test
    void shouldBeAbleToExtractHandleFromHandleFileIfThereIsNoMatchingHandleInTitleHandleMap() throws HandleException {
        var titleHandleMapWithoutDublinCoreTitle = Map.of("Some title", "https://hdl.handle.net/11250/daffdf");
        var dublinCore = generateDublinCoreWithTitle("Some title not in rescue map");
        var expectedHandle = UriWrapper.fromUri("https://hdl.handle.net/11250/2684299").getUri();
        var handleScraper = new HandleScraper(titleHandleMapWithoutDublinCoreTitle);
        var actualHandle = handleScraper.scrapeHandle(handleFile, dublinCore);
        assertThat(actualHandle, is(equalTo(expectedHandle)));
    }

    @Test
    void findsHandleInRescueMapIfTitleIsPresentInDublinCoreAndInRescueMap() throws HandleException {
        var expectedHandle = UriWrapper.fromUri("https://hdl.handle.net/11250/2684299").getUri();
        var title = randomString();
        var dublinCore = generateDublinCoreWithTitle(title);
        var rescueMap = Map.of(title, expectedHandle.toString());
        var handleScraper = new HandleScraper(rescueMap);
        var actualHandle = handleScraper.scrapeHandle(Path.of("invalid/and"
                                                              + "/ignored"),
                                                      dublinCore);
        assertThat(actualHandle, is(equalTo(expectedHandle)));
    }

    @Test
    void shouldCreateListOfHandlesFromExternalHandleFile() {
        var scraper = new HandleScraper();
        var expectedHandles = List.of("https://hdl.handle.net/11250/2759567",
                                      "https://hdl.handle.net/11250/2738884",
                                      "https://hdl.handle.net/11250/2738802");
        var actualHandles = scraper.scrapeHandlesFromSuppliedExternalFile(new File(TEST_FILE_LOCATION));
        assertThat(actualHandles, is(equalTo(expectedHandles)));
    }

    private DublinCore generateDublinCoreWithTitle(String title) {
        var dcValue = new DcValue(Element.TITLE, Qualifier.NONE, title);
        return new DublinCore(List.of(dcValue));
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

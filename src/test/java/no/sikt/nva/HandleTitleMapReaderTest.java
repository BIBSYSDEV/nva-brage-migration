package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import org.junit.jupiter.api.Test;

public class HandleTitleMapReaderTest {

    @Test
    void shouldReadCsvFileAndOutputAMapWithTitlesAndHandles() {
        var handleFileName = "title_handles.csv";
        var handleTitleMapReader = new HandleTitleMapReader(handleFileName);
        var actualTitleHandleMap = handleTitleMapReader.readNveTitleAndHandlesPatch();
        assertThat(actualTitleHandleMap, allOf(aMapWithSize(3),
                                               hasEntry("Some super title", "https://hdl.handle.net/11250/2836938"),
                                               hasEntry("Another title", "https://hdl.handle.net/11250/2836939"),
                                               hasEntry("Some other amazing title",
                                                        "https://hdl.handle.net/11250/2836935")));
    }
}

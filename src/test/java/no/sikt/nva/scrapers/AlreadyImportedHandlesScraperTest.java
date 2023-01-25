package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AlreadyImportedHandlesScraperTest {

    public static final String TEST_FILE_LOCATION = "src/test/resources/handles.csv";

    @Test
    void shouldCreateListOfHandlesFromExternalHandleFile() {
        var expectedHandles = List.of("https://hdl.handle.net/11250/2759567",
                                      "https://hdl.handle.net/11250/2738884",
                                      "https://hdl.handle.net/11250/2738802");
        var actualHandles = AlreadyImportedHandlesScraper.scrapeHandlesFromSuppliedExternalFile(
            new File(TEST_FILE_LOCATION));
        assertThat(actualHandles, is(equalTo(expectedHandles)));
    }
}

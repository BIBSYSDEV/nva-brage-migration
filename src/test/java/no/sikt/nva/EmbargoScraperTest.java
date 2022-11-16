package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import no.sikt.nva.model.Embargo;
import org.junit.jupiter.api.Test;

public class EmbargoScraperTest {

    public static final String HANDLE = "https://hdl.handle.net/11250/2683076";
    public static final String FILENAME = "Simulated"
                                          + " precipitation fields with variance consistent interpolation.pdf";
    public static final String DATE = "2023-10-01";
    public static final String TEST_FILE_LOCATION = "src/test/resources/FileEmbargo.txt";

    @Test
    void shouldExtractEmbargoWithPdfFileOnly() throws IOException {
        var expectedEmbargo = new Embargo(HANDLE, FILENAME, DATE);
        var actualEmbargo = Objects.requireNonNull(EmbargoScraper.getEmbargoList(new File(TEST_FILE_LOCATION))).get(0);
        assertThat(actualEmbargo, is(equalTo(expectedEmbargo)));
    }
}

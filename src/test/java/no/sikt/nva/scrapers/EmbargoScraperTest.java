package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.sikt.nva.model.Embargo;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class EmbargoScraperTest {

    public static final String HANDLE = "https://hdl.handle.net/11250/2683076";
    public static final String FILENAME = "Simulated"
                                          + " precipitation fields with variance consistent interpolation.pdf";
    public static final String DATE = "2023-10-01";
    public static final String TEST_FILE_LOCATION = "src/test/resources/FileEmbargo.txt";

    @Test
    void shouldExtractEmbargoWithPdfFile() {
        var expectedEmbargo = new Embargo(HANDLE, FILENAME, DATE);
        var actualEmbargos =
            Objects.requireNonNull(EmbargoScraper.getEmbargoList(new File(TEST_FILE_LOCATION))).get(HANDLE);
        assertThat(actualEmbargos, hasItem(expectedEmbargo));
    }

    @Test
    void shouldLogNonDetectedEmbargos() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoList(new File(TEST_FILE_LOCATION));
        EmbargoParser.logNonEmbargosDetected(embargos);
        assertThat(appender.getMessages(), containsString("Embargo file not found: "));
    }

    @Test
    void shouldNotLogDetectedEmbargos() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoList(new File(TEST_FILE_LOCATION));
        var record = new Record();
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11250/2683076").getUri());
        record.setContentBundle(contentBundleWithFileNameFromEmbargo(
            "Simulated precipitation fields with variance consistent interpolation.pdf"));
        var updatedRecord = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos);
        assertThat(appender.getMessages(), not(containsString("Embargo file not found: ")));
        var actualContentFile = updatedRecord.getContentBundle().getContentFiles().get(0);
        assertThat(actualContentFile.getEmbargoDate(), is(notNullValue()));
    }

    private ResourceContent contentBundleWithFileNameFromEmbargo(String filename) {
        var contentFile = new ContentFile();
        contentFile.setFilename(filename);
        return new ResourceContent(List.of(contentFile));
    }

    @Test
    void shouldConvertEmbargoDateToInstant() {
        var embargo = new Embargo(HANDLE, FILENAME, DATE);

        assertThat(embargo.getDateAsInstant(), is(instanceOf(Instant.class)));
    }
}

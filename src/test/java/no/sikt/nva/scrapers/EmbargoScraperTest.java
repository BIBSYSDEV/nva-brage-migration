package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    public static final String EMPTY_EMBARGO_FILE = "src/test/resources/EmptyEmbargoFile.txt";
    public static final String TEST_FILE_LOCATION_V2 = "src/test/resources/FileEmbargoV2.txt";
    public static final String TEST_FILE_LOCATION_V3 = "src/test/resources/FileEmbargoV3.txt";
    public static final String EMBARGO_WITH_DISTANT_DATE_TXT = "src/test/resources/FileEmbargo_with_distant_date.txt";


    @Test
    void shouldIgnoreDefaultRowsOfEmbargoFile() {
        var actualEmbargos =
            Objects.requireNonNull(EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION))).get(HANDLE);
        assertThat(actualEmbargos, hasSize(3));
    }

    @Test
    void shouldConvertPrettifiedEmbargoFileToEmbargoes() {
        var expectedEmbargo = new Embargo(HANDLE, FILENAME, DATE);
        var actualEmbargos =
            Objects.requireNonNull(EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION_V3))).get(HANDLE);

        assertThat(actualEmbargos, hasSize(3));
        assertThat(actualEmbargos, hasItem(expectedEmbargo));
    }

    @Test
    void shouldExtractEmbargoWithPdfFile() {
        var expectedEmbargo = new Embargo(HANDLE, FILENAME, DATE);
        var actualEmbargos =
            Objects.requireNonNull(EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION))).get(HANDLE);
        assertThat(actualEmbargos, hasItem(expectedEmbargo));
    }

    @Test
    void shouldExtractEmbargosWithDifferentDelimiter() {
        var expectedEmbargo = new Embargo(HANDLE, FILENAME, DATE);
        var actualEmbargo = Objects.requireNonNull(EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION_V2)))
                                .get(HANDLE);
        assertThat(actualEmbargo, hasItem(expectedEmbargo));
    }

    @Test
    void shouldLogNonDetectedEmbargos() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION));
        EmbargoParser.logNonEmbargosDetected(embargos);
        assertThat(appender.getMessages(), containsString("Embargo file not found: "));
    }

    @Test
    void shouldNotLogDetectedEmbargos() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoes(new File(TEST_FILE_LOCATION));
        var record = new Record();
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11250/2683076").getUri());
        record.setContentBundle(contentBundleWithFileNameFromEmbargo(
            List.of("Simulated precipitation fields with variance consistent interpolation.pdf")));
        var updatedRecord = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos);
        assertThat(appender.getMessages(), not(containsString("Embargo file not found: ")));
        var actualContentFile = updatedRecord.getContentBundle().getContentFiles().get(0);
        assertThat(actualContentFile.getEmbargoDate(), is(notNullValue()));
    }

    @Test
    void shouldSetEmbargoOnMultipleContentFilesIfNecessary() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var embargos = EmbargoScraper.getEmbargoes(new File(EMBARGO_WITH_DISTANT_DATE_TXT));
        var record = new Record();
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11250/2683076").getUri());
        record.setContentBundle(contentBundleWithFileNameFromEmbargo(
            List.of("Simulated precipitation fields with variance consistent interpolation.pdf",
                    "My super secret file.pdf")));
        var updatedRecord = EmbargoParser.checkForEmbargoFromSuppliedEmbargoFile(record, embargos);
        assertThat(appender.getMessages(), not(containsString("Embargo file not found: ")));
        updatedRecord.getContentBundle()
            .getContentFiles()
            .forEach(actualContentFile ->
                         assertThat(actualContentFile.getEmbargoDate(), is(notNullValue())));
    }

    @Test
    void shouldCreateEmptyEmbargoListWhenEmptyEmbargoFile() {
        var embargos = EmbargoScraper.getEmbargoes(new File(EMPTY_EMBARGO_FILE));
        assertThat(embargos.size(), is(0));
    }

    @Test
    void shouldConvertEmbargoDateToInstant() {
        var embargo = new Embargo(HANDLE, FILENAME, DATE);
        assertThat(embargo.getDateAsInstant(), is(instanceOf(Instant.class)));
    }

    @Test
    void shouldConvertFiveDigitEmbargoYearToInstant() {
        var embargo = new Embargo(HANDLE, FILENAME, "20230-10-01");
        assertThat(embargo.getDateAsInstant(), is(instanceOf(Instant.class)));
    }

    private ResourceContent contentBundleWithFileNameFromEmbargo(List<String> filenames) {
        var contentfiles = filenames
                               .stream()
                               .map(this::createContentFile)
                               .collect(Collectors.toList());
        return new ResourceContent(contentfiles);
    }

    private ContentFile createContentFile(String filename) {
        var contentFile = new ContentFile();
        contentFile.setFilename(filename);
        return contentFile;
    }
}

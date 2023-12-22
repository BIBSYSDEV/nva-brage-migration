package no.sikt.nva.scrapers;

import static no.sikt.nva.UnisContentScrapingResult.ERROR_MESSAGE_EMPTY_SHEET;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_INVALID_EMBARGO_FORMAT;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.exceptions.InvalidUnisContentException;
import org.junit.jupiter.api.Test;

class ExcelScraperTest {

    private final String VALID_EXCEL_FILE = "src/test/resources/excelScraper/Metadata.xlsx";
    private final String INVALID_EXCEL_FILE = "src/test/resources/excelScraper/MetadataInvalid.xlsx";
    private final String EMPTY_EXCEL_FILE = "src/test/resources/excelScraper/EmptyFile.xlsx";
    private final String INVALID_EXCEL_FILE_EMBARGO_DATE = "src/test/resources/excelScraper"
                                                           + "/MetadataInvalidEmbargoDate.xlsx";

    @Test
    void shouldNotThrowExceptionWhenValidExcelFileIsProvided() {
        assertDoesNotThrow(() -> ExcelScraper.toRecords(VALID_EXCEL_FILE));
    }

    @Test
    void shouldCreateAListOfRecordsWhenMultipleRowsAreGiven()
        throws ExcelException, InvalidUnisContentException, IOException, URISyntaxException {
        var results = ExcelScraper.toRecords(VALID_EXCEL_FILE);
        assertNotNull(results);
        assertThat(results.size(), is(greaterThan(1)));
    }

    @Test
    void shouldThrowExceptionWhenEmptyExcelFileIsProvided() {
        assertThrows(ExcelException.class,
                     () -> ExcelScraper.toRecords(EMPTY_EXCEL_FILE),
                     ERROR_MESSAGE_EMPTY_SHEET);
    }

    @Test
    void shouldThrowExceptionWhenInvalidExcelFileIsProvided() {
        assertThrows(ExcelException.class, () -> ExcelScraper.toRecords(INVALID_EXCEL_FILE));
    }

    @Test
    void shouldThrowExceptionWhenExcelFileWithInvalidEmbargoDateFormatIsProvided() {
        assertThrows(ExcelException.class,
                     () -> ExcelScraper.toRecords(INVALID_EXCEL_FILE_EMBARGO_DATE),
                     ERROR_MESSAGE_INVALID_EMBARGO_FORMAT);
    }

    @Test
    void shouldThrowExceptionWhenExcelPathIsInvalid() {
        assertThrows(NoSuchFileException.class, () -> ExcelScraper.toRecords(randomString()));
    }
}
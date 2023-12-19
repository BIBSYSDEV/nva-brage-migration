package no.sikt.nva.scrapers;

import static no.sikt.nva.UnisContent.ERROR_MESSAGE_INVALID_EMBARGO_FORMAT;
import static no.sikt.nva.UnisContentScrapingResult.ERROR_MESSAGE_EMPTY_SHEET;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.file.NoSuchFileException;
import no.sikt.nva.exceptions.ExcelException;
import org.junit.jupiter.api.Test;

class ExcelScraperTest {

    private final String VALID_EXCEL_FILE = "src/test/resources/excelScraper/Metadata.xlsx";
    private final String INVALID_EXCEL_FILE = "src/test/resources/excelScraper/MetadataInvalid.xlsx";
    private final String EMPTY_EXCEL_FILE = "src/test/resources/excelScraper/EmptyFile.xlsx";
    private final String INVALID_EXCEL_FILE_EMBARGO_DATE = "src/test/resources/excelScraper"
                                                           + "/MetadataInvalidEmbargoDate.xlsx";

    @Test
    void shouldNotThrowExceptionWhenValidExcelFileIsProvided() {
        assertDoesNotThrow(() -> ExcelScraper.toRecord(VALID_EXCEL_FILE));
    }

    @Test
    void shouldThrowExceptionWhenEmptyExcelFileIsProvided() {
        assertThrows(ExcelException.class,
                     () -> ExcelScraper.toRecord(EMPTY_EXCEL_FILE),
                     ERROR_MESSAGE_EMPTY_SHEET);
    }

    @Test
    void shouldThrowExceptionWhenInvalidExcelFileIsProvided() {
        assertThrows(ExcelException.class, () -> ExcelScraper.toRecord(INVALID_EXCEL_FILE));
    }

    @Test
    void shouldThrowExceptionWhenExcelFileWithInvalidEmbargoDateFormatIsProvided() {
        assertThrows(ExcelException.class,
                     () -> ExcelScraper.toRecord(INVALID_EXCEL_FILE_EMBARGO_DATE),
                     ERROR_MESSAGE_INVALID_EMBARGO_FORMAT);
    }

    @Test
    void shouldThrowExceptionWhenExcelPathIsInvalid() {
        assertThrows(NoSuchFileException.class, () -> ExcelScraper.toRecord(randomString()));
    }
}
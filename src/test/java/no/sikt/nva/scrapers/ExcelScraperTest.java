package no.sikt.nva.scrapers;

import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_INVALID_EMBARGO_FORMAT;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.FileNotFoundException;
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
                     EMPTY_EXCEL_FILE);
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
        assertThrows(FileNotFoundException.class, () -> ExcelScraper.toRecord(randomString()));
    }

    @Test
    void shouldProduceRecordWhenValidExcelFileIsProvided() {
    }

    @Test
    void shouldThrowExceptionIfExcelFileReferencesFilesNotProvided() {

    }

    @Test
    void shouldThrowExceptionIfNotAllFilesAreReferencedInExcelFile() {

    }

    @Test
    void shouldThrowExceptionIfContentFileNotFound() {

    }

    @Test
    void shouldSetPublicationContextToCristin() {

    }

    @Test
    void shouldAssignRandomUUIDAsContentId() {

    }

    @Test
    void shouldAssignAcceptedVersion() {

    }

    @Test
    void shouldAssignPublishedVersion() {

    }

    @Test
    void shouldSetLicenseToValidURI() {

    }

    @Test
    void shouldParseEmbargoDateForMultipleFilesForSameCristinPost() {

    }
}
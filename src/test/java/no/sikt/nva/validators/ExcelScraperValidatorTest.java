package no.sikt.nva.validators;

import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_EMPTY_SHEET;
import static no.sikt.nva.validators.ExcelTestingUtils.createWorkbook;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.scrapers.ExcelScraper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExcelScraperValidatorTest {
    private Object[] VALID_HEADERS;
    private Object[] VALID_ROW;

    @BeforeEach
    void init() {
        VALID_HEADERS = ExcelHeaderValidator.VALID_HEADERS.clone();
        VALID_ROW = ExcelTestingUtils.VALID_ROW;
    }

    @Test
    void shouldNotThrowExceptionWhenValidExcelDataIsProvided() {
        var validExcelData = new Object[][] {
            VALID_HEADERS,
            VALID_ROW
        };
        assertDoesNotThrow(() -> ExcelScraperValidator.validate(createWorkbook(validExcelData)));
    }

    @Test
    void shouldThrowExceptionWhenNoData() {
        var invalidExcelData = new Object[][] {
            {},
            {}
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_EMPTY_SHEET);
    }
}
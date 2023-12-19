package no.sikt.nva;

import static no.sikt.nva.UnisContent.CRISTIN_ID_COLUMN;
import static no.sikt.nva.UnisContent.TITLE_COLUMN;
import static no.sikt.nva.UnisContentScrapingResult.ERROR_MESSAGE_EMPTY_SHEET;
import static no.sikt.nva.UnisContentScrapingResult.ERROR_MESSAGE_INVALID_HEADERS;
import static no.sikt.nva.validators.ExcelTestingUtils.addValue;
import static no.sikt.nva.validators.ExcelTestingUtils.createWorkbook;
import static no.sikt.nva.validators.ExcelTestingUtils.removeValue;
import static no.sikt.nva.validators.ExcelTestingUtils.substituteValue;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.*;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.validators.ExcelTestingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnisContentScrapingResultTest {
    private Object[] VALID_HEADERS;
    private Object[] VALID_ROW;

    @BeforeEach
    void init() {
        VALID_HEADERS = UnisContentScrapingResult.VALID_HEADERS.clone();
        VALID_ROW = ExcelTestingUtils.VALID_ROW;
    }

    @Test
    void shouldNotThrowExceptionWhenValidExcelDataIsProvided() {
        var validExcelData = new Object[][] {
            VALID_HEADERS,
            VALID_ROW
        };
        assertDoesNotThrow(() -> UnisContentScrapingResult.fromWorkbook(createWorkbook(validExcelData)));
    }

    @Test
    void shouldThrowExceptionWhenNoData() {
        var invalidExcelData = new Object[][] {
            {},
            {}
        };
        assertThrows(ExcelException.class,
                     () -> UnisContentScrapingResult.fromWorkbook(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_EMPTY_SHEET);
    }

    @Test
    void shouldThrowExceptionWhenHeaderIsMissing() {
        var headersWithoutTitleHeader = removeValue(VALID_HEADERS, TITLE_COLUMN);
        var invalidExcelData = new Object[][] {
            headersWithoutTitleHeader
        };
        assertThrows(ExcelException.class,
                     () -> UnisContentScrapingResult.fromWorkbook(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }

    @Test
    void shouldThrowExceptionWhenExtraHeaderIsPresent() {
        var headersWithoutTitleHeader = addValue(VALID_HEADERS, randomString());
        var invalidExcelData = new Object[][] {
            headersWithoutTitleHeader
        };
        assertThrows(ExcelException.class,
                     () -> UnisContentScrapingResult.fromWorkbook(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }

    @Test
    void shouldThrowExceptionIfUnknownColumnHeaderExists() {
        var headersWithInvalidCristinIdName = substituteValue(VALID_HEADERS, randomString(), CRISTIN_ID_COLUMN);
        var invalidExcelData = new Object[][] {
            headersWithInvalidCristinIdName
        };
        assertThrows(ExcelException.class,
                     () -> UnisContentScrapingResult.fromWorkbook(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }
}
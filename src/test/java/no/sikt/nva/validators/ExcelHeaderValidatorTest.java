package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.ExcelScraper.CRISTIN_ID_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.TITLE_COLUMN;
import static no.sikt.nva.validators.ExcelHeaderValidator.ERROR_MESSAGE_INVALID_HEADERS;
import static no.sikt.nva.validators.ExcelTestingUtils.createWorkbook;
import static no.sikt.nva.validators.ExcelTestingUtils.removeValue;
import static no.sikt.nva.validators.ExcelTestingUtils.substituteValue;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import no.sikt.nva.exceptions.ExcelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExcelHeaderValidatorTest {
    private Object[] VALID_HEADERS;

    @BeforeEach
    void init() {
        VALID_HEADERS = ExcelHeaderValidator.VALID_HEADERS.clone();
    }

    @Test
    void shouldNotThrowExceptionWhenValidHeadersAreProvided() {
        Object[][] invalidExcelData = {
            VALID_HEADERS
        };
        assertDoesNotThrow(() -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)));
    }

    @Test
    void shouldThrowExceptionWhenHeaderIsMissing() {
        var headersWithoutTitleHeader = removeValue(VALID_HEADERS, TITLE_COLUMN);
        Object[][] invalidExcelData = {
            headersWithoutTitleHeader
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }

    @Test
    void shouldThrowExceptionIfUnknownColumnHeaderExists() {
        var headersWithInvalidCristinIdName = substituteValue(VALID_HEADERS, randomString(), CRISTIN_ID_COLUMN);
        Object[][] invalidExcelData = {
            headersWithInvalidCristinIdName
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }
}
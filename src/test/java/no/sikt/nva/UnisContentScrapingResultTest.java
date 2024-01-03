package no.sikt.nva;

import static no.sikt.nva.UnisContentScrapingResult.ERROR_MESSAGE_EMPTY_SHEET;
import static no.sikt.nva.UnisContentScrapingResult.ERROR_MESSAGE_INVALID_HEADERS;
import static no.sikt.nva.validators.ExcelTestingUtils.addValue;
import static no.sikt.nva.validators.ExcelTestingUtils.createWorkbook;
import static no.sikt.nva.validators.ExcelTestingUtils.removeValue;
import static no.sikt.nva.validators.ExcelTestingUtils.substituteValue;
import static no.sikt.nva.validators.UnisContentValidator.CRISTIN_ID_COLUMN;
import static no.sikt.nva.validators.UnisContentValidator.TITLE_COLUMN;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.net.URISyntaxException;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.exceptions.InvalidUnisContentException;
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
        var validExcelData = new Object[][]{
            VALID_HEADERS,
            VALID_ROW
        };
        assertDoesNotThrow(() -> UnisContentScrapingResult.fromWorkbook(createWorkbook(validExcelData)));
    }

    @Test
    void shouldCreateAListOfRecordsWhenMultipleRowsAreGiven()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {
        var validExcelData = new Object[][]{
            VALID_HEADERS,
            VALID_ROW,
            VALID_ROW
        };
        var results = UnisContentScrapingResult.fromWorkbook(createWorkbook(validExcelData)).getResults();
        assertNotNull(results);
        assertThat(results.size(), is(greaterThan(0)));
    }

    @Test
    void shouldThrowExceptionWhenNoData() {
        var invalidExcelData = new Object[][]{
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
        var invalidExcelData = new Object[][]{
            headersWithoutTitleHeader
        };
        assertThrows(ExcelException.class,
                     () -> UnisContentScrapingResult.fromWorkbook(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }

    @Test
    void shouldThrowExceptionWhenExtraHeaderIsPresent() {
        var headersWithoutTitleHeader = addValue(VALID_HEADERS, randomString());
        var invalidExcelData = new Object[][]{
            headersWithoutTitleHeader
        };
        assertThrows(ExcelException.class,
                     () -> UnisContentScrapingResult.fromWorkbook(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }

    @Test
    void shouldThrowExceptionIfUnknownColumnHeaderExists() {
        var headersWithInvalidCristinIdName = substituteValue(VALID_HEADERS, randomString(), CRISTIN_ID_COLUMN);
        var invalidExcelData = new Object[][]{
            headersWithInvalidCristinIdName
        };
        assertThrows(ExcelException.class,
                     () -> UnisContentScrapingResult.fromWorkbook(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }

    @Test
    void shouldAppendContentFilesToSameRecordWhenCristinIdIsIdentical()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {

        var firstCristinId = randomInteger();
        var firstRowWithDuplicateCristinId = substituteValue(VALID_ROW, firstCristinId, CRISTIN_ID_COLUMN);
        var secondRowWithDuplicateCristinId = substituteValue(VALID_ROW, firstCristinId, CRISTIN_ID_COLUMN);

        var secondCristinId = randomInteger();
        var thirdRowWithUniqueCristinId = substituteValue(VALID_ROW, secondCristinId, CRISTIN_ID_COLUMN);

        var validExcelData = new Object[][]{
            VALID_HEADERS,
            firstRowWithDuplicateCristinId,
            secondRowWithDuplicateCristinId,
            thirdRowWithUniqueCristinId
        };

        var records = UnisContentScrapingResult.fromWorkbook(createWorkbook(validExcelData)).getResults();
        assertNotNull(records);
        assertThat(records.size(), is(equalTo(2)));

        var recordWithMultipleFiles = records.stream()
                                          .filter(
                                              record -> Integer.toString(firstCristinId).equals(record.getCristinId()))
                                          .findAny()
                                          .orElse(null);
        assertNotNull(recordWithMultipleFiles.getContentBundle().getContentFiles());
        assertThat(recordWithMultipleFiles.getContentBundle().getContentFiles().size(), is(equalTo(2)));

        var recordWithOneFile = records.stream()
                                    .filter(record -> Integer.toString(secondCristinId).equals(record.getCristinId()))
                                    .findAny()
                                    .orElse(null);
        assertNotNull(recordWithOneFile.getContentBundle().getContentFiles());
        assertThat(recordWithOneFile.getContentBundle().getContentFiles().size(), is(equalTo(1)));
    }
}
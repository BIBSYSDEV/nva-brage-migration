package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.ExcelScraper.CRISTIN_ID_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.EMBARGO_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.FILENAME_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.LICENCE_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.TITLE_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.VERSION_COLUMN;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_INVALID_CRISTIN_ID;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_INVALID_EMBARGO_FORMAT;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_INVALID_HEADERS;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_INVALID_LICENCE;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_INVALID_VERSION;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_MISSING_CELL_VALUE;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_MISSING_CRISTIN_ID;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_MISSING_FILENAME;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_MISSING_LICENCE;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_MISSING_TITLE;
import static no.sikt.nva.validators.ExcelScraperValidator.ERROR_MESSAGE_MISSING_VERSION;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.record.BrageVersion;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.scrapers.ExcelScraper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExcelScraperValidatorTest {

    public static final String INVALID_DATE_FORMAT_EXAMPLE = "03/19/2024";
    private final Object[] VALID_ROW = {
        123,
        "En tittel",
        BrageVersion.ACCEPTED.getValue(),
        Date.from(Instant.now()),
        BrageLicense.CC_BY.getValue(),
        "Fil.pdf"
    };
    private final String EMPTY_STRING = "";
    private Object[] VALID_HEADERS;

    @BeforeEach
    void init() {
        VALID_HEADERS = ExcelScraperValidator.VALID_HEADERS.clone();
    }

    @Test
    void shouldNotThrowExceptionWhenValidExcelDataIsProvided() {
        Object[][] validExcelData = {
            VALID_HEADERS,
            VALID_ROW
        };
        assertDoesNotThrow(() -> ExcelScraper.toRecord(createWorkbook(validExcelData)));
    }

    @Test
    void shouldThrowExceptionIfUnknownColumnHeaderExists() {
        var headersWithInvalidCristinIdName = substituteValue(VALID_HEADERS, randomString(), CRISTIN_ID_COLUMN);
        Object[][] invalidExcelData = {
            headersWithInvalidCristinIdName,
            VALID_ROW
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_HEADERS);
    }

    @Test
    void shouldThrowExceptionIfRowDoesNotHaveMandatoryFieldsFilled() {
        var rowWithoutVersion = substituteValue(VALID_ROW, EMPTY_STRING, VERSION_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithoutVersion
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_MISSING_CELL_VALUE);
    }

    @Test
    void shouldThrowExceptionIfRowHasTooManyCellsFilled() {
        var rowWithExtraCell = addValue(VALID_ROW, randomString());
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithExtraCell
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
    }

    @Test
    void shouldThrowExceptionIfRowHasTooFewCellsFilled() {
        var rowWithoutCell = removeValue(VALID_ROW, VERSION_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithoutCell
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
    }

    @Test
    void shouldThrowExceptionWhenCristinIdIsMissing() {
        var rowWithEmptyCristinId = substituteValue(VALID_ROW, EMPTY_STRING, CRISTIN_ID_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithEmptyCristinId
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_MISSING_CRISTIN_ID);
    }

    @Test
    void shouldThrowExceptionWhenCristinIdIsNonNumeric() {
        var rowWithInvalidCristinId = substituteValue(VALID_ROW, randomString(), CRISTIN_ID_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithInvalidCristinId
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_CRISTIN_ID);
    }

    @Test
    void shouldThrowExceptionWhenMissingTitle() {
        var rowWithMissingTitle = substituteValue(VALID_ROW, EMPTY_STRING, TITLE_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithMissingTitle
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_MISSING_TITLE);
    }

    @Test
    void shouldThrowExceptionWhenMissingVersion() {
        var rowWithMissingVersion = substituteValue(VALID_ROW, EMPTY_STRING, VERSION_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithMissingVersion
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_MISSING_VERSION);
    }

    @Test
    void shouldThrowExceptionWhenInvalidVersion() {
        var rowWithInvalidVersion = substituteValue(VALID_ROW, randomString(), VERSION_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithInvalidVersion
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_VERSION);
    }

    @Test
    void shouldNotThrowExceptionWhenVersionIsValidEnum() {
        var rowWithValidVersionAkseptert = substituteValue(VALID_ROW,
                                                           BrageVersion.ACCEPTED.getValue(),
                                                           VERSION_COLUMN);
        Object[][] validExcelDataAkseptert = {
            VALID_HEADERS,
            rowWithValidVersionAkseptert
        };
        assertDoesNotThrow(() -> ExcelScraper.toRecord(createWorkbook(validExcelDataAkseptert)));

        var rowWithValidVersionPublisert = substituteValue(VALID_ROW,
                                                           BrageVersion.PUBLISHED.getValue(),
                                                           VERSION_COLUMN);
        Object[][] validExcelDataPublisert = {
            VALID_HEADERS,
            rowWithValidVersionPublisert
        };
        assertDoesNotThrow(() -> ExcelScraper.toRecord(createWorkbook(validExcelDataPublisert)));
    }

    @Test
    void shouldAllowEmptyEmbargoDate() {
        var rowWithoutEmbargo = substituteValue(VALID_ROW, EMPTY_STRING, EMBARGO_COLUMN);
        Object[][] validExcelData = {
            VALID_HEADERS,
            rowWithoutEmbargo
        };
        assertDoesNotThrow(() -> ExcelScraper.toRecord(createWorkbook(validExcelData)));
    }

    @Test
    void shouldThrowExceptionWhenEmbargoDateHasInvalidFormat() {
        var rowWithInvalidEmbargo = substituteValue(VALID_ROW, INVALID_DATE_FORMAT_EXAMPLE, EMBARGO_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithInvalidEmbargo
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraper.toRecord(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_EMBARGO_FORMAT);
    }

    @Test
    void shouldThrowExceptionWhenMissingLicence() {
        var rowWithMissingLicence = substituteValue(VALID_ROW, EMPTY_STRING, LICENCE_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithMissingLicence
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_MISSING_LICENCE);
    }

    @Test
    void shouldThrowExceptionWhenInvalidLicence() {
        var rowWithInvalidLicence = substituteValue(VALID_ROW, randomString(), LICENCE_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithInvalidLicence
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_INVALID_LICENCE);
    }

    @Test
    void shouldThrowExceptionWhenMissingFilename() {
        var rowWithMissingFilename = substituteValue(VALID_ROW, EMPTY_STRING, FILENAME_COLUMN);
        Object[][] invalidExcelData = {
            VALID_HEADERS,
            rowWithMissingFilename
        };
        assertThrows(ExcelException.class,
                     () -> ExcelScraperValidator.validate(createWorkbook(invalidExcelData)),
                     ERROR_MESSAGE_MISSING_FILENAME);
    }

    private Workbook createWorkbook(Object[][] bookData) {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Metadata-test");

        var createHelper = workbook.getCreationHelper();
        var styleDate = workbook.createCellStyle();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

        int rowCount = 0;

        for (Object[] aBook : bookData) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            for (Object field : aBook) {
                Cell cell = row.createCell(columnCount++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                } else if (field instanceof Date) {
                    cell.setCellStyle(styleDate);
                    cell.setCellValue((Date) field);
                }
            }
        }

        return workbook;
    }

    private Object[] substituteValue(Object[] list, Object newValue, int index) {
        list[index] = newValue;
        return list;
    }

    private Object[] addValue(Object[] list, Object newValue) {
        var arrayWithExtraValue = Arrays.copyOf(list, list.length + 1);
        arrayWithExtraValue[arrayWithExtraValue.length - 1] = newValue;
        return arrayWithExtraValue;
    }

    private Object[] removeValue(Object[] list, int index) {
        var newArray = new ArrayList<>(List.of(list));
        newArray.remove(index);
        return newArray.toArray();
    }
}
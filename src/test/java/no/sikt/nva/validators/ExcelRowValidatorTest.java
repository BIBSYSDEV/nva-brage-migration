package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.ExcelScraper.CRISTIN_ID_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.EMBARGO_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.FILENAME_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.LICENCE_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.TITLE_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.VERSION_COLUMN;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_INVALID_CRISTIN_ID;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_INVALID_EMBARGO_FORMAT;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_INVALID_LICENCE;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_INVALID_VERSION;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_MISSING_CRISTIN_ID;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_MISSING_FILENAME;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_MISSING_LICENCE;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_MISSING_TITLE;
import static no.sikt.nva.validators.ExcelRowValidator.ERROR_MESSAGE_MISSING_VERSION;
import static no.sikt.nva.validators.ExcelTestingUtils.EMPTY_STRING;
import static no.sikt.nva.validators.ExcelTestingUtils.INVALID_DATE_FORMAT_EXAMPLE;
import static no.sikt.nva.validators.ExcelTestingUtils.addValue;
import static no.sikt.nva.validators.ExcelTestingUtils.createWorkbook;
import static no.sikt.nva.validators.ExcelTestingUtils.removeValue;
import static no.sikt.nva.validators.ExcelTestingUtils.substituteValue;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import no.sikt.nva.brage.migration.common.model.record.BrageVersion;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.scrapers.ExcelScraper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExcelRowValidatorTest {
    private Object[] VALID_HEADERS;
    private Object[] VALID_ROW;

    @BeforeEach
    void init() {
        VALID_HEADERS = ExcelHeaderValidator.VALID_HEADERS.clone();
        VALID_ROW = ExcelTestingUtils.VALID_ROW.clone();
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
}
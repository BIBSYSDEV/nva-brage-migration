package no.sikt.nva;

import static no.sikt.nva.UnisContent.CRISTIN_ID_COLUMN;
import static no.sikt.nva.UnisContent.EMBARGO_COLUMN;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_INVALID_CRISTIN_ID;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_INVALID_EMBARGO_FORMAT;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_INVALID_LICENCE;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_INVALID_PUBLISHER_AUTHORITY;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_MISSING_CRISTIN_ID;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_MISSING_FILENAME;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_MISSING_LICENCE;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_MISSING_PUBLISHER_AUTHORITY;
import static no.sikt.nva.UnisContent.ERROR_MESSAGE_MISSING_TITLE;
import static no.sikt.nva.UnisContent.FILENAME_COLUMN;
import static no.sikt.nva.UnisContent.LICENCE_COLUMN;
import static no.sikt.nva.UnisContent.PUBLISHER_AUTHORITY_COLUMN;
import static no.sikt.nva.UnisContent.TITLE_COLUMN;
import static no.sikt.nva.validators.ExcelTestingUtils.EMPTY_STRING;
import static no.sikt.nva.validators.ExcelTestingUtils.INVALID_DATE_FORMAT_EXAMPLE;
import static no.sikt.nva.validators.ExcelTestingUtils.addValue;
import static no.sikt.nva.validators.ExcelTestingUtils.createRow;
import static no.sikt.nva.validators.ExcelTestingUtils.removeValue;
import static no.sikt.nva.validators.ExcelTestingUtils.substituteValue;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.*;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthorityEnum;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.validators.ExcelTestingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnisContentTest {
    private Object[] VALID_ROW;

    @BeforeEach
    void init() {
        VALID_ROW = ExcelTestingUtils.VALID_ROW.clone();
    }

    @Test
    void shouldThrowExceptionIfRowHasTooManyCellsFilled() {
        var rowWithExtraCell = addValue(VALID_ROW, randomString());
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithExtraCell)),
                     ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
    }

    @Test
    void shouldThrowExceptionIfRowHasTooFewCellsFilled() {
        var rowWithoutCell = removeValue(VALID_ROW, PUBLISHER_AUTHORITY_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithoutCell)),
                     ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
    }

    @Test
    void shouldThrowExceptionWhenCristinIdIsMissing() {
        var rowWithEmptyCristinId = substituteValue(VALID_ROW, EMPTY_STRING, CRISTIN_ID_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithEmptyCristinId)),
                     ERROR_MESSAGE_MISSING_CRISTIN_ID);
    }

    @Test
    void shouldThrowExceptionWhenCristinIdIsNonNumeric() {
        var rowWithInvalidCristinId = substituteValue(VALID_ROW, randomString(), CRISTIN_ID_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithInvalidCristinId)),
                     ERROR_MESSAGE_INVALID_CRISTIN_ID);
    }

    @Test
    void shouldThrowExceptionWhenMissingTitle() {
        var rowWithMissingTitle = substituteValue(VALID_ROW, EMPTY_STRING, TITLE_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithMissingTitle)),
                     ERROR_MESSAGE_MISSING_TITLE);
    }

    @Test
    void shouldThrowExceptionWhenMissingVersion() {
        var rowWithMissingVersion = substituteValue(VALID_ROW, EMPTY_STRING, PUBLISHER_AUTHORITY_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithMissingVersion)),
                     ERROR_MESSAGE_MISSING_PUBLISHER_AUTHORITY);
    }

    @Test
    void shouldThrowExceptionWhenInvalidVersion() {
        var rowWithInvalidVersion = substituteValue(VALID_ROW, randomString(), PUBLISHER_AUTHORITY_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithInvalidVersion)),
                     ERROR_MESSAGE_INVALID_PUBLISHER_AUTHORITY);
    }

    @Test
    void shouldNotThrowExceptionWhenVersionIsValidEnum() {
        var rowWithValidVersionAkseptert = substituteValue(VALID_ROW,
                                                           PublisherAuthorityEnum.ACCEPTED.getValue(),
                                                           PUBLISHER_AUTHORITY_COLUMN);
        assertDoesNotThrow(() -> UnisContent.fromRow(createRow(rowWithValidVersionAkseptert)));

        var rowWithValidVersionPublisert = substituteValue(VALID_ROW,
                                                           PublisherAuthorityEnum.PUBLISHED.getValue(),
                                                           PUBLISHER_AUTHORITY_COLUMN);
        assertDoesNotThrow(() -> UnisContent.fromRow(createRow(rowWithValidVersionPublisert)));
    }

    @Test
    void shouldAllowEmptyEmbargoDate() {
        var rowWithoutEmbargo = substituteValue(VALID_ROW, EMPTY_STRING, EMBARGO_COLUMN);
        assertDoesNotThrow(() -> UnisContent.fromRow(createRow(rowWithoutEmbargo)));
    }

    @Test
    void shouldThrowExceptionWhenEmbargoDateHasInvalidFormat() {
        var rowWithInvalidEmbargo = substituteValue(VALID_ROW, INVALID_DATE_FORMAT_EXAMPLE, EMBARGO_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithInvalidEmbargo)),
                     ERROR_MESSAGE_INVALID_EMBARGO_FORMAT);
    }

    @Test
    void shouldThrowExceptionWhenMissingLicence() {
        var rowWithMissingLicence = substituteValue(VALID_ROW, EMPTY_STRING, LICENCE_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithMissingLicence)),
                     ERROR_MESSAGE_MISSING_LICENCE);
    }

    @Test
    void shouldThrowExceptionWhenInvalidLicence() {
        var rowWithInvalidLicence = substituteValue(VALID_ROW, randomString(), LICENCE_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithInvalidLicence)),
                     ERROR_MESSAGE_INVALID_LICENCE);
    }

    @Test
    void shouldThrowExceptionWhenMissingFilename() {
        var rowWithMissingFilename = substituteValue(VALID_ROW, EMPTY_STRING, FILENAME_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithMissingFilename)),
                     ERROR_MESSAGE_MISSING_FILENAME);
    }
}
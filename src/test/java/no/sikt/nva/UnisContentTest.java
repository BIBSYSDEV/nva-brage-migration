package no.sikt.nva;

import static no.sikt.nva.UnisContent.DUMMY_HANDLE_THAT_EXIST_FOR_PROCESSING_UNIS;
import static no.sikt.nva.UnisContent.UNIS_ID;
import static no.sikt.nva.brage.migration.common.model.NvaType.CRISTIN_RECORD;
import static no.sikt.nva.validators.ExcelTestingUtils.EMPTY_STRING;
import static no.sikt.nva.validators.ExcelTestingUtils.INVALID_DATE_FORMAT_EXAMPLE;
import static no.sikt.nva.validators.ExcelTestingUtils.addValue;
import static no.sikt.nva.validators.ExcelTestingUtils.createRow;
import static no.sikt.nva.validators.ExcelTestingUtils.removeValue;
import static no.sikt.nva.validators.ExcelTestingUtils.substituteValue;
import static no.sikt.nva.validators.UnisContentValidator.CRISTIN_ID_COLUMN;
import static no.sikt.nva.validators.UnisContentValidator.EMBARGO_COLUMN;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_INVALID_CRISTIN_ID;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_INVALID_EMBARGO_FORMAT;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_INVALID_LICENSE;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_INVALID_PUBLISHER_AUTHORITY;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_MISSING_CRISTIN_ID;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_MISSING_FILENAME;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_MISSING_LICENSE;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_MISSING_PUBLISHER_AUTHORITY;
import static no.sikt.nva.validators.UnisContentValidator.ERROR_MESSAGE_MISSING_TITLE;
import static no.sikt.nva.validators.UnisContentValidator.FILENAME_COLUMN;
import static no.sikt.nva.validators.UnisContentValidator.LICENSE_COLUMN;
import static no.sikt.nva.validators.UnisContentValidator.PUBLISHER_AUTHORITY_COLUMN;
import static no.sikt.nva.validators.UnisContentValidator.TITLE_COLUMN;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthorityEnum;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent.BundleType;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.exceptions.InvalidUnisContentException;
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
        var rowWithMissingLicence = substituteValue(VALID_ROW, EMPTY_STRING, LICENSE_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithMissingLicence)),
                     ERROR_MESSAGE_MISSING_LICENSE);
    }

    @Test
    void shouldThrowExceptionWhenInvalidLicence() {
        var rowWithInvalidLicence = substituteValue(VALID_ROW, randomString(), LICENSE_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithInvalidLicence)),
                     ERROR_MESSAGE_INVALID_LICENSE);
    }

    @Test
    void shouldThrowExceptionWhenMissingFilename() {
        var rowWithMissingFilename = substituteValue(VALID_ROW, EMPTY_STRING, FILENAME_COLUMN);
        assertThrows(ExcelException.class,
                     () -> UnisContent.fromRow(createRow(rowWithMissingFilename)),
                     ERROR_MESSAGE_MISSING_FILENAME);
    }

    @Test
    void shouldAddCristinIdToRecordIdWhenCreatingRecord()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {
        var record = UnisContent.fromRow(createRow(VALID_ROW)).toRecord();
        assertThat(record.getId().toString(), containsString(VALID_ROW[CRISTIN_ID_COLUMN].toString()));
    }

    @Test
    void shouldAddDummyHandlerToRecordIdWhenCreatingRecord()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {
        var record = UnisContent.fromRow(createRow(VALID_ROW)).toRecord();
        assertThat(record.getId().toString(), containsString(DUMMY_HANDLE_THAT_EXIST_FOR_PROCESSING_UNIS));
    }

    @Test
    void shouldSetUnisAsResourceOwnerWhenCreatingRecord()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {
        var record = UnisContent.fromRow(createRow(VALID_ROW)).toRecord();
        assertThat(record.getResourceOwner().getOwner(), is(equalTo(UNIS_ID)));
    }

    @Test
    void shouldSetPublisherAuthorityWhenCreatingRecord()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {
        var record = UnisContent.fromRow(createRow(VALID_ROW)).toRecord();

        var pubAuthString = VALID_ROW[PUBLISHER_AUTHORITY_COLUMN].toString();
        var pubAuth = PublisherAuthorityEnum.fromValue(pubAuthString);

        assertNotNull(pubAuth);
        assertThat(record.getPublisherAuthority(), is(equalTo(pubAuth.toPublisherAuthority())));
    }

    @Test
    void shouldSetCristinAsTypeWhenCreatingRecord()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {
        var record = UnisContent.fromRow(createRow(VALID_ROW)).toRecord();
        assertThat(record.getType().getNva(), is(equalTo(CRISTIN_RECORD.getValue())));
    }

    @Test
    void shouldCreateContentFileWithValuesWhenCreatingRecord()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {
        var record = UnisContent.fromRow(createRow(VALID_ROW)).toRecord();

        var contentBundle = record.getContentBundle();
        assertNotNull(contentBundle);
        assertNotNull(contentBundle.getContentFiles());
        assertThat(contentBundle.getContentFiles().size(), is(greaterThan(0)));

        var file = contentBundle.getContentFiles().get(0);
        assertNotNull(file);
        assertThat(file.getIdentifier(), is(notNullValue()));
        assertThat(file.getFilename(), is(equalTo(VALID_ROW[FILENAME_COLUMN])));
        assertThat(file.getBundleType(), is(equalTo(BundleType.ORIGINAL)));
        assertThat(file.getLicense().toString(), is(equalTo(VALID_ROW[LICENSE_COLUMN])));

        var expectedEmbargo = (Date) VALID_ROW[EMBARGO_COLUMN];
        assertThat(file.getEmbargoDate(), is(equalTo(expectedEmbargo.toInstant())));
    }

    @Test
    void shouldCreateContentFileWithoutEmbargoWhenEmbargoIsMissing()
        throws ExcelException, InvalidUnisContentException, URISyntaxException {
        var rowWithoutEmbargo = substituteValue(VALID_ROW, EMPTY_STRING, EMBARGO_COLUMN);
        var record = UnisContent.fromRow(createRow(rowWithoutEmbargo)).toRecord();

        var contentBundle = record.getContentBundle();
        assertNotNull(contentBundle);
        assertNotNull(contentBundle.getContentFiles());
        assertThat(contentBundle.getContentFiles().size(), is(greaterThan(0)));

        var file = contentBundle.getContentFiles().get(0);
        assertNotNull(file);
        assertNull(file.getEmbargoDate());
    }
}
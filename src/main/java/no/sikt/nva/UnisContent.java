package no.sikt.nva;

import static no.sikt.nva.UnisContentScrapingResult.VALID_HEADERS;
import java.util.Date;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthorityEnum;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.exceptions.ExcelException;
import nva.commons.core.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

public final class UnisContent {
    public static final int FIRST_COLUMN = 0;
    public static final int CRISTIN_ID_COLUMN = 0;
    public static final int TITLE_COLUMN = 1;
    public static final int PUBLISHER_AUTHORITY_COLUMN = 2;
    public static final int EMBARGO_COLUMN = 3;
    public static final int LICENCE_COLUMN = 4;
    public static final int FILENAME_COLUMN = 5;
    public static final String ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS = "Invalid number of columns";
    public static final String ERROR_MESSAGE_MISSING_FIRST_COLUMN_VALUE = "Missing first column value";
    public static final String ERROR_MESSAGE_MISSING_CRISTIN_ID = "Missing Cristin Id";
    public static final String ERROR_MESSAGE_INVALID_CRISTIN_ID = "Invalid Cristin Id";
    public static final String ERROR_MESSAGE_MISSING_TITLE = "Missing Title";
    public static final String ERROR_MESSAGE_MISSING_PUBLISHER_AUTHORITY = "Missing Publisher Authority";
    public static final String ERROR_MESSAGE_INVALID_PUBLISHER_AUTHORITY = "Invalid Publisher Authority value";
    public static final String ERROR_MESSAGE_INVALID_EMBARGO_FORMAT = "Invalid Embargo format";
    public static final String ERROR_MESSAGE_MISSING_LICENCE = "Missing Licence";
    public static final String ERROR_MESSAGE_INVALID_LICENCE = "Invalid Licence value";
    public static final String ERROR_MESSAGE_MISSING_FILENAME = "Missing Filename";

    private int cristinId;
    private String title;
    private PublisherAuthorityEnum publisherAuthority;
    private Date embargo;
    private BrageLicense licence;
    private String filename;

    private UnisContent() {
    }

    public static UnisContent fromRow(Row row) throws ExcelException {
        if (row.getFirstCellNum() != FIRST_COLUMN) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_FIRST_COLUMN_VALUE);
        }

        if (getNumberOfColumns(row) != VALID_HEADERS.length) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
        }

        var unisContent = new UnisContent();
        unisContent.setCristinId(row);
        unisContent.setTitle(row);
        unisContent.setPublisherAuthority(row);
        unisContent.setEmbargo(row);
        unisContent.setLicence(row);
        unisContent.setFilename(row);
        return unisContent;
    }

    public int getCristinId() {
        return cristinId;
    }

    public void setCristinId(Row row) throws ExcelException {
        var cristinIdCell = row.getCell(CRISTIN_ID_COLUMN);
        if (isEmpty(cristinIdCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_CRISTIN_ID);
        } else if (!CellType.NUMERIC.equals(cristinIdCell.getCellType())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_CRISTIN_ID);
        }
        this.cristinId = (int) cristinIdCell.getNumericCellValue();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(Row row) throws ExcelException {
        var titleCell = row.getCell(TITLE_COLUMN);
        if (isEmpty(titleCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_TITLE);
        }
        this.title = titleCell.getStringCellValue();
    }

    public PublisherAuthorityEnum getPublisherAuthority() {
        return publisherAuthority;
    }

    public void setPublisherAuthority(Row row) throws ExcelException {
        var publisherAuthorityCell = row.getCell(PUBLISHER_AUTHORITY_COLUMN);
        if (isEmpty(publisherAuthorityCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_PUBLISHER_AUTHORITY);
        } else if (!CellType.STRING.equals(publisherAuthorityCell.getCellType())
                   || !PublisherAuthorityEnum.isValid(publisherAuthorityCell.getStringCellValue())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_PUBLISHER_AUTHORITY);
        }
        this.publisherAuthority = PublisherAuthorityEnum.fromValue(publisherAuthorityCell.getStringCellValue());
    }

    public Date getEmbargo() {
        return embargo;
    }

    public void setEmbargo(Row row) throws ExcelException {
        var embargoCell = row.getCell(EMBARGO_COLUMN);
        if (!isEmpty(embargoCell)) {
            if (!CellType.NUMERIC.equals(embargoCell.getCellType())
                 || !DateUtil.isCellDateFormatted(embargoCell)) {
                throw new ExcelException(ERROR_MESSAGE_INVALID_EMBARGO_FORMAT);
            }
            this.embargo = embargoCell.getDateCellValue();
        }
    }

    public BrageLicense getLicence() {
        return licence;
    }

    public void setLicence(Row row) throws ExcelException {
        var licenceCell = row.getCell(LICENCE_COLUMN);
        if (isEmpty(licenceCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_LICENCE);
        } else if (!CellType.STRING.equals(licenceCell.getCellType())
                   || !BrageLicense.isValid(licenceCell.getStringCellValue())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_LICENCE);
        }
        this.licence = BrageLicense.fromValue(licenceCell.getStringCellValue());
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(Row row) throws ExcelException {
        var filenameCell = row.getCell(FILENAME_COLUMN);
        if (isEmpty(filenameCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_FILENAME);
        }
        this.filename = filenameCell.getStringCellValue();
    }

    public Record toRecord() {
        return new Record();
    }

    private static boolean isEmpty(Cell cell) {
        if (cell == null || CellType.BLANK.equals(cell.getCellType())) {
            return true;
        }

        return cell.getCellType().equals(CellType.STRING)
               && StringUtils.isBlank(cell.getStringCellValue());
    }

    private static int getNumberOfColumns(Row row) {
        return row.getLastCellNum() - row.getFirstCellNum();
    }
}

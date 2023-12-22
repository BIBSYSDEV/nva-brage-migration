package no.sikt.nva.validators;

import static no.sikt.nva.UnisContentScrapingResult.VALID_HEADERS;
import java.time.Instant;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthorityEnum;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.exceptions.ExcelException;
import nva.commons.core.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

public class UnisContentValidator {
    public static final int FIRST_COLUMN = 0;
    public static final int CRISTIN_ID_COLUMN = 0;
    public static final int TITLE_COLUMN = 1;
    public static final int PUBLISHER_AUTHORITY_COLUMN = 2;
    public static final int EMBARGO_COLUMN = 3;
    public static final int LICENSE_COLUMN = 4;
    public static final int FILENAME_COLUMN = 5;
    public static final String ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS = "Invalid number of columns";
    public static final String ERROR_MESSAGE_MISSING_FIRST_COLUMN_VALUE = "Missing first column value";
    public static final String ERROR_MESSAGE_MISSING_CRISTIN_ID = "Missing Cristin Id";
    public static final String ERROR_MESSAGE_INVALID_CRISTIN_ID = "Invalid Cristin Id";
    public static final String ERROR_MESSAGE_MISSING_TITLE = "Missing Title";
    public static final String ERROR_MESSAGE_MISSING_PUBLISHER_AUTHORITY = "Missing Publisher Authority";
    public static final String ERROR_MESSAGE_INVALID_PUBLISHER_AUTHORITY = "Invalid Publisher Authority value";
    public static final String ERROR_MESSAGE_INVALID_EMBARGO_FORMAT = "Invalid Embargo format";
    public static final String ERROR_MESSAGE_MISSING_LICENSE = "Missing Licence";
    public static final String ERROR_MESSAGE_INVALID_LICENSE = "Invalid Licence value";
    public static final String ERROR_MESSAGE_MISSING_FILENAME = "Missing Filename";

    public static void validateRow(Row row) throws ExcelException {
        if (row.getFirstCellNum() != FIRST_COLUMN) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_FIRST_COLUMN_VALUE);
        }

        if (getNumberOfColumns(row) != VALID_HEADERS.length) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
        }
    }

    public static int validateAndExtractCristinId(Row row) throws ExcelException {
        var cristinIdCell = row.getCell(CRISTIN_ID_COLUMN);
        if (isEmpty(cristinIdCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_CRISTIN_ID);
        } else if (!CellType.NUMERIC.equals(cristinIdCell.getCellType())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_CRISTIN_ID);
        }
        return (int) cristinIdCell.getNumericCellValue();
    }

    public static String validateAndExtractTitle(Row row) throws ExcelException {
        var titleCell = row.getCell(TITLE_COLUMN);
        if (isEmpty(titleCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_TITLE);
        }
        return titleCell.getStringCellValue();
    }

    public static PublisherAuthorityEnum validateAndExtractPublisherAuthority(Row row) throws ExcelException {
        var publisherAuthorityCell = row.getCell(PUBLISHER_AUTHORITY_COLUMN);
        if (isEmpty(publisherAuthorityCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_PUBLISHER_AUTHORITY);
        } else if (!CellType.STRING.equals(publisherAuthorityCell.getCellType())
                   || !PublisherAuthorityEnum.isValid(publisherAuthorityCell.getStringCellValue())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_PUBLISHER_AUTHORITY);
        }
        return PublisherAuthorityEnum.fromValue(publisherAuthorityCell.getStringCellValue());
    }

    public static Instant validateAndExtractEmbargo(Row row) throws ExcelException {
        var embargoCell = row.getCell(EMBARGO_COLUMN);
        if (!isEmpty(embargoCell)) {
            if (!CellType.NUMERIC.equals(embargoCell.getCellType())
                || !DateUtil.isCellDateFormatted(embargoCell)) {
                throw new ExcelException(ERROR_MESSAGE_INVALID_EMBARGO_FORMAT);
            }
            return embargoCell.getDateCellValue().toInstant();
        }
        return null;
    }

    public static BrageLicense validateAndExtractLicense(Row row) throws ExcelException {
        var licenseCell = row.getCell(LICENSE_COLUMN);
        if (isEmpty(licenseCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_LICENSE);
        } else if (!CellType.STRING.equals(licenseCell.getCellType())
                   || !BrageLicense.isValid(licenseCell.getStringCellValue())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_LICENSE);
        }
        return BrageLicense.fromValue(licenseCell.getStringCellValue());
    }

    public static String validateAndExtractFilename(Row row) throws ExcelException {
        var filenameCell = row.getCell(FILENAME_COLUMN);
        if (isEmpty(filenameCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_FILENAME);
        }
        return filenameCell.getStringCellValue();
    }

    private static int getNumberOfColumns(Row row) {
        return row.getLastCellNum() - row.getFirstCellNum();
    }

    private static boolean isEmpty(Cell cell) {
        if (cell == null || CellType.BLANK.equals(cell.getCellType())) {
            return true;
        }

        return cell.getCellType().equals(CellType.STRING)
               && StringUtils.isBlank(cell.getStringCellValue());
    }
}

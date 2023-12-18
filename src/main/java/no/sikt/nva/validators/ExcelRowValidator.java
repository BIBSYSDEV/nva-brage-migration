package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.ExcelScraper.CRISTIN_ID_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.EMBARGO_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.FILENAME_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.FIRST_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.LICENCE_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.TITLE_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.VERSION_COLUMN;
import static no.sikt.nva.validators.ExcelHeaderValidator.VALID_HEADERS;
import no.sikt.nva.brage.migration.common.model.record.BrageVersion;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.exceptions.ExcelException;
import nva.commons.core.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

public class ExcelRowValidator {
    public static final String ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS = "Invalid number of columns";
    public static final String ERROR_MESSAGE_MISSING_FIRST_COLUMN_VALUE = "Missing first column value";
    public static final String ERROR_MESSAGE_MISSING_CRISTIN_ID = "Missing Cristin Id";
    public static final String ERROR_MESSAGE_INVALID_CRISTIN_ID = "Invalid Cristin Id";
    public static final String ERROR_MESSAGE_MISSING_TITLE = "Missing Title";
    public static final String ERROR_MESSAGE_MISSING_VERSION = "Missing Version";
    public static final String ERROR_MESSAGE_INVALID_VERSION = "Invalid Version value";
    public static final String ERROR_MESSAGE_INVALID_EMBARGO_FORMAT = "Invalid Embargo format";
    public static final String ERROR_MESSAGE_MISSING_LICENCE = "Missing Licence";
    public static final String ERROR_MESSAGE_INVALID_LICENCE = "Invalid Licence value";
    public static final String ERROR_MESSAGE_MISSING_FILENAME = "Missing Filename";

    public static void validateRow(Row row) throws ExcelException {
        if (row.getFirstCellNum() != FIRST_COLUMN) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_FIRST_COLUMN_VALUE);
        }

        if (getNumberOfColumns(row) != VALID_HEADERS.length) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
        }

        validateCristinId(row);
        validateTitle(row);
        validateVersion(row);
        validateEmbargo(row);
        validateLicence(row);
        validateFilename(row);
    }

    private static void validateCristinId(Row row) throws ExcelException {
        var cristinIdCell = row.getCell(CRISTIN_ID_COLUMN);
        if (isEmpty(cristinIdCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_CRISTIN_ID);
        } else if (cristinIdCell.getCellType() != CellType.NUMERIC) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_CRISTIN_ID);
        }
    }

    private static void validateTitle(Row row) throws ExcelException {
        if (isEmpty(row.getCell(TITLE_COLUMN))) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_TITLE);
        }
    }

    private static void validateVersion(Row row) throws ExcelException {
        var versionCell = row.getCell(VERSION_COLUMN);
        if (isEmpty(versionCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_VERSION);
        } else if (versionCell.getCellType() != CellType.STRING
                   || !BrageVersion.isValid(versionCell.getStringCellValue())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_VERSION);
        }
    }

    private static void validateEmbargo(Row row) throws ExcelException {
        var embargoCell = row.getCell(EMBARGO_COLUMN);
        if (!isEmpty(embargoCell)
            && (embargoCell.getCellType() != CellType.NUMERIC
                || !DateUtil.isCellDateFormatted(embargoCell))) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_EMBARGO_FORMAT);
        }
    }

    private static void validateLicence(Row row) throws ExcelException {
        var licenceCell = row.getCell(LICENCE_COLUMN);
        if (isEmpty(licenceCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_LICENCE);
        } else if (licenceCell.getCellType() != CellType.STRING
                   || !BrageLicense.isValid(licenceCell.getStringCellValue())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_LICENCE);
        }
    }

    private static void validateFilename(Row row) throws ExcelException {
        if (isEmpty(row.getCell(FILENAME_COLUMN))) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_FILENAME);
        }
    }

    private static int getNumberOfColumns(Row row) {
        return row.getLastCellNum() - row.getFirstCellNum();
    }

    private static boolean isEmpty(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return true;
        }

        return cell.getCellType() == CellType.STRING
               && StringUtils.isBlank(cell.getStringCellValue());
    }
}

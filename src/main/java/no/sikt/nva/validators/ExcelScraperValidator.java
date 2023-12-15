package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.ExcelScraper.CRISTIN_ID_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.EMBARGO_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.FILENAME_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.FIRST_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.FIRST_ROW;
import static no.sikt.nva.scrapers.ExcelScraper.LICENCE_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.SECOND_ROW;
import static no.sikt.nva.scrapers.ExcelScraper.TITLE_COLUMN;
import static no.sikt.nva.scrapers.ExcelScraper.VERSION_COLUMN;
import java.util.ArrayList;
import java.util.Arrays;
import no.sikt.nva.brage.migration.common.model.record.BrageVersion;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import no.sikt.nva.exceptions.ExcelException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelScraperValidator {

    public static final String[] VALID_HEADERS = {"Post", "Tittel", "Versjon", "Embargo", "Lisens", "Filnavn"};
    public static final String ERROR_MESSAGE_EMPTY_FILE = "Empty file";
    public static final String ERROR_MESSAGE_MISSING_FIRST_ROW = "Missing first row";
    public static final String ERROR_MESSAGE_INVALID_HEADER = "Invalid header value";
    public static final String ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS = "Invalid number of columns";
    public static final String ERROR_MESSAGE_MISSING_FIRST_COLUMN_VALUE = "Missing first column value";
    public static final String ERROR_MESSAGE_INVALID_HEADERS = "Invalid header value(s)";
    public static final String ERROR_MESSAGE_MISSING_CELL_VALUE = "Missing cell value";
    public static final String ERROR_MESSAGE_MISSING_CRISTIN_ID = "Missing Cristin Id";
    public static final String ERROR_MESSAGE_INVALID_CRISTIN_ID = "Invalid Cristin Id";
    public static final String ERROR_MESSAGE_MISSING_TITLE = "Missing Title";
    public static final String ERROR_MESSAGE_MISSING_VERSION = "Missing Version";
    public static final String ERROR_MESSAGE_INVALID_VERSION = "Invalid Version value";
    public static final String ERROR_MESSAGE_INVALID_EMBARGO_FORMAT = "Invalid Embargo format";
    public static final String ERROR_MESSAGE_MISSING_LICENCE = "Missing Licence";
    public static final String ERROR_MESSAGE_INVALID_LICENCE = "Invalid Licence value";
    public static final String ERROR_MESSAGE_MISSING_FILENAME = "Missing Filename";

    public static void validate(Workbook workbook) throws ExcelException {
        Sheet sheet = workbook.getSheetAt(0);

        if (sheet.getLastRowNum() == 0 && sheet.getRow(0) == null) {
            throw new ExcelException(ERROR_MESSAGE_EMPTY_FILE);
        }

        var firstRow = sheet.getRow(FIRST_ROW);
        if (firstRow == null) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_FIRST_ROW);
        }

        var validHeaders = Arrays.asList(VALID_HEADERS);
        var actualHeaders = new ArrayList<String>();
        for (int i = FIRST_COLUMN; i < firstRow.getLastCellNum(); i++) {
            var headerCell = firstRow.getCell(i);
            if (headerCell == null || headerCell.getCellType() != CellType.STRING) {
                throw new ExcelException(ERROR_MESSAGE_INVALID_HEADER);
            }
            actualHeaders.add(headerCell.getStringCellValue().trim());
        }

        if (actualHeaders.size() != validHeaders.size()) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
        }

        if (!actualHeaders.equals(validHeaders)) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_HEADERS);
        }

        for (int i = SECOND_ROW; i <= getLastNonEmptyRowIndex(sheet); i++) {
            Row currentRow = sheet.getRow(i);
            validateRow(currentRow);
        }
    }

    public static void validateRow(Row row) throws ExcelException {
        if (row.getFirstCellNum() != FIRST_COLUMN) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_FIRST_COLUMN_VALUE);
        }

        if (getNumberOfColumns(row) != VALID_HEADERS.length) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_NUMBER_OF_COLUMNS);
        }

        var cristinIdCell = row.getCell(CRISTIN_ID_COLUMN);
        if (isEmpty(cristinIdCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_CRISTIN_ID);
        } else if (cristinIdCell.getCellType() != CellType.NUMERIC) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_CRISTIN_ID);
        }

        if (isEmpty(row.getCell(TITLE_COLUMN))) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_TITLE);
        }

        var versionCell = row.getCell(VERSION_COLUMN);
        if (isEmpty(versionCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_VERSION);
        } else if (versionCell.getCellType() != CellType.STRING
                   || !BrageVersion.isValid(versionCell.getStringCellValue())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_VERSION);
        }

        var embargoCell = row.getCell(EMBARGO_COLUMN);
        if (!isEmpty(embargoCell)
            && (embargoCell.getCellType() != CellType.NUMERIC
                || !DateUtil.isCellDateFormatted(embargoCell))) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_EMBARGO_FORMAT);
        }

        var licenceCell = row.getCell(LICENCE_COLUMN);
        if (isEmpty(licenceCell)) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_LICENCE);
        } else if (licenceCell.getCellType() != CellType.STRING
                   || !BrageLicense.isValid(licenceCell.getStringCellValue())) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_LICENCE);
        }

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

        return cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty();
    }

    private static int getLastNonEmptyRowIndex(Sheet sheet) {
        var lastRowIndex = sheet.getLastRowNum();
        for (var i = lastRowIndex; i >= FIRST_ROW; i--) {
            var row = sheet.getRow(i);
            if (row != null && row.getLastCellNum() != -1) {
                return i;
            }
        }
        return -1;
    }
}

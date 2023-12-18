package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.ExcelScraper.FIRST_ROW;
import static no.sikt.nva.scrapers.ExcelScraper.SECOND_ROW;
import static no.sikt.nva.validators.ExcelHeaderValidator.validateHeaders;
import static no.sikt.nva.validators.ExcelRowValidator.validateRow;
import no.sikt.nva.exceptions.ExcelException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelScraperValidator {
    public static final String ERROR_MESSAGE_EMPTY_SHEET = "Empty sheet";

    public static void validate(Workbook workbook) throws ExcelException {
        Sheet sheet = workbook.getSheetAt(0);

        validateSheet(sheet);
        validateHeaders(sheet.getRow(FIRST_ROW));

        for (int i = SECOND_ROW; i <= getLastNonEmptyRowIndex(sheet); i++) {
            validateRow(sheet.getRow(i));
        }
    }

    public static void validateSheet(Sheet sheet) throws ExcelException {
        if (sheet.getLastRowNum() == FIRST_ROW && sheet.getRow(FIRST_ROW) == null) {
            throw new ExcelException(ERROR_MESSAGE_EMPTY_SHEET);
        }
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

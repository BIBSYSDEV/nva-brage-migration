package no.sikt.nva;

import static java.util.Objects.nonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.exceptions.ExcelException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public final class UnisContentScrapingResult {
    public static final String ERROR_MESSAGE_EMPTY_SHEET = "Empty sheet";
    public static final String[] VALID_HEADERS = {"Post", "Tittel", "Versjon", "Embargo", "Lisens", "Filnavn"};
    public static final String ERROR_MESSAGE_MISSING_HEADER_ROW = "Missing header row";
    public static final String ERROR_MESSAGE_INVALID_HEADER = "Invalid header value";
    public static final String ERROR_MESSAGE_INVALID_HEADERS = "Invalid header value(s)";
    public static final int FIRST_ROW = 0;
    public static final int SECOND_ROW = 1;
    public static final int FIRST_COLUMN = 0;
    private List<Record> results;

    private UnisContentScrapingResult() {}

    public static UnisContentScrapingResult fromWorkbook(Workbook workbook) throws ExcelException {
        Sheet sheet = workbook.getSheetAt(0);

        validateSheet(sheet);
        validateHeaders(sheet.getRow(FIRST_ROW));

        var results = new ArrayList<Record>();
        for (int i = SECOND_ROW; i <= getLastNonEmptyRowIndex(sheet); i++) {
            var unisContent = UnisContent.fromRow(sheet.getRow(i));
            results.add(unisContent.toRecord());
        }

        UnisContentScrapingResult result = new UnisContentScrapingResult();
        result.setResults(results);
        return result;
    }

    public void setResults(List<Record> results) {
        this.results = results;
    }

    public List<Record> getResults() {
        return this.results;
    }

    private static void validateSheet(Sheet sheet) throws ExcelException {
        if (sheet.getLastRowNum() == FIRST_ROW && sheet.getRow(FIRST_ROW) == null) {
            throw new ExcelException(ERROR_MESSAGE_EMPTY_SHEET);
        }
    }

    private static void validateHeaders(Row headerRow) throws ExcelException {
        if (headerRow == null) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_HEADER_ROW);
        }

        var actualHeaders = new ArrayList<String>();
        for (int i = FIRST_COLUMN; i < headerRow.getLastCellNum(); i++) {
            var headerCell = headerRow.getCell(i);
            if (headerCell == null || !CellType.STRING.equals(headerCell.getCellType())) {
                throw new ExcelException(ERROR_MESSAGE_INVALID_HEADER);
            }
            actualHeaders.add(headerCell.getStringCellValue().trim());
        }

        var validHeaders = Arrays.asList(VALID_HEADERS);
        if (!actualHeaders.equals(validHeaders)) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_HEADERS);
        }
    }

    private static int getLastNonEmptyRowIndex(Sheet sheet) {
        var lastRowIndex = sheet.getLastRowNum();
        for (var i = lastRowIndex; i >= FIRST_ROW; i--) {
            var row = sheet.getRow(i);
            if (nonNull(row) && row.getLastCellNum() != -1) {
                return i;
            }
        }
        return -1;
    }
}

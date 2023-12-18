package no.sikt.nva.validators;

import static no.sikt.nva.scrapers.ExcelScraper.FIRST_COLUMN;
import java.util.ArrayList;
import java.util.Arrays;
import no.sikt.nva.exceptions.ExcelException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class ExcelHeaderValidator {
    public static final String[] VALID_HEADERS = {"Post", "Tittel", "Versjon", "Embargo", "Lisens", "Filnavn"};
    public static final String ERROR_MESSAGE_MISSING_HEADER_ROW = "Missing header row";
    public static final String ERROR_MESSAGE_INVALID_HEADER = "Invalid header value";
    public static final String ERROR_MESSAGE_INVALID_HEADERS = "Invalid header value(s)";

    public static void validateHeaders(Row headerRow) throws ExcelException {
        if (headerRow == null) {
            throw new ExcelException(ERROR_MESSAGE_MISSING_HEADER_ROW);
        }

        var actualHeaders = new ArrayList<String>();
        for (int i = FIRST_COLUMN; i < headerRow.getLastCellNum(); i++) {
            var headerCell = headerRow.getCell(i);
            if (headerCell == null || headerCell.getCellType() != CellType.STRING) {
                throw new ExcelException(ERROR_MESSAGE_INVALID_HEADER);
            }
            actualHeaders.add(headerCell.getStringCellValue().trim());
        }

        var validHeaders = Arrays.asList(VALID_HEADERS);
        if (!actualHeaders.equals(validHeaders)) {
            throw new ExcelException(ERROR_MESSAGE_INVALID_HEADERS);
        }
    }
}

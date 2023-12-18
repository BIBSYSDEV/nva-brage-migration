package no.sikt.nva.scrapers;

import static no.sikt.nva.validators.ExcelScraperValidator.validate;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.exceptions.ExcelException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelScraper {

    public static final int FIRST_ROW = 0;
    public static final int SECOND_ROW = 1;
    public static final int FIRST_COLUMN = 0;
    public static final int CRISTIN_ID_COLUMN = 0;
    public static final int TITLE_COLUMN = 1;
    public static final int VERSION_COLUMN = 2;
    public static final int EMBARGO_COLUMN = 3;
    public static final int LICENCE_COLUMN = 4;
    public static final int FILENAME_COLUMN = 5;

    public static Record toRecord(String excelFilePath) throws ExcelException, IOException {
        try (InputStream excelFileStream = Files.newInputStream(Paths.get(excelFilePath))) {
            return toRecord(new XSSFWorkbook(excelFileStream));
        }
    }

    public static Record toRecord(Workbook workbook) throws ExcelException {
        validate(workbook);

        // TODO: Below is just temporary code. Should map to Record

        Sheet datatypeSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = datatypeSheet.iterator();

        while (iterator.hasNext()) {
            Row currentRow = iterator.next();
            Iterator<Cell> cellIterator = currentRow.iterator();

            while (cellIterator.hasNext()) {

                Cell currentCell = cellIterator.next();
                if (currentCell.getCellType() == CellType.STRING) {
                    System.out.print(currentCell.getStringCellValue() + "--");
                } else if (currentCell.getCellType() == CellType.NUMERIC) {
                    System.out.print(currentCell.getNumericCellValue() + "--");
                }
            }
            System.out.println();
        }

        return new Record();
    }
}

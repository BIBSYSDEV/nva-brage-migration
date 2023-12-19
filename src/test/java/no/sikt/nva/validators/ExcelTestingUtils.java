package no.sikt.nva.validators;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.record.PublisherAuthorityEnum;
import no.sikt.nva.brage.migration.common.model.record.license.BrageLicense;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelTestingUtils {
    public static final Object[] VALID_ROW = {
        123,
        "En tittel",
        PublisherAuthorityEnum.ACCEPTED.getValue(),
        Date.from(Instant.now()),
        BrageLicense.CC_BY.getValue(),
        "Fil.pdf"
    };
    public static final String INVALID_DATE_FORMAT_EXAMPLE = "03/19/2024";
    public static final String EMPTY_STRING = "";

    public static Workbook createWorkbook(Object[][] data) {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Metadata-test");

        var createHelper = workbook.getCreationHelper();
        var styleDate = workbook.createCellStyle();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

        int rowCount = 0;

        for (Object[] rowData : data) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;

            for (Object field : rowData) {
                Cell cell = row.createCell(columnCount++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                } else if (field instanceof Date) {
                    cell.setCellStyle(styleDate);
                    cell.setCellValue((Date) field);
                }
            }
        }

        return workbook;
    }

    public static Row createRow(Object[] data) {
        var workbookData = new Object[][] {data};
        var workbook = createWorkbook(workbookData);
        return workbook.getSheetAt(0).getRow(0);
    }

    public static Object[] substituteValue(Object[] list, Object newValue, int index) {
        var newArray = new ArrayList<>(List.of(list));
        newArray.set(index, newValue);
        return newArray.toArray();
    }

    public static Object[] addValue(Object[] list, Object newValue) {
        var arrayWithExtraValue = Arrays.copyOf(list, list.length + 1);
        arrayWithExtraValue[arrayWithExtraValue.length - 1] = newValue;
        return arrayWithExtraValue;
    }

    public static Object[] removeValue(Object[] list, int index) {
        var newArray = new ArrayList<>(List.of(list));
        newArray.remove(index);
        return newArray.toArray();
    }
}

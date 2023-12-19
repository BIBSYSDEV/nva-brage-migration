package no.sikt.nva.scrapers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import no.sikt.nva.UnisContentScrapingResult;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.exceptions.ExcelException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelScraper {

    public static List<Record> toRecord(String excelFilePath) throws ExcelException, IOException {
        try (InputStream excelFileStream = Files.newInputStream(Paths.get(excelFilePath))) {
            return toRecord(new XSSFWorkbook(excelFileStream));
        }
    }

    public static List<Record> toRecord(Workbook workbook) throws ExcelException {
        return UnisContentScrapingResult.fromWorkbook(workbook).getResults();
    }
}

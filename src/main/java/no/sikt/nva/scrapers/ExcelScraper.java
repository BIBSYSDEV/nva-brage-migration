package no.sikt.nva.scrapers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import no.sikt.nva.UnisContentScrapingResult;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.exceptions.ExcelException;
import no.sikt.nva.exceptions.InvalidUnisContentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelScraper {

    public static List<Record> toRecords(String excelFilePath)
        throws ExcelException, IOException, InvalidUnisContentException {
        try (InputStream excelFileStream = Files.newInputStream(Paths.get(excelFilePath))) {
            return toRecords(new XSSFWorkbook(excelFileStream));
        }
    }

    public static List<Record> toRecords(Workbook workbook) throws ExcelException, InvalidUnisContentException {
        return UnisContentScrapingResult.fromWorkbook(workbook).getResults();
    }
}

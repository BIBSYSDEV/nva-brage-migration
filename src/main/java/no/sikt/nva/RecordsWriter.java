package no.sikt.nva;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.exceptions.RecordsWriterException;
import no.unit.nva.commons.json.JsonUtils;
import org.apache.jena.ext.com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RecordsWriter {

    public static final String WRITING_TO_JSON_FILE_HAS_FAILED = "WRITING TO JSON FILE HAS FAILED IN BUNDLE =";
    private static final Logger logger = LoggerFactory.getLogger(RecordsWriter.class);
    private static int counter;

    private RecordsWriter() {
    }

    public static int getCounter() {
        return counter;
    }

    public static void writeRecordsToFile(String fileName, List<Record> records) {
        try {
            createFileWithRecords(fileName, records);
            countRecords(records);
        } catch (RecordsWriterException e) {
            logger.warn(e.getMessage());
        }
    }

    private static void countRecords(List<Record> records) {
        if (nonNull(records) && !records.isEmpty()) {
            counter += records.size();
        }
    }

    public static String convertMultipleRecordsToJsonString(List<Record> records) throws JsonProcessingException {
        return JsonUtils.dtoObjectMapper.writeValueAsString(records);
    }

    private static void createFileWithRecords(String fileName, List<Record> records) {
        writeRecords(fileName, records);
    }

    private static void writeRecords(String fileName, List<Record> records) {
        if (nonNull(records)) {
            try (var fileWriter = Files.newWriter(new File(fileName), StandardCharsets.UTF_8)) {
                fileWriter.write(convertMultipleRecordsToJsonString(records));
            } catch (Exception e) {
                throw new RecordsWriterException(WRITING_TO_JSON_FILE_HAS_FAILED, fileName);
            }
        }
    }
}

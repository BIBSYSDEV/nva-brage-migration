package no.sikt.nva;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private static final List<Record> recordList = new ArrayList<>();
    private static int counter;

    @SuppressWarnings("PMD.AssignmentToNonFinalStatic")
    private RecordsWriter() {
        counter = 0;
    }

    public static int getCounter() {
        return counter;
    }

    public static List<Record> getRecordList() {
        return recordList;
    }

    public static String getNumberOfUniqueRecords() {
        var numberOfUniqueRecords = recordList.stream().map(Record::getId).distinct().count();
        return String.valueOf(numberOfUniqueRecords);
    }

    public static void writeRecordsToFile(String fileName, List<Record> records) {
        try {
            removeIdenticalElementFromRecords(records);
            createFileWithRecords(fileName, records);
            counter += records.size();
            if (!records.isEmpty()) {
                recordList.addAll(records);
            }
        } catch (RecordsWriterException e) {
            logger.warn(e.getMessage());
        }
    }

    public static String convertRecordsToJsonString(List<Record> records) throws JsonProcessingException {
        return JsonUtils.dtoObjectMapper.writeValueAsString(records);
    }

    private static void removeIdenticalElementFromRecords(List<Record> records) {
        for (Record record : records) {
            if (recordList.contains(record)) {
                records.remove(record);
                logger.info("RECORD WAS REMOVED FROM IMPORT BECAUSE OF DUPLICATE: " + record.getId());
            }
        }
    }

    private static void createFileWithRecords(String fileName, List<Record> records) {
        writeRecords(fileName, records);
    }

    private static void writeRecords(String fileName, List<Record> records) {
        try (var fileWriter = Files.newWriter(new File(fileName), StandardCharsets.UTF_8)) {
            fileWriter.write(convertRecordsToJsonString(records));
        } catch (Exception e) {
            throw new RecordsWriterException(WRITING_TO_JSON_FILE_HAS_FAILED, fileName);
        }
    }
}

package no.sikt.nva.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.FileWriter;
import java.util.List;
import no.sikt.nva.Main;
import no.sikt.nva.exceptions.RecordsWriterException;
import no.sikt.nva.model.record.Record;
import no.unit.nva.commons.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordsWriter {

    public static final String WRITING_RECORDS_HAS_FAILED = "Writing records to file has failed";

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public void writeRecordsToFile(String fileName, List<Record> records) {
        try {
            createFileWithRecords(fileName, records);
        } catch (RecordsWriterException e) {
            logger.warn(e.getMessage());
        }
    }

    public String convertRecordsToJsonString(List<Record> records) throws JsonProcessingException {
        return JsonUtils.dtoObjectMapper.writeValueAsString(records);
    }

    @SuppressWarnings("PMD.AvoidFileStream")
    private void createFileWithRecords(String fileName, List<Record> records) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(convertRecordsToJsonString(records));
        } catch (Exception e) {
            throw new RecordsWriterException(WRITING_RECORDS_HAS_FAILED, e);
        }
    }
}

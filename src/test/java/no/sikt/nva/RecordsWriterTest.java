package no.sikt.nva;

import static no.sikt.nva.model.RecordsWriter.WRITING_RECORDS_HAS_FAILED;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.model.RecordsWriter;
import no.sikt.nva.model.record.Record;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class RecordsWriterTest {

    RecordsWriter recordsWriter = new RecordsWriter();

    @Test
    void shouldThrowExceptionIfWritingToFileFails() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        recordsWriter.writeRecordsToFile("", generateListOfRecords());
        assertThat(appender.getMessages(), containsString(WRITING_RECORDS_HAS_FAILED));
    }

    private List<Record> generateListOfRecords() {
        return IntStream.range(1, 2).boxed().map(item -> createRecord()).collect(Collectors.toList());
    }

    private Record createRecord() {
        List<String> authors = new ArrayList<>();
        authors.add(randomString());
        authors.add(randomString());

        Record record = new Record();
        record.setId(randomUri());
        record.setType(randomString());
        record.setLicense(randomString());
        record.setLanguage(randomString());
        record.setAuthors(authors);

        return record;
    }
}

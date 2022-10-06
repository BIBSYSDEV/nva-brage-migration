package no.sikt.nva;

import static no.sikt.nva.RecordsWriter.WRITING_RECORDS_HAS_FAILED;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import java.util.ArrayList;
import java.util.List;
import no.sikt.nva.model.record.Record;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class RecordsWriterTest {

    public static final String INVALID_FILE_NAME = "";
    RecordsWriter recordsWriter = new RecordsWriter();

    @Test
    void shouldLogIfWritingToFileFails() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        recordsWriter.writeRecordsToFile(INVALID_FILE_NAME, List.of(createRecord()));
        assertThat(appender.getMessages(), containsString(WRITING_RECORDS_HAS_FAILED));
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

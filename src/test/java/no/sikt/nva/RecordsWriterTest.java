package no.sikt.nva;

import static no.sikt.nva.RecordsWriter.WRITING_TO_JSON_FILE_HAS_FAILED;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import java.util.Collections;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.scrapers.TypeMapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class RecordsWriterTest {

    public static final String INVALID_FILE_NAME = "";

    @Test
    void shouldLogIfWritingToFileFails() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        RecordsWriter.writeRecordsToFile(INVALID_FILE_NAME, List.of(createRecord()));
        assertThat(appender.getMessages(), containsString(WRITING_TO_JSON_FILE_HAS_FAILED));
    }

    private Record createRecord() {
        var record = new Record();
        record.setId(randomUri());
        var types = Collections.singleton("Research report");
        record.setType(new Type(types, TypeMapper.convertBrageTypeToNvaType(types)));
        return record;
    }
}

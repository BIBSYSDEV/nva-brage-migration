package no.sikt.nva;

import static no.sikt.nva.RecordsWriter.WRITING_RECORDS_HAS_FAILED;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import java.util.Collections;
import java.util.List;
import no.sikt.nva.model.record.Language;
import no.sikt.nva.model.record.Record;
import no.sikt.nva.model.record.Type;
import no.sikt.nva.scrapers.TypeMapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class RecordsWriterTest {

    public static final String INVALID_FILE_NAME = "";

    @Test
    void shouldLogIfWritingToFileFails() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        RecordsWriter.writeRecordsToFile(INVALID_FILE_NAME, List.of(createRecord()));
        assertThat(appender.getMessages(), containsString(WRITING_RECORDS_HAS_FAILED));
    }

    private Record createRecord() {
        var record = new Record();
        record.setId(randomUri());
        List<String> types = Collections.singletonList("Research report");
        record.setType(new Type(types, TypeMapper.convertBrageTypeToNvaType(types)));
        record.setLanguage(new Language(List.of("as"), randomUri()));
        record.setLanguage(new Language(List.of("nob"), randomUri()));

        return record;
    }
}

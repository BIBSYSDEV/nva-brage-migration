package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.util.ArrayList;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.record.Record;
import org.junit.jupiter.api.Test;

public class DublinCoreParserTest {

    public static final String CRISTIN_DUBLIN_CORE = "src/test/resources/dublin_core_with_cristin_identifier.xml";
    private final DublinCoreParser dublinCoreParser = new DublinCoreParser();

    @Test
    void shouldConvertFilesWithValidFields() throws Exception {
        var expectedRecord = createTestRecord();
        var actualRecord = dublinCoreParser.parseDublinCoreToRecord(new File("src/test/resources/dublin_core.xml"));

        assertThat(actualRecord, is(equalTo(expectedRecord)));
    }

    @Test
    void shouldReturnExceptionIfResourceIsInCristin() {
        assertThrows(DublinCoreException.class, () -> dublinCoreParser.parseDublinCoreToRecord(new File(
            CRISTIN_DUBLIN_CORE)));
    }


    private Record createTestRecord() {
        ArrayList<String> authors = new ArrayList<>();
        authors.add("Navnesen1, Fornavn1 Mellomnavn1");
        authors.add("Navnesen2, Fornavn2 Mellomnavna2 Mellomnavnb2");
        authors.add("Navnesen3, Fornavn3 Mellomnavn3");

        Record record = new Record();
        record.setId("123567");
        record.setType("Research report");
        record.setTitle("Studie av friluftsliv blant barn og unge i Oslo: Sosial ulikhet og sosial utjevning");
        record.setLanguage("nob");
        record.setAuthors(authors);
        return record;
    }
}

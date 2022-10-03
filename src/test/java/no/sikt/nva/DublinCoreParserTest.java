package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.util.ArrayList;
import no.sikt.nva.exceptions.CristinException;
import no.sikt.nva.model.record.Record;
import org.junit.jupiter.api.Test;

public class DublinCoreParserTest {

    private final DublinCoreParser dublinCoreParser = new DublinCoreParser();

    @Test
    void shouldConvertFilesWithValidFields() throws Exception {
        var expectedRecord = createTestRecord();
        var actualRecord = dublinCoreParser.parseDublinCore(new File("src/test/resources/dublin_core.xml"));

        assertThat(actualRecord, is(equalTo(expectedRecord)));
    }

    @Test
    void shouldReturnExceptionIfResourceIsInCristin() {
        assertThrows(CristinException.class, () -> dublinCoreParser.parseDublinCore(new File("src/test/resources/dublin_core_with_cristin_identifier.xml")));
    }



    private Record createTestRecord() {
        Record record = new Record();
        ArrayList<String> authors = new ArrayList<>();
        authors.add("Navnesen1, Fornavn1 Mellomnavn1");
        authors.add("Navnesen2, Fornavn2 Mellomnavna2 Mellomnavnb2");
        authors.add("Navnesen3, Fornavn3 Mellomnavn3");
        record.setId("123567");
        record.setType("Research report");
        record.setTitle("Studie av friluftsliv blant barn og unge i Oslo: Sosial ulikhet og sosial utjevning");
        record.setLanguage("nob");
        record.setAuthors(authors);
        return record;
    }
}

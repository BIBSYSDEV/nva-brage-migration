package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.util.ArrayList;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.publisher.Publication;
import no.sikt.nva.model.record.Record;
import org.junit.jupiter.api.Test;

public class DublinCoreParserTest {

    public static final String CRISTIN_DUBLIN_CORE = "src/test/resources/dublin_core_with_cristin_identifier.xml";
    private final DublinCoreParser dublinCoreParser = new DublinCoreParser();

    @Test
    void shouldConvertFilesWithValidFields() {
        var expectedRecord = createTestRecord();
        var record = new Record();
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File("src/test/resources/dublin_core.xml"),
                                                                   record.getOriginInformation());
        var actualRecord = dublinCoreParser.convertDublinCoreToRecord(dublinCore, record);

        assertThat(actualRecord, is(equalTo(expectedRecord)));
    }

    @Test
    void shouldReturnExceptionIfResourceIsInCristin() {
        var record = new Record();
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(
            CRISTIN_DUBLIN_CORE), record.getOriginInformation());
        assertThrows(DublinCoreException.class, () ->
                                                    dublinCoreParser.convertDublinCoreToRecord(dublinCore, record));
    }

    private Record createTestRecord() {
        Record record = new Record();
        record.setType("Research report");
        record.setTitle("Studie av friluftsliv blant barn og unge i Oslo: Sosial ulikhet og sosial utjevning");
        record.setLanguage("nob");
        record.setAuthors(createAuthors());
        record.setPublication(createPublication());
        return record;
    }

    private Publication createPublication() {

        Publication publication = new Publication();
        publication.setIssn("2345-2344-5567");
        return publication;
    }

    private ArrayList<String> createAuthors() {
        ArrayList<String> authors = new ArrayList<>();
        authors.add("Navnesen1, Fornavn1 Mellomnavn1");
        authors.add("Navnesen2, Fornavn2 Mellomnavna2 Mellomnavnb2");
        authors.add("Navnesen3, Fornavn3 Mellomnavn3");
        return authors;
    }
}

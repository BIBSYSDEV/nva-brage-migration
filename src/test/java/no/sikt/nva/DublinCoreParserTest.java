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

    public static final String INVALID_DUBLIN_CORE = "src/test/resources/invalid_dublin_core.xml";

    public static final String VALID_DUBLIN_CORE = "src/test/resources/valid_dublin_core.xml";

    @Test
    void shouldConvertFilesWithValidFields() {
        var expectedRecord = createTestRecord();
        var actualRecord = new Record();
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(VALID_DUBLIN_CORE),
                                                                   actualRecord.getOriginInformation());
       DublinCoreParser.validateAndParseDublinCore(dublinCore, actualRecord);

        assertThat(actualRecord, is(equalTo(expectedRecord)));
    }

    @Test
    void shouldReturnExceptionIfResourceIsInCristin() {
        var record = new Record();
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(
            INVALID_DUBLIN_CORE), record.getOriginInformation());
        assertThrows(DublinCoreException.class, () ->
                                                    DublinCoreParser.validateAndParseDublinCore(dublinCore, record));
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

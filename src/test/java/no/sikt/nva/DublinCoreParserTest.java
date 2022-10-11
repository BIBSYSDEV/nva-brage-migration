package no.sikt.nva;

import static no.sikt.nva.DublinCoreParser.FIELD_WAS_NOT_SCRAPED_IN_LOCATION_LOG_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.record.Record;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class DublinCoreParserTest {

    public static final String CRISTIN_DUBLIN_CORE = "src/test/resources/dublin_core_with_cristin_identifier.xml";
    private final DublinCoreParser dublinCoreParser = new DublinCoreParser();

    @Test
    void shouldConvertFilesWithValidFields() {
        var expectedRecord = createTestRecord();
        var record = new Record();
        var actualRecord = dublinCoreParser.parseDublinCoreToRecord(new File("src/test/resources/dublin_core.xml"),
                                                                    record);

        assertThat(actualRecord, is(equalTo(expectedRecord)));
    }

    @Test
    void shouldReturnExceptionIfResourceIsInCristin() {
        var record = new Record();
        assertThrows(DublinCoreException.class, () ->
                                                    dublinCoreParser
                                                        .parseDublinCoreToRecord(new File(
                                                            CRISTIN_DUBLIN_CORE), record));
    }

    @Test
    void shouldLogDcValuesThatAreNotUsedForScraping() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var expectedDcValuedLogged = new DcValue(Element.DESCRIPTION, Qualifier.PROVENANCE,
                                                 "Gurba Gurba gurba gurba gurba gurba gurba gurba gurba gurba gurba "
                                                 + "gurba gurba gurba gurba gurba gurba gurba (øæsdfadfåp)").toString();
        var record = new Record();
        record.setOrigin(Path.of("something/something"));
        dublinCoreParser.parseDublinCoreToRecord(new File("src/test/resources/dublin_core.xml"),
                                                 record);
        assertThat(appender.getMessages(), containsString(String.format(
            FIELD_WAS_NOT_SCRAPED_IN_LOCATION_LOG_MESSAGE, expectedDcValuedLogged, record.getOriginInformation())));
    }

    private Record createTestRecord() {
        ArrayList<String> authors = new ArrayList<>();
        authors.add("Navnesen1, Fornavn1 Mellomnavn1");
        authors.add("Navnesen2, Fornavn2 Mellomnavna2 Mellomnavnb2");
        authors.add("Navnesen3, Fornavn3 Mellomnavn3");
        Record record = new Record();
        record.setType("Research report");
        record.setTitle("Studie av friluftsliv blant barn og unge i Oslo: Sosial ulikhet og sosial utjevning");
        record.setLanguage("nob");
        record.setAuthors(authors);
        return record;
    }
}

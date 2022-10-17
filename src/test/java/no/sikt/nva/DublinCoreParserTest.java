package no.sikt.nva;

import static no.sikt.nva.DublinCoreParser.FIELD_WAS_NOT_SCRAPED_IN_LOCATION_LOG_MESSAGE;
import static no.sikt.nva.ResourceNameConstants.INVALID_DUBLIN_CORE_XML_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.ResourceNameConstants.VALID_DUBLIN_CORE_XML_FILE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.publisher.Publication;
import no.sikt.nva.model.record.Record;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class DublinCoreParserTest {

    public static final String INVALID_DUBLIN_CORE = "src/test/resources/invalid_dublin_core.xml";

    public static final String VALID_DUBLIN_CORE = "src/test/resources/valid_dublin_core.xml";


    @Test
    void shouldConvertFilesWithValidFields() {
        var expectedRecord = createTestRecord();
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(
            new File(TEST_RESOURCE_PATH + VALID_DUBLIN_CORE_XML_FILE_NAME));
        var actualRecord = DublinCoreParser.validateAndParseDublinCore(dublinCore, brageLocation);
        DublinCoreParser.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(actualRecord, is(equalTo(expectedRecord)));
    }

    @Test
    void shouldReturnExceptionIfResourceIsInCristin() {
        var brageLocation = new BrageLocation(Path.of("somebundle/someindex"));
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(
            new File(TEST_RESOURCE_PATH + INVALID_DUBLIN_CORE_XML_FILE_NAME));
        assertThrows(DublinCoreException.class,
                     () -> DublinCoreParser.validateAndParseDublinCore(dublinCore,
                                                                       brageLocation));
    }

    @Test
    void shouldLogDcValuesThatAreNotUsedForScraping() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var expectedDcValuedLogged = new DcValue(Element.DESCRIPTION, Qualifier.PROVENANCE,
                                                 "Gurba Gurba gurba gurba gurba gurba "
                                                 + "gurba gurba gurba gurba gurba gurba gurba "
                                                 + "gurba gurba gurba gurba gurba (øæsdfadfåp)").toXmlString();

        var brageLocation = new BrageLocation(Path.of("somebundle/someindex"));
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(
            new File(TEST_RESOURCE_PATH + VALID_DUBLIN_CORE_XML_FILE_NAME));
        var record = DublinCoreParser.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(appender.getMessages(), containsString(String.format(
            FIELD_WAS_NOT_SCRAPED_IN_LOCATION_LOG_MESSAGE, expectedDcValuedLogged, record.getOriginInformation())));
    }

    @Test
    void shouldConvertValidVersionToPublisherAuthority() {
        var expectedPublisherAuthority = true;
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValue(Element.DESCRIPTION,
                                                                       Qualifier.VERSION,
                                                                       "publishedVersion");
        var record = DublinCoreParser.validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        record.setOrigin(Path.of("something/something"));
        var actualPublisherAuthority = record.getPublisherAuthority();
        assertThat(actualPublisherAuthority, is(equalTo(expectedPublisherAuthority)));
    }

    @Test
    void shouldCreatePublisherAuthorityIfVersionIsNotPresent() {
        var record = new Record();
        record.setOrigin(Path.of("something/something"));

        var dublinCoreWithoutVersion = DublinCoreFactory.createDublinCoreWithDcValue(Element.CONTRIBUTOR,
                                                                                     Qualifier.AUTHOR,
                                                                                     "someAuthor");
        DublinCoreParser.validateAndParseDublinCore(dublinCoreWithoutVersion, new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority();
        assertThat(actualPublisherAuthority, is(equalTo(null)));
    }

    private Record createTestRecord() {
        Record record = new Record();
        record.setType("Research report");
        record.setTitle("Studie av friluftsliv blant barn og unge i Oslo: Sosial ulikhet og sosial utjevning");
        record.setLanguage("nob");
        record.setAuthors(createAuthors());
        record.setPublication(createPublication());
        record.setRightsHolder("NVE");
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

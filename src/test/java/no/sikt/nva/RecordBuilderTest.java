package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import no.sikt.nva.model.record.Record;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;

public class RecordBuilderTest {

    private final RecordBuilder recordBuilder = new RecordBuilder();

    @Test
    void shouldConvertFilesWithValidFields() throws JAXBException, IOException {
        var expectedRecord = createTestRecord();
        var inputStream = IoUtils.inputStreamFromResources("testinput.zip");
        var actualBundles = recordBuilder.extractBundles(inputStream, new File("tmp"));
        var actualRecords = recordBuilder.getRecords(actualBundles);

        assertThat(actualRecords, hasItem(is(equalTo(expectedRecord))));
    }

    @Test
    void shouldDropFilesWithoutNecessaryFields() throws JAXBException, IOException {
        var inputStream = IoUtils.inputStreamFromResources("inputThatShouldBeDropped.zip");
        var actualBundles = recordBuilder.extractBundles(inputStream, new File("tmp"));
        var actualRecords = recordBuilder.getRecords(actualBundles);

        assertThat(actualRecords, is(empty()));
    }

    @Test
    void shouldSkipSingleFile() throws JAXBException, IOException {
        var inputStream = IoUtils.inputStreamFromResources("inputWithFileWithoutDirectory.zip");
        var actualBundles = recordBuilder.extractBundles(inputStream, new File("tmp"));
        var actualRecords = recordBuilder.getRecords(actualBundles);

        assertThat(actualRecords, hasSize(2));
    }


    private Record createTestRecord() {
        Record record = new Record();
        ArrayList<String> authors = new ArrayList<>();
        authors.add("Navnesen1, Fornavn1 Mellomnavn1");
        authors.add("Navnesen2, Fornavn2 Mellomnavna2 Mellomnavnb2");
        authors.add("Navnesen3, Fornavn3 Mellomnavn3");
        record.setId("112502684299");
        record.setType("Research report");
        record.setTitle("Studie av friluftsliv blant barn og unge i Oslo: Sosial ulikhet og sosial utjevning");
        record.setLanguage("nob");
        record.setLicense(
            " NOTE: PLACE YOUR OWN LICENSE HERE. This sample license is provided for informational purposes only., ");
        record.setAuthors(authors);
        return record;
    }
}

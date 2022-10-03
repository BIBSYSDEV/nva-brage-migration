package no.sikt.nva;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.GodClass", "PMD.CollapsibleIfStatements"})
public class RecordBuilder {

    public static final String SLASH = "/";
    public static final String HANDLE_FILE_NAME = "handle";
    public static final String LACK_OF_AUTHORS_MESSAGE =
        "Record with id {} has been dropped because of lack of authors";
    public static final String CRISTIN_ID_QUALIFIER = "cristin";
    private static final String DUBLIN_CORE_XML_FILE_NAME = "dublin_core.xml";
    private static final String AUTHOR_QUALIFIER = "author";
    private static final String IDENTIFIER_ELEMENT = "identifier";
    private static final String CONTRIBUTOR_ELEMENT = "contributor";
    private static final String TITLE_ELEMENT = "title";
    private static final String LANGUAGE_ELEMENT = "language";
    private static final String TYPE_ELEMENT = "type";
    private static final String URI_QUALIFIER = "uri";
    private static final String LICENCE_FILE_NAME = "license.txt";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final UnZipper unZipper = new UnZipper();

    public List<File> extractBundles(InputStream inputStream, File destinationDirectory) {
        var file = unZipper.unzip(inputStream, destinationDirectory);
        return Arrays.stream(Objects.requireNonNull(file.listFiles()))
                   .collect(Collectors.toList());
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<Record> getRecords(List<File> directories) throws JAXBException, IOException {
        List<Record> records = new ArrayList<>();
        for (File entryDirectory : directories) {
            if (entryDirectory.isDirectory()) {
                Record record = new Record();
                for (File file : Objects.requireNonNull(entryDirectory.listFiles())) {
                    setValuesFromDublinCore(record, file);
                    setLicense(record, file);
                    setId(record, file);
                }
                if (record.getAuthors().isEmpty() || record.getAuthors() == null) {
                    logger.info(LACK_OF_AUTHORS_MESSAGE, record.getId());
                    record = null;
                }
                records.add(record);
            }
        }
        records.removeAll(Collections.singleton(null));
        return records;
    }

    private void setValuesFromDublinCore(Record record, File file) throws JAXBException {
        if (DUBLIN_CORE_XML_FILE_NAME.equals(file.getName())) {
            var unmarshaller = getUnmarshaller();
            DublinCore dublinCore = (DublinCore) unmarshaller.unmarshal(file);
            var recordFromDublinCore = convertDublinCoreToRecord(dublinCore);
            record.createRecord(recordFromDublinCore);
        }
    }

    private void setLicense(Record record, File file) throws IOException {
        if (LICENCE_FILE_NAME.equals(file.getName())) {
            var license = readLicenseFile(file);
            record.setLicense(license);
        }
    }

    private void setId(Record record, File file) throws IOException {
        if (HANDLE_FILE_NAME.equals(file.getName())) {
            var id = readHandleFile(file)
                         .replace("/", "")
                         .replace("[", "")
                         .replace("]", "");
            if (id.equals(record.getId())) {
                return;
            }
            record.setId(id);
        }
    }

    private String readLicenseFile(File file) throws IOException {
        return Files.lines(Path.of(file.getAbsolutePath()))
                   .collect(Collectors.toList())
                   .toString()
                   .replace("[", "")
                   .replace("]", "");
    }

    private String readHandleFile(File file) throws IOException {
        return Files.lines(Path.of(file.getAbsolutePath()))
                   .collect(Collectors.toList())
                   .toString();
    }

    private Record convertDublinCoreToRecord(DublinCore dublinCore) {
        Record record = new Record();
        ArrayList<String> authors = new ArrayList<>();

        for (DcValue dcValue : dublinCore.getDcValues()) {
            var element = dcValue.getElement();
            var qualifier = dcValue.getQualifier();
            if (element != null) {
                if (CONTRIBUTOR_ELEMENT.equals(element.getValue())) {
                    extractAuthor(authors, dcValue);
                    continue;
                }
                if (TITLE_ELEMENT.equals(element.getValue())) {
                    extractTitle(record, dcValue);
                    continue;
                }
                if (LANGUAGE_ELEMENT.equals(element.getValue())) {
                    extractLanguage(record, dcValue);
                    continue;
                }
                if (TYPE_ELEMENT.equals(element.getValue())) {
                    extractType(record, dcValue);
                    continue;
                }
            }
            if (isValidUriIdentifier(element, qualifier)) {
                extractId(record, dcValue);
            }
        }

        record.setAuthors(authors);
        return record;
    }

    private boolean isValidUriIdentifier(Element element, Qualifier qualifier) {
        return element != null && qualifier != null &&
               IDENTIFIER_ELEMENT.equals(element.getValue()) &&
               URI_QUALIFIER.equals(qualifier.getValue());
    }

    private void extractTitle(Record record, DcValue dcValue) {
        var element = dcValue.getElement();
        if (element != null) {
            if (TITLE_ELEMENT.equals(element.getValue())) {
                record.setTitle(dcValue.getValue());
            }
        }
    }

    private void extractAuthor(ArrayList<String> authors, DcValue dcValue) {
        var qualifier = dcValue.getQualifier();
        var element = dcValue.getElement();
        if (qualifier != null && element != null) {
            if (AUTHOR_QUALIFIER.equals(qualifier.getValue()) && CONTRIBUTOR_ELEMENT.equals(element.getValue())) {
                authors.add(dcValue.getValue());
            }
        }
    }

    private void extractLanguage(Record record, DcValue dcValue) {
        var element = dcValue.getElement();
        if (element != null) {
            if (LANGUAGE_ELEMENT.equals(element.getValue())) {
                record.setLanguage(dcValue.getValue());
            }
        }
    }

    private void extractType(Record record, DcValue dcValue) {
        var element = dcValue.getElement();
        if (element != null) {
            if (TYPE_ELEMENT.equals(element.getValue())) {
                record.setType(dcValue.getValue());
            }
        }
    }

    private void extractId(Record record, DcValue dcValue) {
        var element = dcValue.getElement();
        var qualifier = dcValue.getQualifier();
        if (element != null && qualifier != null) {
            if (IDENTIFIER_ELEMENT.equals(element.getValue()) && URI_QUALIFIER.equals(qualifier.getValue())) {
                var id = convertUriToHandle(dcValue.getValue());
                record.setId(id);
            }
        }
    }

    private boolean hasCristinId(DcValue dcValue) {
        var element = dcValue.getElement();
        var qualifier = dcValue.getQualifier();
        if (element != null && qualifier != null) {
            return IDENTIFIER_ELEMENT.equals(element.getValue()) && CRISTIN_ID_QUALIFIER.equals(qualifier.getValue());
        }
        return false;
    }

    private String convertUriToHandle(String uri) {
        var list = uri.split(SLASH);
        var firstPartOfId = list[list.length - 2];
        var secondPartOfId = list[list.length - 1];
        return firstPartOfId + secondPartOfId;
    }

    private Unmarshaller getUnmarshaller() throws JAXBException {
        return JAXBContext.newInstance(DublinCore.class).createUnmarshaller();
    }
}

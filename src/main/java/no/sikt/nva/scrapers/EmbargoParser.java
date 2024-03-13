package no.sikt.nva.scrapers;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.model.Embargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbargoParser {

    public static final String PDF_TXT = "pdf.txt";
    public static final String PDF_JPG = "pdf.jpg";
    public static final String LICENSE_RDF = "license_rdf";
    public static final String LICENSE_TXT = "license.txt";
    private static final Logger logger = LoggerFactory.getLogger(EmbargoParser.class);
    private static final String ORE_XML = "ORE.xml";

    private static final String REGEX_SWORD_XML_FILENAME = "sword-\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\"
                                                           + ".original\\.xml";
    private static final Pattern PATTERN_SWORD_XML_FILENAME = Pattern.compile(REGEX_SWORD_XML_FILENAME);

    private static final String REGEX_CRISTIN_ZIP_METADATA_FILE = "cristin-\\d+\\.zip";
    private static final Pattern PATTERN_CRISTIN_ZIP_METADATA_FILE = Pattern.compile(REGEX_CRISTIN_ZIP_METADATA_FILE);
    private static final String REGEX_CRISTIN_XML_METADATA_FILE = "cristin-\\d+\\.xml";
    private static final Pattern PATTERN_CRISTIN_XML_METADATA_FILE = Pattern.compile(REGEX_CRISTIN_XML_METADATA_FILE);

    public static Record checkForEmbargoFromSuppliedEmbargoFile(Record record, Map<String, List<Embargo>> embargoes) {
        var handle = record.getId().toString();
        if (containsHandle(embargoes, handle)) {
            embargoContentFiles(embargoes.get(handle), record);
        }
        return record;
    }

    public static void logNonEmbargosDetected(Map<String, List<Embargo>> embargoes) {
        embargoes
            .values()
            .stream()
            .flatMap(List::stream)
            .filter(EmbargoParser::shouldBeLogged)
            .forEach(EmbargoParser::logEmbargoAsNotDetected);
    }

    private static boolean containsHandle(Map<String, List<Embargo>> embargoes, String handle) {
        return embargoes.containsKey(handle);
    }

    private static void embargoContentFiles(List<Embargo> embargoList, Record record) {
        record.getContentBundle()
            .getContentFiles()
            .forEach(contentFile -> setEmbargoIfNeeded(contentFile, embargoList));
    }

    private static void setEmbargoIfNeeded(ContentFile contentFile,
                                           List<Embargo> embargoList) {
        var optionalEmbargo =
            embargoList
                .stream()
                .filter(embargo -> embargoMatchesFile(contentFile, embargo))
                .findFirst();
        if (optionalEmbargo.isPresent()) {
            var embargo = optionalEmbargo.get();
            updateContentFileWithEmbargo(contentFile, embargo);
            embargo.setDetectedFile(true);
        }
    }

    private static void updateContentFileWithEmbargo(ContentFile contentFile,
                                                     Embargo embargo) {
        contentFile.setEmbargoDate(embargo.getDateAsInstant());
    }

    private static boolean embargoMatchesFile(ContentFile contentFile, Embargo embargo) {
        return embargo.getFilename().equals(contentFile.getFilename());
    }

    private static void logEmbargoAsNotDetected(Embargo embargo) {
        logger.error("Embargo file not found: "
                     + embargo.getFilename()
                     + ", for handle: "
                     + embargo.getHandle());
    }

    private static boolean shouldBeLogged(Embargo embargo) {
        return !embargo.isDetectedFile()
               && !isIgnoredFile(embargo);
    }

    private static boolean isIgnoredFile(Embargo embargo) {
        return embargo.getFilename().contains(PDF_TXT)
               || embargo.getFilename().contains(PDF_JPG)
               || isCristinMetadataZipfile(embargo.getFilename())
               || isCristinMetadataXmlFile(embargo.getFilename())
               || isSwordXmlFile(embargo.getFilename())
               || ORE_XML.equals(embargo.getFilename())
               || LICENSE_RDF.equals(embargo.getFilename())
               || LICENSE_TXT.equals(embargo.getFilename());
    }

    private static boolean isCristinMetadataXmlFile(String filename) {
        var matcher = PATTERN_CRISTIN_XML_METADATA_FILE.matcher(filename);
        return matcher.matches();
    }

    private static boolean isSwordXmlFile(String filename) {
        var matcher = PATTERN_SWORD_XML_FILENAME.matcher(filename);
        return matcher.matches();
    }

    private static boolean isCristinMetadataZipfile(String filename) {
        var matcher = PATTERN_CRISTIN_ZIP_METADATA_FILE.matcher(filename);
        return matcher.matches();
    }
}

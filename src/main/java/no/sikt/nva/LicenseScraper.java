package no.sikt.nva;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import no.sikt.nva.exceptions.LicenseExtractingException;
import no.sikt.nva.model.record.Record;
import nva.commons.core.SingletonCollector;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseScraper {

    public static final Property LICENSE = ResourceFactory.createProperty("http://creativecommons.org/ns#license");
    public static final Property WORK = ResourceFactory.createProperty("http://creativecommons.org/ns#Work");
    public static final Resource ANY_SUBJECT = null;
    public static final RDFNode ANY_OBJECT = null;
    public static final String LICENSE_EXCEPTION_MESSAGE = "Extraction of license failed";
    public static final String READING_LICENSE_FILE_EXCEPTION_MESSAGE = "Reading license file has failed";
    public static final String COULD_NOT_EXTRACT_LICENSE_FROM_SPECIFIED_LOCATION_LOG_MESSAGE_WARNING =
        "No license in bundle found, default license used. Bundle information + %s";
    public static final String DEFAULT_LICENSE = "Rights Reserved";
    private static final Logger logger = LoggerFactory.getLogger(LicenseScraper.class);
    private final String customLicenseFilename;

    public LicenseScraper(String customLicenseFilename) {
        this.customLicenseFilename = customLicenseFilename;
    }

    public String extractOrCreateLicense(File bundleDirectory, String bundleInformation) {
        try {
            var licenseFile = new File(bundleDirectory, customLicenseFilename);
            return extractLicenseFromFile(licenseFile);
        } catch (Exception e) {
            logger.warn(String.format(COULD_NOT_EXTRACT_LICENSE_FROM_SPECIFIED_LOCATION_LOG_MESSAGE_WARNING,
                                      bundleInformation),
                        e);
            return DEFAULT_LICENSE;
        }
    }

    /**
     * Extracts license uri from specified file.
     *
     * @param file licenseFile as xml
     * @return uri as string
     * @throws LicenseExtractingException if license file has incorrect format
     */
    private static String extractLicenseFromFile(File file) {
        try {
            Model model = createModel(file);
            var work = new SimpleSelector(ANY_SUBJECT, RDF.type, WORK).getSubject();
            SimpleSelector uriSelector = new SimpleSelector(work, LICENSE, ANY_OBJECT);

            return extractLicense(model.listStatements(uriSelector)
                                      .toList()
                                      .stream()
                                      .map(Statement::getObject)
                                      .filter(RDFNode::isResource));
        } catch (Exception e) {
            throw new LicenseExtractingException(LICENSE_EXCEPTION_MESSAGE, e);
        }
    }

    private static String extractLicense(Stream<RDFNode> model) {
        return model
                   .map(RDFNode::asResource)
                   .map(Resource::getURI)
                   .collect(SingletonCollector.collect());
    }

    private static Model createModel(File file) {
        Model model = ModelFactory.createDefaultModel();

        try (InputStream inputStream = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
            RDFDataMgr.read(model, inputStream, Lang.RDFXML);
            return model;
        } catch (Exception e) {
            throw new LicenseExtractingException(READING_LICENSE_FILE_EXCEPTION_MESSAGE, e);
        }
    }
}

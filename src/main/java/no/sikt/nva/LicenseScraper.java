package no.sikt.nva;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import no.sikt.nva.exceptions.LicenseExtractingException;
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
    private static final Logger logger = LoggerFactory.getLogger(LicenseScraper.class);

    public URI extractLicenseUri(File file) throws LicenseExtractingException {
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
            logger.info(e.getMessage());
            throw new LicenseExtractingException(LICENSE_EXCEPTION_MESSAGE);
        }
    }

    private URI extractLicense(Stream<RDFNode> model) {
        return model
                   .map(RDFNode::asResource)
                   .map(Resource::getURI)
                   .map(URI::create)
                   .collect(SingletonCollector.collect());
    }

    private Model createModel(File file) throws IOException {
        Model model = ModelFactory.createDefaultModel();

        try (InputStream inputStream = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
            RDFDataMgr.read(model, inputStream, Lang.RDFXML);
            return model;
        } catch (Exception e) {
            throw new IOException(READING_LICENSE_FILE_EXCEPTION_MESSAGE);
        }
    }
}

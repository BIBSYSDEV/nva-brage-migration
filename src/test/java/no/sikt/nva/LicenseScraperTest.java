package no.sikt.nva;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Stream;
import nva.commons.core.SingletonCollector;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
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
import org.junit.jupiter.api.Test;

public class LicenseScraperTest {

    public static final String QUERY = "SELECT ?uri WHERE {?blanknode a <http://creativecommons.org/ns#Work> ;"
                                       + "<http://creativecommons.org/ns#license> ?uri .} ";
    public static final Property LICENSE = ResourceFactory.createProperty("http://creativecommons.org/ns#license");
    public static final Property WORK = ResourceFactory.createProperty("http://creativecommons.org/ns#Work");
    public static final Resource ANY_SUBJECT = null;
    public static final RDFNode ANY_OBJECT = null;
    private final static String singleLineLicense = "[ NOTE: PLACE YOUR OWN LICENSE HERE. This sample license is "
                                                    + "provided for informational purposes only.]";
    private final static String multiLineLicense = "[ NOTE: PLACE YOUR OWN LICENSE HERE. This sample license is "
                                                   + "provided for informational purposes only. NOTE: PLACE YOUR OWN "
                                                   + "LICENSE HERE. This sample license is provided for informational"
                                                   + " purposes only."
                                                   + "NOTE: PLACE YOUR OWN LICENSE HERE. This sample license is "
                                                   + "provided for informational purposes only."
                                                   + "NOTE: PLACE YOUR OWN LICENSE HERE. This sample license is "
                                                   + "provided for informational purposes only."
                                                   + "NOTE: PLACE YOUR OWN LICENSE HERE. This sample license is "
                                                   + "provided for informational purposes only. NOTE: PLACE YOUR OWN "
                                                   + "LICENSE HERE. This sample license is provided for informational"
                                                   + " purposes only. NOTE: PLACE YOUR OWN LICENSE HERE. This sample "
                                                   + "license is provided for informational purposes only. ]";

    @Test
    void shouldParseSingleLineLicenseFileToString() throws IOException {
        var licenseFile = new File("src/test/resources/singleLineLicense.txt");
        LicenseScraper licenseScraper = new LicenseScraper();
        var actualLicense = licenseScraper.scrapeLicense(licenseFile);

        assertThat(actualLicense, is(equalTo(singleLineLicense)));
    }

    @Test
    void shouldParseMultiLineLicenseFileToString() throws IOException {
        var licenseFile = new File("src/test/resources/multipleLinesLicense.txt");
        LicenseScraper licenseScraper = new LicenseScraper();
        var actualLicense = licenseScraper.scrapeLicense(licenseFile);

        assertThat(actualLicense, is(equalTo(multiLineLicense)));
    }

    @Test
    void shouldReadLicenseXml() {
        var expectedLicense = URI.create("http://creativecommons.org/licenses/by/4.0/deed.no");

        Model model = createModel();
        var licenseClass = model.createProperty("http://creativecommons.org/ns#license");
        SimpleSelector simpleSelector = new SimpleSelector(null, licenseClass, (RDFNode) null);
        var statements = extractLicense(model.listStatements(simpleSelector)
                                            .toList()
                                            .stream()
                                            .map(Statement::getObject)
                                            .filter(RDFNode::isResource));

        assertThat(statements, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldReadLicenseXmlV2() {
        var expectedLicense = URI.create("http://creativecommons.org/licenses/by/4.0/deed.no");

        URI statements = extractLicenseUri();

        assertThat(statements, is(equalTo(expectedLicense)));
    }

    @Test
    void shouldReadLicenseXmlV3() {
        var expectedLicense = URI.create("http://creativecommons.org/licenses/by/4.0/deed.no");

        Model model = createModel();
        String queryString = QUERY;
        var list = new ArrayList<RDFNode>();
        executeQuery(model, queryString, list);

        var stream = extractLicense(list.stream());

        assertThat(stream, is(equalTo(expectedLicense)));
    }

    private URI extractLicenseUri() {
        Model model = createModel();

        var work = new SimpleSelector(ANY_SUBJECT, RDF.type, WORK).getSubject();
        SimpleSelector uriSelector = new SimpleSelector(work, LICENSE, ANY_OBJECT);

        return extractLicense(model.listStatements(uriSelector)
                                  .toList()
                                  .stream()
                                  .map(Statement::getObject)
                                  .filter(RDFNode::isResource));
    }

    private URI extractLicense(Stream<RDFNode> model) {
        return model
                   .map(RDFNode::asResource)
                   .map(Resource::getURI)
                   .map(URI::create)
                   .collect(SingletonCollector.collect());
    }

    private Model createModel() {
        Model model = ModelFactory.createDefaultModel();
        InputStream inputStream = IoUtils.inputStreamFromResources("license_rdf");
        RDFDataMgr.read(model, inputStream, Lang.RDFXML);
        return model;
    }

    private void executeQuery(Model model, String queryString, ArrayList<RDFNode> list) {
        try (QueryExecution queryExecution = QueryExecutionFactory.create(queryString, model)) {
            var results = queryExecution.execSelect();
            while (results.hasNext()) {
                var v = results.getResultVars().stream().collect(SingletonCollector.collect());
                list.add(results.next().get(v));
            }
        }
    }
}



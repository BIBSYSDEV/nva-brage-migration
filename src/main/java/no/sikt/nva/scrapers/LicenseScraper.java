package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import static no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier.DEFAULT_LICENSE;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicense;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier;
import no.sikt.nva.exceptions.LicenseExtractingException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import nva.commons.core.SingletonCollector;
import nva.commons.core.StringUtils;
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

public class LicenseScraper {

    public static final Property LICENSE = ResourceFactory.createProperty("http://creativecommons.org/ns#license");
    public static final Property WORK = ResourceFactory.createProperty("http://creativecommons.org/ns#Work");
    public static final Resource ANY_SUBJECT = null;
    public static final RDFNode ANY_OBJECT = null;
    public static final String READING_LICENSE_FILE_EXCEPTION_MESSAGE = "Reading license file has failed";
    public static final String CC_BASE_URL = "creativecommons.org";
    public static final String NORWEGIAN_BOKMAAL = "nb";

    public static final License FALLBACK_LICENSE =
        new License(null, new NvaLicense(DEFAULT_LICENSE, getLicenseLabels(DEFAULT_LICENSE)));

    private final String customLicenseFilename;

    public LicenseScraper(String customLicenseFilename) {
        this.customLicenseFilename = customLicenseFilename;
    }

    public static boolean isValidCCLicense(License license) {
        if (isNull(license)) {
            return false;
        }
        if (isNull(license.getBrageLicense())) {
            return false;
        }
        return license.getBrageLicense().contains(CC_BASE_URL);
    }

    public License extractLicense(File bundleDirectory, DublinCore dublinCore) {
        return extractLicenseFromDublinCore(dublinCore)
                   .orElseGet(() -> extractLicenseFromFile(bundleDirectory)
                                        .orElse(FALLBACK_LICENSE));
    }

    private static License constructLicense(NvaLicenseIdentifier nvaLicenseIdentifier, String brageLicense) {
        var nvaLicenseLabels = getLicenseLabels(nvaLicenseIdentifier);
        return new License(brageLicense, new NvaLicense(nvaLicenseIdentifier, nvaLicenseLabels));
    }

    private static Optional<License> extractLicenseFromDublinCore(DublinCore dublinCore) {
        var licenseStringFromDublinCore = dublinCore.getDcValues().stream()
                                              .filter(DcValue::isLicense)
                                              .findAny()
                                              .map(DcValue::scrapeValueAndSetToScraped)
                                              .orElse(StringUtils.EMPTY_STRING);
        var licenseFromDublinCore = LicenseMapper.mapLicenseToNva(
            licenseStringFromDublinCore).map(nvaLicenseIdentifier -> constructLicense(nvaLicenseIdentifier,
                                                                                      licenseStringFromDublinCore));
        if (licenseFromDublinCore.isPresent() && isValidCCLicense(licenseFromDublinCore.get())) {
            return licenseFromDublinCore;
        } else {
            return Optional.empty();
        }
    }

    private static Map<String, String> getLicenseLabels(NvaLicenseIdentifier nvaLicenseIdentifier) {
        return Map.of(NORWEGIAN_BOKMAAL, nvaLicenseIdentifier.getValue());
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

    private Optional<License> extractLicenseFromFile(File bundleDirectory) {
        try {
            var licenseFile = new File(bundleDirectory, customLicenseFilename);
            Model model = createModel(licenseFile);
            var work = new SimpleSelector(ANY_SUBJECT, RDF.type, WORK).getSubject();
            SimpleSelector uriSelector = new SimpleSelector(work, LICENSE, ANY_OBJECT);

            var brageLicense = extractLicense(model.listStatements(uriSelector)
                                                  .toList()
                                                  .stream()
                                                  .map(Statement::getObject)
                                                  .filter(RDFNode::isResource));

            return LicenseMapper.mapLicenseToNva(brageLicense)
                       .map(nvaLicenseIdentifier -> constructLicense(nvaLicenseIdentifier, brageLicense));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

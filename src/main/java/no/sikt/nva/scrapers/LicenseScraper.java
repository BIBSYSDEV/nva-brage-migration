package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import static no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier.DEFAULT_LICENSE;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicense;
import no.sikt.nva.brage.migration.common.model.record.license.NvaLicenseIdentifier;
import no.sikt.nva.exceptions.LicenseExtractingException;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
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
import org.jetbrains.annotations.NotNull;

public class LicenseScraper {

    public static final Property LICENSE = ResourceFactory.createProperty("http://creativecommons.org/ns#license");
    public static final Property WORK = ResourceFactory.createProperty("http://creativecommons.org/ns#Work");
    public static final Resource ANY_SUBJECT = null;
    public static final RDFNode ANY_OBJECT = null;
    public static final String LICENSE_EXCEPTION_MESSAGE = "Extraction of license failed";
    public static final String READING_LICENSE_FILE_EXCEPTION_MESSAGE = "Reading license file has failed";
    public static final String CC_BASE_URL = "creativecommons.org";

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

    public License extractOrCreateLicense(File bundleDirectory, DublinCore dublinCore) {
        try {
            var licenseFile = new File(bundleDirectory, customLicenseFilename);
            var license = extractLicenseFromFile(licenseFile, dublinCore);
            return mapValidLicenseUriToNvaLicense(license, LicenseMapper.mapLicenseToNva(license));
        } catch (Exception e) {
            return mapValidLicenseUriToNvaLicense(null, DEFAULT_LICENSE);
        }
    }

    private static String extractLicenseFromFile(File file, DublinCore dublinCore) {
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
            var licenseStringFromDublinCore = dublinCore.getDcValues().stream()
                                                  .filter(DcValue::isLicense)
                                                  .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
            var licenseFromDublinCore = mapValidLicenseUriToNvaLicense(licenseStringFromDublinCore,
                                                                       LicenseMapper.mapLicenseToNva(
                                                                           licenseStringFromDublinCore));
            if (isValidCCLicense(licenseFromDublinCore)) {
                return licenseFromDublinCore.getBrageLicense();
            } else {
                throw new LicenseExtractingException(LICENSE_EXCEPTION_MESSAGE, e);
            }
        }
    }

    @NotNull
    private static License mapValidLicenseUriToNvaLicense(String licenseStringFromDublinCore,
                                                          NvaLicenseIdentifier licenseStringFromDublinCore1) {
        return new License(licenseStringFromDublinCore,
                           new NvaLicense(licenseStringFromDublinCore1));
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

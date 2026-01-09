package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.BUNDLE_PATH;
import static no.sikt.nva.ResourceNameConstants.EMPTY_CONTENT_FILE_PATH;
import static no.sikt.nva.ResourceNameConstants.UIO_CONTENT_FILE_PATH;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.CONTENT_FILE_MISSING;
import static no.sikt.nva.scrapers.CustomerMapper.UIO;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.ErrorDetails.Error;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent.BundleType;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ContentScraperTest {

    public static final String ORIGINAL_FILENAME_1 = "rapport2022_25_1.pdf";
    public static final String ORIGINAL_FILENAME_2 = "rapport2022_25_2.pdf";
    public static final String CUSTOM_LICENSE_PDF_FILENAME = "CustomLicense.pdf";
    public static final String DUBLIN_CORE_FILE_NAME = "dublin_core.xml";
    private static final License someLicense = new License(null, null);
    protected static final String UNKNOWN_UIO_FILE = "uio_unknown_file.pdf";
    private ContentScraper contentScraper = new ContentScraper(Path.of(BUNDLE_PATH),
                                                               new BrageLocation(null),
                                                               someLicense, null, randomString());

    @Test
    void shouldCreateResourceContentCorrectly() {
        var actualContentFilenameList = contentScraper.scrapeContent()
                                            .getContentFiles()
                                            .stream()
                                            .map(ContentFile::getFilename)
                                            .collect(Collectors.toList());
        assertThat(actualContentFilenameList, containsInAnyOrder(ORIGINAL_FILENAME_1, ORIGINAL_FILENAME_2,
                                                                 CUSTOM_LICENSE_PDF_FILENAME, DUBLIN_CORE_FILE_NAME));
    }

    @Test
    void shouldScrapeNonDefaultLicenseFile() {
        var resourceContent = contentScraper.scrapeContent();
        var licenseFile = resourceContent.getContentFiles().stream()
                              .filter(file -> file.getBundleType().equals(BundleType.LICENSE))
                              .findFirst().orElseThrow();
        assertThat(licenseFile.getFilename(), is(equalTo(CUSTOM_LICENSE_PDF_FILENAME)));
    }

    @Test
    void shouldCreateResourceContentWithEmbargoCorrectly() {
        var expectedEmbargo = LocalDate.parse("3022-08-24").atStartOfDay(ZoneId.systemDefault()).toInstant();
        contentScraper = new ContentScraper(Path.of(BUNDLE_PATH),
                                            new BrageLocation(null),
                                            someLicense, "3022-08-24", randomString());
        var actualContentFilenameList = contentScraper.scrapeContent().getContentFiles().stream()
                                            .filter(ContentScraperTest::isNotDublinCoreFile)
                                            .collect(Collectors.toList());
        assertTrue(actualContentFilenameList.stream().allMatch(contentFile -> contentFile.getEmbargoDate().equals(expectedEmbargo)));
        assertThat(actualContentFilenameList.stream().map(ContentFile::getFilename).collect(Collectors.toList()),
                   containsInAnyOrder(ORIGINAL_FILENAME_1, ORIGINAL_FILENAME_2, CUSTOM_LICENSE_PDF_FILENAME));
    }

    @Test
    void shouldLogUnknownContentFile() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        contentScraper.scrapeContent();
        assertThat(appender.getMessages(), containsString("does not exist"));
    }

    @Test
    void shouldReturnMissingFilesWarningMessageWhenContentFileIsEmpty() {
        var contentScraper = new ContentScraper(Path.of(EMPTY_CONTENT_FILE_PATH),
                                                new BrageLocation(null),
                                                someLicense, null, randomString());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        contentScraper.scrapeContent();
        assertThat(appender.getMessages(), containsString(Error.CONTENT_FILE_MISSING.toString()));
    }

    @Test
    void shouldReturnContentFileMissingErrorMessageWhenContentFileIsNotPresent() {
        var contentScraper = new ContentScraper(Path.of("some/Random/Path"),
                                                new BrageLocation(null),
                                                someLicense, null, randomString());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        contentScraper.scrapeContent();
        assertThat(appender.getMessages(), containsString(String.valueOf(CONTENT_FILE_MISSING)));
    }

    @Test
    void shouldMapUnknownFileWhenMappingFilesForUIOAndFilesIsNotOfTypeOriginalOrSword() {
        var contentScraper = new ContentScraper(Path.of(UIO_CONTENT_FILE_PATH),
                                                new BrageLocation(null),
                                                someLicense, null, UIO);
        assertTrue(contentScraper.scrapeContent()
                                  .getContentFiles().stream()
                                  .anyMatch(file -> file.getBundleType().equals(BundleType.IGNORED)
                                                    && UNKNOWN_UIO_FILE.equals(file.getFilename())));
    }

    @Test
    void shouldIncludeDublinCoreFileEvenWhenContentFileMissing() {
        var contentScraper = new ContentScraper(Path.of("some/Random/Path"),
                                                new BrageLocation(null),
                                                someLicense, null, randomString());
        var resourceContent = contentScraper.scrapeContent();

        assertThat(resourceContent.getContentFiles().size(), is(equalTo(1)));
        assertThat(resourceContent.getContentFiles().get(0).getFilename(), is(equalTo(DUBLIN_CORE_FILE_NAME)));
    }

    @Test
    void shouldIncludeDublinCoreFileEvenWhenContentFileEmpty() {
        var contentScraper = new ContentScraper(Path.of(EMPTY_CONTENT_FILE_PATH),
                                                new BrageLocation(null),
                                                someLicense, null, randomString());
        var resourceContent = contentScraper.scrapeContent();

        assertThat(resourceContent.getContentFiles().size(), is(equalTo(1)));
        assertThat(resourceContent.getContentFiles().get(0).getFilename(), is(equalTo(DUBLIN_CORE_FILE_NAME)));
    }

    @Test
    void shouldSkipInvalidFilesFromContentFilesAndCreateValid(@TempDir Path tempDir) throws Exception {
        var bundle = initiateBundle(tempDir);
        var contentScraper = new ContentScraper(bundle, new BrageLocation(null),
                                                someLicense, null, randomString());
        var filenames = contentScraper.scrapeContent().getContentFiles().stream()
                            .map(ContentFile::getFilename)
                            .collect(Collectors.toList());

        assertThat(filenames, containsInAnyOrder(ORIGINAL_FILENAME_1, ORIGINAL_FILENAME_2, DUBLIN_CORE_FILE_NAME));
    }

    private static Path initiateBundle(Path tempDir) throws IOException {
        var bundle = tempDir.resolve("bundle");
        Files.createDirectories(bundle);
        var contentsFile = bundle.resolve("contents");
        var contentWithInvalidLine = ORIGINAL_FILENAME_1 + "\tbundle:ORIGINAL\tdescription:Valid file\n"
                                     + "invalid_file_line\n"
                                     + ORIGINAL_FILENAME_2 + "\tbundle:ORIGINAL\tdescription:Another valid file";
        Files.writeString(contentsFile, contentWithInvalidLine);

        Files.writeString(bundle.resolve(ORIGINAL_FILENAME_1), "test content 1");
        Files.writeString(bundle.resolve(ORIGINAL_FILENAME_2), "test content 2");
        return bundle;
    }

    private static boolean isNotDublinCoreFile(ContentFile file) {
        return !DUBLIN_CORE_FILE_NAME.equals(file.getFilename());
    }
}

package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.CONTENT_FILE_PATH;
import static no.sikt.nva.ResourceNameConstants.EMPTY_CONTENT_FILE_PATH;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.CONTENT_FILE_MISSING;
import static no.sikt.nva.scrapers.ContentScraper.UNKNOWN_FILE_LOG_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent.BundleType;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.exceptions.ContentException;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class ContentScraperTest {

    public static final String ORIGINAL_FILENAME_1 = "rapport2022_25_1.pdf";
    public static final String ORIGINAL_FILENAME_2 = "rapport2022_25_2.pdf";
    public static final String CUSTOM_LICENSE_PDF_FILENAME = "CustomLicense.pdf";
    public static final String DUBLIN_CORE_FILE_NAME = "dublin_core.xml";
    private static final License someLicense = new License(null, null);
    private ContentScraper contentScraper = new ContentScraper(Path.of(CONTENT_FILE_PATH),
                                                               new BrageLocation(null),
                                                               someLicense, null);

    @Test
    void shouldCreateResourceContentCorrectly() throws ContentException {
        var actualContentFilenameList = contentScraper.scrapeContent()
                                            .getContentFiles()
                                            .stream()
                                            .map(ContentFile::getFilename)
                                            .collect(Collectors.toList());
        assertThat(actualContentFilenameList, containsInAnyOrder(ORIGINAL_FILENAME_1, ORIGINAL_FILENAME_2,
                                                                 CUSTOM_LICENSE_PDF_FILENAME, DUBLIN_CORE_FILE_NAME));
    }

    @Test
    void shouldScrapeNonDefaultLicenseFile() throws ContentException {
        var resourceContent = contentScraper.scrapeContent();
        var licenseFile = resourceContent.getContentFiles().stream()
                              .filter(file -> file.getBundleType().equals(BundleType.LICENSE))
                              .findFirst().orElseThrow();
        assertThat(licenseFile.getFilename(), is(equalTo(CUSTOM_LICENSE_PDF_FILENAME)));
    }

    @Test
    void shouldCreateResourceContentWithEmbargoCorrectly() throws ContentException {
        var expectedEmbargo = LocalDate.parse("3022-08-24").atStartOfDay(ZoneId.systemDefault()).toInstant();
        contentScraper = new ContentScraper(Path.of(CONTENT_FILE_PATH),
                                            new BrageLocation(null),
                                            someLicense, "3022-08-24");
        var actualContentFilenameList = contentScraper.scrapeContent().getContentFiles().stream()
                                            .filter(ContentScraperTest::isNotDublinCoreFile)
                                            .collect(Collectors.toList());
        assertTrue(actualContentFilenameList.stream().allMatch(contentFile -> contentFile.getEmbargoDate().equals(expectedEmbargo)));
        assertThat(actualContentFilenameList.stream().map(ContentFile::getFilename).collect(Collectors.toList()),
                   containsInAnyOrder(ORIGINAL_FILENAME_1, ORIGINAL_FILENAME_2, CUSTOM_LICENSE_PDF_FILENAME));
    }

    private static boolean isNotDublinCoreFile(ContentFile file) {
        return !DUBLIN_CORE_FILE_NAME.equals(file.getFilename());
    }

    @Test
    void shouldNotLogKnownContentFile() throws ContentException {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        contentScraper.scrapeContent();
        assertThat(appender.getMessages(), containsString(UNKNOWN_FILE_LOG_MESSAGE));
    }

    @Test
    void shouldReturnMissingFilesWarningMessageWhenContentFileIsEmpty() throws ContentException {
        var contentScraper = new ContentScraper(Path.of(EMPTY_CONTENT_FILE_PATH),
                                                new BrageLocation(null),
                                                someLicense, null);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        contentScraper.scrapeContent();
        assertThat(appender.getMessages(), containsString(Warning.MISSING_FILES.toString()));
    }

    @Test
    void shouldReturnContentFileMissingErrorMessageWhenContentFileIsNotPresent() throws ContentException {
        var contentScraper = new ContentScraper(Path.of("some/Random/Path"),
                                                new BrageLocation(null),
                                                someLicense, null);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        contentScraper.scrapeContent();
        assertThat(appender.getMessages(), containsString(String.valueOf(CONTENT_FILE_MISSING)));
    }
}

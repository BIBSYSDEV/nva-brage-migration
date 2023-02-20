package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.CONTENT_FILE_PATH;
import static no.sikt.nva.ResourceNameConstants.EMPTY_CONTENT_FILE_PATH;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.CONTENT_FILE_MISSING;
import static no.sikt.nva.scrapers.ContentScraper.UNKNOWN_FILE_LOG_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import java.nio.file.Path;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.exceptions.ContentException;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class ContentScraperTest {

    public static final String ORIGINAL_FILENAME_1 = "rapport2022_25_1.pdf";
    public static final String ORIGINAL_FILENAME_2 = "rapport2022_25_2.pdf";
    private static final License someLicense = new License(null, null);
    private final ContentScraper contentScraper = new ContentScraper(Path.of(CONTENT_FILE_PATH),
                                                                     new BrageLocation(null),
                                                                     someLicense);

    @Test
    void shouldCreateResourceContentCorrectly() throws ContentException {
        var actualContentFilenameList = contentScraper.scrapeContent()
                                            .getContentFiles()
                                            .stream()
                                            .map(ContentFile::getFilename)
                                            .collect(Collectors.toList());
        assertThat(actualContentFilenameList, containsInAnyOrder(ORIGINAL_FILENAME_1, ORIGINAL_FILENAME_2));
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
                                                someLicense);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        contentScraper.scrapeContent();
        assertThat(appender.getMessages(), containsString(Warning.MISSING_FILES.toString()));
    }

    @Test
    void shouldReturnContentFileMissingErrorMessageWhenContentFileIsNotPresent() throws ContentException {
        var contentScraper = new ContentScraper(Path.of("some/Random/Path"),
                                                new BrageLocation(null),
                                                someLicense);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        contentScraper.scrapeContent();
        assertThat(appender.getMessages(), containsString(String.valueOf(CONTENT_FILE_MISSING)));
    }
}

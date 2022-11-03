package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.CONTENT_FILE_PATH;
import static no.sikt.nva.scrapers.ContentScraper.UNKNOWN_FILE_LOG_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import java.nio.file.Path;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.ContentException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.content.ContentFile;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class ContentScraperTest {

    public static final String ORIGINAL_FILENAME_1 = "rapport2022_25_1.pdf";
    public static final String ORIGINAL_FILENAME_2 = "rapport2022_25_2.pdf";
    public static final String TEXT_FILE_NAME = "rapport2022_25.pdf.txt";
    public static final String THUMBNAIL_FILENAME = "rapport2022_25.pdf.jpg";

    @Test
    void shouldCreateResourceContentCorrectly() throws ContentException {
        var actualContentFilenameList = ContentScraper.scrapeContent(Path.of(CONTENT_FILE_PATH),
                                                                     new BrageLocation(null))
                                            .getContentFiles()
                                            .stream()
                                            .map(ContentFile::getFilename)
                                            .collect(Collectors.toList());

        assertThat(actualContentFilenameList, containsInAnyOrder(ORIGINAL_FILENAME_1,
                                                                 ORIGINAL_FILENAME_2,
                                                                 TEXT_FILE_NAME,
                                                                 THUMBNAIL_FILENAME));
    }

    @Test
    void shouldLogUnknownContentFile() throws ContentException {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        ContentScraper.scrapeContent(Path.of(CONTENT_FILE_PATH), new BrageLocation(null));
        assertThat(appender.getMessages(), containsString(UNKNOWN_FILE_LOG_MESSAGE));
    }
}

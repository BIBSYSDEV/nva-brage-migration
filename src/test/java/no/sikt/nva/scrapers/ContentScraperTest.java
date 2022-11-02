package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.CONTENT_FILE_PATH;
import static no.sikt.nva.scrapers.ContentScraper.UNKNOWN_FILE_LOG_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.nio.file.Path;
import java.util.List;
import no.sikt.nva.exceptions.ContentException;
import no.sikt.nva.model.content.ContentFile;
import no.sikt.nva.model.content.ResourceContent;
import no.sikt.nva.model.content.ResourceContent.BundleType;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class ContentScraperTest {

    @Test
    void shouldCreateResourceContentCorrectly() throws ContentException {
        var expectedContent = new ResourceContent();
        var originalFile = new ContentFile("rapport2022_25.pdf", BundleType.ORIGINAL, "Datagrunnlag");
        var text = new ContentFile("rapport2022_25.pdf.txt", BundleType.TEXT, "Extracted text");
        var thumbnail = new ContentFile("rapport2022_25.pdf.jpg", BundleType.THUMBNAIL, "Generated Thumbnail");
        expectedContent.setContentFiles(List.of(originalFile, text, thumbnail));
        var actualContent = ContentScraper.scrapeContent(Path.of(CONTENT_FILE_PATH));
        assertThat(actualContent.getContentFiles(), is(equalTo(expectedContent.getContentFiles())));
    }

    @Test
    void shouldLogUnknownContentFile() throws ContentException {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        ContentScraper.scrapeContent(Path.of(CONTENT_FILE_PATH));
        assertThat(appender.getMessages(), containsString(UNKNOWN_FILE_LOG_MESSAGE));
    }
}

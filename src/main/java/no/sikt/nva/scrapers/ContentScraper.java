package no.sikt.nva.scrapers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import no.sikt.nva.exceptions.ContentException;
import no.sikt.nva.model.content.ContentFile;
import no.sikt.nva.model.content.ResourceContent;
import no.sikt.nva.model.content.ResourceContent.BundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContentScraper {

    public static final int SIZE_3 = 3;
    public static final String CONTENT_FILE_PARSING_ERROR_MESSAGE = "could not parse content file";
    public static final String UNKNOWN_FILE_LOG_MESSAGE = "Unknown file in contents";
    public static final List<String> KNOWN_CONTENT_FILE_TYPES = List.of(BundleType.CCLICENSE.name(),
                                                                        BundleType.LICENSE.name(),
                                                                        BundleType.ORIGINAL.name(),
                                                                        BundleType.TEXT.name(),
                                                                        BundleType.THUMBNAIL.name());
    private static final Logger logger = LoggerFactory.getLogger(ContentScraper.class);

    public static ResourceContent scrapeContent(Path contentFilePath) throws ContentException {
        try {
            return createResourceContent(contentFilePath);
        } catch (Exception e) {
            throw new ContentException(CONTENT_FILE_PARSING_ERROR_MESSAGE);
        }
    }

    private static ResourceContent createResourceContent(Path contentFilePath) throws IOException {
        var contentAsString = Files.readString(contentFilePath);
        var contentFilesList = contentAsString.split("\n");
        var contentFileList = new ArrayList<ContentFile>();

        for (String fileInfo : contentFilesList) {
            var array = Arrays.asList(fileInfo.split("\t"));
            extractOriginalFile(array).ifPresent(contentFileList::add);
            extractText(array).ifPresent(contentFileList::add);
            extractThumbnail(array).ifPresent(contentFileList::add);
            logWhenUnknownType(array);
        }
        return new ResourceContent(contentFileList);
    }

    private static void logWhenUnknownType(List<String> array) {
        if (KNOWN_CONTENT_FILE_TYPES.contains(getBundleType(array))) {
            logger.warn(UNKNOWN_FILE_LOG_MESSAGE);
        }
    }

    private static Optional<ContentFile> extractOriginalFile(List<String> array) {
        if (hasSizeThree(array) && BundleType.ORIGINAL.name().equals(getBundleType(array))) {
            ContentFile contentFile = new ContentFile(getFileName(array), BundleType.ORIGINAL,
                                                      getDescription(array));
            return Optional.of(contentFile);
        }

        return Optional.empty();
    }

    private static Optional<ContentFile> extractText(List<String> array) {
        if (hasSizeThree(array) && BundleType.TEXT.name().equals(getBundleType(array))) {
            ContentFile contentFile = new ContentFile(getFileName(array), BundleType.TEXT, getDescription(array));
            return Optional.of(contentFile);
        }
        return Optional.empty();
    }

    private static Optional<ContentFile> extractThumbnail(List<String> array) {

        if (hasSizeThree(array) && BundleType.THUMBNAIL.name().equals(getBundleType(array))) {
            ContentFile contentFile = new ContentFile(getFileName(array), BundleType.THUMBNAIL,
                                                      getDescription(array));
            return Optional.of(contentFile);
        }

        return Optional.empty();
    }

    private static String getFileName(List<String> list) {
        return list.get(0);
    }

    private static String getDescription(List<String> list) {
        return Arrays.asList(list.get(list.size() - 1).split(":")).get(1);
    }

    private static String getBundleType(List<String> list) {
        return Arrays.asList(list.get(1).split(":")).get(1);
    }

    private static boolean hasSizeThree(List<String> array) {
        return array.size() == SIZE_3;
    }
}

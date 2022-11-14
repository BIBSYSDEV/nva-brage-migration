package no.sikt.nva.scrapers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.ContentException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.content.ContentFile;
import no.sikt.nva.model.content.ResourceContent;
import no.sikt.nva.model.content.ResourceContent.BundleType;
import no.sikt.nva.model.record.License;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContentScraper {

    public static final String CONTENT_FILE_PARSING_ERROR_MESSAGE = "could not parse content file";
    public static final String UNKNOWN_FILE_LOG_MESSAGE = "Unknown file in contents: ";
    public static final List<String> KNOWN_CONTENT_FILE_TYPES = List.of(BundleType.CCLICENSE.getValue(),
                                                                        BundleType.LICENSE.getValue(),
                                                                        BundleType.ORIGINAL.getValue(),
                                                                        BundleType.TEXT.getValue(),
                                                                        BundleType.THUMBNAIL.getValue(),
                                                                        BundleType.SWORD.getValue(),
                                                                        BundleType.ORE.getValue(),
                                                                        BundleType.METADATA.getValue());
    public static final String EMPTY_LINE_REGEX = "(?m)(^\\s*$\\r?\\n)+";
    private static final Logger logger = LoggerFactory.getLogger(ContentScraper.class);
    private final Path contentFilePath;
    private final BrageLocation brageLocation;
    private final License license;

    public ContentScraper(Path contentFilePath, BrageLocation brageLocation, License license) {
        this.contentFilePath = contentFilePath;
        this.brageLocation = brageLocation;
        this.license = license;
    }

    public ResourceContent scrapeContent()
        throws ContentException {
        try {
            return createResourceContent();
        } catch (Exception e) {
            throw new ContentException(CONTENT_FILE_PARSING_ERROR_MESSAGE);
        }
    }

    private static boolean isOriginalFileBundle(List<String> fileInformationList) {
        return BundleType.ORIGINAL.name().equals(getBundleType(fileInformationList));
    }

    private static BundleType extractBundleType(List<String> fileInformationList) {
        return BundleType.valueOf(getBundleType(fileInformationList));
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

    private ResourceContent createResourceContent()
        throws IOException {
        var contentFileAsString = Files.readString(contentFilePath).replaceAll(EMPTY_LINE_REGEX,
                                                                               StringUtils.EMPTY_STRING);
        var contentFilesFromListAsString = contentFileAsString.split("\n");
        var contentFileList = Arrays.stream(contentFilesFromListAsString)
                                  .map(this::convertToFile)
                                  .flatMap(Optional::stream)
                                  .collect(Collectors.toList());
        return new ResourceContent(contentFileList);
    }

    private Optional<ContentFile> convertToFile(String fileInfo) {
        var fileInformationList = Arrays.asList(fileInfo.split("\t"));
        if (isOriginalFileBundle(fileInformationList)) {
            return Optional.of(extractFileContent(fileInformationList));
        } else {
            logWhenUnknownType(fileInformationList);
            return Optional.empty();
        }
    }

    private ContentFile extractFileContent(List<String> fileInformationList) {
        var contentFile = new ContentFile();
        contentFile.setFilename(getFileName(fileInformationList));
        contentFile.setLicense(license);
        contentFile.setDescription(getDescription(fileInformationList));
        contentFile.setBundleType(extractBundleType(fileInformationList));
        contentFile.setIdentifier(UUID.randomUUID());
        return contentFile;
    }

    private void logWhenUnknownType(List<String> fileInformationList) {
        var type = getBundleType(fileInformationList);
        if (!KNOWN_CONTENT_FILE_TYPES.contains(type)) {
            logger.warn(UNKNOWN_FILE_LOG_MESSAGE
                        + getBundleType(fileInformationList)
                        + StringUtils.SPACE
                        + brageLocation.getOriginInformation());
        }
    }
}

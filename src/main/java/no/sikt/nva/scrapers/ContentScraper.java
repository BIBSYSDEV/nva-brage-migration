package no.sikt.nva.scrapers;

import static java.util.Objects.nonNull;
import static no.sikt.nva.scrapers.CustomerMapper.UIO;
import static nva.commons.core.StringUtils.isEmpty;
import static nva.commons.core.attempt.Try.attempt;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.ErrorDetails.Error;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent.BundleType;
import no.sikt.nva.brage.migration.common.model.record.license.License;
import no.sikt.nva.exceptions.ContentException;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContentScraper {

    private static final String CONTENT_FILE_DEFAULT_NAME = "contents";
    public static final String CONTENT_FILE_PARSING_ERROR_MESSAGE = "Could not parse content file: ";
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
    public static final String DEFAULT_LICENSE_FILENAME = "license.txt";
    private static final Logger logger = LoggerFactory.getLogger(ContentScraper.class);
    private final Path bundlePath;
    private final BrageLocation brageLocation;
    private final License license;
    private final String embargo;
    private final String customer;

    public ContentScraper(Path bundlePath, BrageLocation brageLocation, License license, String embargo,
                          String customer) {
        this.bundlePath = bundlePath;
        this.brageLocation = brageLocation;
        this.license = license;
        this.embargo = embargo;
        this.customer = customer;
    }

    public ResourceContent scrapeContent() throws ContentException {
        try {
            return createResourceContent();
        } catch (Exception e) {
            var contentFile = new File(String.valueOf(bundlePath.resolve(CONTENT_FILE_DEFAULT_NAME)));
            if (!contentFile.exists()) {
                logger.error(new ErrorDetails(Error.CONTENT_FILE_MISSING).toString());
                return ResourceContent.emptyResourceContent();
            }
            var stringFromFile = attempt(() -> Files.readString(bundlePath.resolve(CONTENT_FILE_DEFAULT_NAME))).orElseThrow();
            if (isEmpty(stringFromFile)) {
                logger.info(new WarningDetails(Warning.MISSING_FILES).toString());
                return ResourceContent.emptyResourceContent();
            } else {
                throw new ContentException(CONTENT_FILE_PARSING_ERROR_MESSAGE + e.getMessage());
            }
        }
    }

    private static boolean isOriginalFileBundle(List<String> fileInformationList) {
        return BundleType.ORIGINAL.name().equals(getBundleType(fileInformationList));
    }

    private static BundleType extractBundleType(List<String> fileInformationList) {
        return attempt(() -> BundleType.valueOf(getBundleType(fileInformationList)))
                   .orElse(failure -> BundleType.IGNORED);
    }

    private static String getFileName(List<String> list) {
        return list.get(0);
    }

    private static String getDescription(List<String> list) {
        if (hasMoreThanThreeItems(list)) {
            return list.get(list.size() - 1);
        } else {
            return Arrays.asList(list.get(list.size() - 1).split(":")).get(1);
        }
    }

    private static boolean hasMoreThanThreeItems(List<String> list) {
        return list.size() > 3;
    }

    private static String getBundleType(List<String> list) {
        return Arrays.asList(list.get(1).split(":")).get(1);
    }

    private ResourceContent createResourceContent()
        throws IOException {
        var contentFileAsString = Files.readString(bundlePath.resolve(CONTENT_FILE_DEFAULT_NAME))
                                      .replaceAll(EMPTY_LINE_REGEX, StringUtils.EMPTY_STRING);
        var contentFilesFromListAsString = contentFileAsString.split("\n");
        var contentFileList = Arrays.stream(contentFilesFromListAsString)
                                  .map(this::convertToFile)
                                  .flatMap(Optional::stream)
                                  .collect(Collectors.toList());
        contentFileList.add(createDublinCoreFile());
        return new ResourceContent(contentFileList);
    }

    private ContentFile createDublinCoreFile() {
        return new ContentFile("dublin_core.xml", BundleType.IGNORED, null, UUID.randomUUID(), null, null);
    }

    private Optional<ContentFile> convertToFile(String fileInfo) {
        var fileInformationList = Arrays.asList(fileInfo.split("\t"));
        var fileName = getFileName(fileInformationList);
        if (!Files.exists(bundlePath.resolve(fileName))) {
            logger.warn("File {} does not exist at brage location {}", fileName, brageLocation);
            return Optional.empty();
        }
        if (isOriginalFileBundle(fileInformationList)
            || isNonDefaultLicense(fileInformationList)
            || shouldBeMigratedAsHiddenFile(fileInformationList)) {
            return Optional.of(extractFileContent(fileInformationList));
        } else {
            logWhenUnknownType(fileInformationList);
            return Optional.empty();
        }
    }

    private boolean shouldBeMigratedAsHiddenFile(List<String> fileInformationList) {
        return !isOriginalFileBundle(fileInformationList)
               && !isSwordFile(fileInformationList)
               && UIO.equals(customer);
    }

    private boolean isSwordFile(List<String> fileInformationList) {
        return BundleType.SWORD.name().equals(getBundleType(fileInformationList));
    }

    private boolean isNonDefaultLicense(List<String> fileInformationList) {
        return BundleType.LICENSE.name().equals(getBundleType(fileInformationList))
               && isNotDefaultLicense(fileInformationList);
        
    }

    private static boolean isNotDefaultLicense(List<String> fileInformationList) {
        return !DEFAULT_LICENSE_FILENAME.equals(getFileName(fileInformationList));
    }

    private ContentFile extractFileContent(List<String> fileInformationList) {
        var contentFile = new ContentFile();
        contentFile.setFilename(getFileName(fileInformationList));
        contentFile.setLicense(license);
        contentFile.setDescription(getDescription(fileInformationList));
        contentFile.setBundleType(extractBundleType(fileInformationList));
        contentFile.setIdentifier(UUID.randomUUID());
        contentFile.setEmbargoDate(convertEmbargoToInstant());
        return contentFile;
    }

    private Instant convertEmbargoToInstant() {
        return nonNull(embargo) ? toInstant() : null;
    }

    private Instant toInstant() {
        return attempt(() -> LocalDate.parse(embargo).atStartOfDay(ZoneId.systemDefault()).toInstant())
                   .orElse(failure -> null);
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

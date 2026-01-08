package no.sikt.nva.scrapers;

import static java.util.Objects.nonNull;
import static no.sikt.nva.scrapers.CustomerMapper.UIO;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.StringUtils.isEmpty;
import static nva.commons.core.attempt.Try.attempt;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
import nva.commons.core.StringUtils;
import nva.commons.core.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContentScraper {

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
    private final Path contentFilePath;
    private final BrageLocation brageLocation;
    private final License license;
    private final String embargo;
    private final String customer;

    public ContentScraper(Path contentFilePath, BrageLocation brageLocation, License license, String embargo,
                          String customer) {
        this.contentFilePath = contentFilePath;
        this.brageLocation = brageLocation;
        this.license = license;
        this.embargo = embargo;
        this.customer = customer;
    }

    public ResourceContent scrapeContent() {
        var contentFileList = parseContentFile();
        contentFileList.add(createDublinCoreFile());
        return new ResourceContent(contentFileList);
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

    private List<ContentFile> parseContentFile() {
        var contentFile = new File(String.valueOf(contentFilePath));
        if (!contentFile.exists()) {
            logger.error(new ErrorDetails(Error.CONTENT_FILE_MISSING).toString());
            return new ArrayList<>();
        }

        var contentFileAsString = attempt(() -> Files.readString(contentFilePath)).orElse(this::emptyString);

        if (isEmpty(contentFileAsString)) {
            logger.info(new WarningDetails(Warning.MISSING_FILES).toString());
            return new ArrayList<>();
        }

        var cleanedContent = contentFileAsString.replaceAll(EMPTY_LINE_REGEX, EMPTY_STRING);
        var contentFilesFromListAsString = cleanedContent.split("\n");

        return Arrays.stream(contentFilesFromListAsString)
                   .map(this::convertToFileWithErrorHandling)
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private String emptyString(Failure<String> failure) {
        logger.error("Failed to read content file: " + failure.getException().getMessage());
        return EMPTY_STRING;
    }

    private Optional<ContentFile> convertToFileWithErrorHandling(String fileInfo) {
        try {
            return convertToFile(fileInfo);
        } catch (Exception e) {
            logger.warn("Failed to parse file line: " + fileInfo + " - " + e.getMessage());
            return Optional.empty();
        }
    }

    private ContentFile createDublinCoreFile() {
        return new ContentFile("dublin_core.xml", BundleType.IGNORED, null, UUID.randomUUID(), null, null);
    }

    private Optional<ContentFile> convertToFile(String fileInfo) {
        var fileInformationList = Arrays.asList(fileInfo.split("\t"));
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

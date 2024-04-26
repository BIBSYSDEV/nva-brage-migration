package no.sikt.nva.brage.migration.aws;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3StorageImpl implements S3Storage {

    public static final String DEFAULT_ERROR_FILENAME = "%s-application-error.log";
    public static final String DEFAULT_WARNING_FILENAME = "%s-application-warn.log";
    public static final String DEFAULT_INFO_FILENAME = "%s-application-info.log";
    public static final String COULD_NOT_WRITE_RECORD_MESSAGE = "Could not write files to s3 for: ";
    public static final String COULD_NOT_WRITE_LOGS_MESSAGE = "Could not write logs to s3: ";
    public static final String JSON_STRING = ".json";
    public static final String APPLICATION_JSON = "application/json";
    public static final String PATH_DELIMITER = "/";
    public static final String EXPERIMENTAL_BUCKET_NAME = "anette-kir-brage-migration-experiment";
    public static final String SANDBOX_BUCKET_NAME = "brage-migration-input-files-750639270376";
    public static final String PROD_BUCKET_NAME = "brage-migration-input-files-755923822223";

    public static final String TEST_BUCKET_NAME = "brage-migration-input-files-812481234721";
    public static final String RECORDS_JSON_FILE_NAME = "records.json";
    public static final String PROBLEM_PUSHING_PROCESSED_RECORDS_TO_S3 = "Problem pushing processed records to S3: ";
    private static final String DEVELOP_BUCKET_NAME = "brage-migration-input-files-884807050265";
    private static final ColoredLogger logger = ColoredLogger.create(S3StorageImpl.class);
    public static final String ZIP = ".zip";
    public static final String FILE_DOES_NOT_EXIST_MESSAGE = "FILE DOES NOT EXIST ";
    public static final String SUCCESSFULLY_PROCEEDED_MESSAGE = "Successfully proceeded records to AWS: ";
    private final String bucketName;
    private final S3Client s3Client;
    private final String pathPrefixString;
    private final String customer;

    public S3StorageImpl(S3Client s3Client, String pathPrefixString, String customer, String awsBucket) {
        this.s3Client = s3Client;
        this.pathPrefixString = pathPrefixString;
        this.customer = customer;
        this.bucketName = determineBucketFromAwsEnvironment(awsBucket);
    }

    @Override
    public void storeRecord(Record record) {
        try {
            writeAssociatedFilesToS3(record);
            writeRecordToS3(record);
            logger.info(record.getId() + " stored to aws");
        } catch (Exception e) {
            logger.error(COULD_NOT_WRITE_RECORD_MESSAGE + record.getBrageLocation() + " " + e);
        }
    }

    @Override
    public void storeLogs(String customer) {
        try {
            writeLogsToS3(customer);
        } catch (Exception e) {
            logger.error(COULD_NOT_WRITE_LOGS_MESSAGE + e.getMessage());
        }
    }

    @Override
    public void storeProcessedCollections(String[] collections) {
        try {
            var collectionFiles = getCollections(stripZipBundlesToCollections(collections));
            var listOfRecordsCollections = getRecordsJsonFiles(collectionFiles);
            var records = extractRecords(listOfRecordsCollections);
            records.forEach(this::storeRecord);
            logSuccessfullyProcessedRecords(records);
            writeLogsToS3(customer);
        } catch (Exception e) {
            logger.error(PROBLEM_PUSHING_PROCESSED_RECORDS_TO_S3 + e);
        }
    }

    private static void logSuccessfullyProcessedRecords(List<Record> records) {
        logger.info(String.join(StringUtils.EMPTY_STRING, SUCCESSFULLY_PROCEEDED_MESSAGE,
                                String.valueOf(records.size())));
    }

    @Override
    public void storeInputFile(String startingDirectory, String filename) {
        var file = new File(Path.of(startingDirectory, filename).toString());
        if (file.exists() && file.isFile()) {
            writeFileToS3(file);
        }
    }

    public String getPathPrefixString() {
        return pathPrefixString;
    }

    public List<Record> readRecordsFromFile(File recordsFile) throws IOException {
        return attempt(() -> readFileAsString(getPathPrefixString() + recordsFile.getPath()))
                   .map(s -> JsonUtils.dtoObjectMapper.readValue(s, Record[].class))
                   .map(Arrays::asList)
                   .orElse(failure -> List.<Record>of());
    }

    private static String readFileAsString(String path) throws IOException {
        try {
            return Files.readString(Path.of(path));
        } catch (NoSuchFileException e) {
            logger.error(FILE_DOES_NOT_EXIST_MESSAGE + path);
            return null;
        }
    }

    public static String determineBucketFromAwsEnvironment(String awsBucket) {
        switch (awsBucket) {
            case "sandbox":
                return SANDBOX_BUCKET_NAME;
            case "dev":
                return DEVELOP_BUCKET_NAME;
            case "prod":
                return PROD_BUCKET_NAME;
            case "test":
                return TEST_BUCKET_NAME;
            default:
                return EXPERIMENTAL_BUCKET_NAME;
        }
    }

    private static List<File> getRecordsJsonFiles(List<File> collectionFiles) {
        return collectionFiles.stream()
                   .map(file -> new File(file.getName() + PATH_DELIMITER + RECORDS_JSON_FILE_NAME))
                   .collect(Collectors.toList());
    }

    private static List<String> stripZipBundlesToCollections(String... collections) {
        return Arrays.stream(collections)
                   .map(collectionString -> collectionString.replaceAll(ZIP, StringUtils.EMPTY_STRING))
                   .collect(Collectors.toList());
    }

    private static String extractHandleForFilename(Record record) {
        var handleToList = Arrays.asList(record.getId().toString().split(PATH_DELIMITER));
        return handleToList.get(handleToList.size() - 1) + JSON_STRING;
    }

    private static void addFilePathToMap(ConcurrentMap<UUID, File> map,
                                         ContentFile contentFile,
                                         Record record,
                                         String filePath) {
        try {
            var file = new File(filePath);
            map.put(contentFile.getIdentifier(), file);
        } catch (Exception e) {
            logger.error("FILE NOT FOUND: "  + filePath + ", in bundle: " + record.getBrageLocation());
        }
    }

    private List<Record> extractRecords(List<File> listOfRecordsCollections) throws IOException {
        List<Record> records = new ArrayList<>();
        for (File file : listOfRecordsCollections) {
            if (nonNull(file)) {
                records.addAll(readRecordsFromFile(file));
            }
        }
        return records;
    }

    private List<File> getCollections(List<String> bundles) {
        return bundles.stream().map(File::new).collect(Collectors.toList());
    }

    private File findAssociatedFiles(Record record) {
        return new File(getPathPrefixString() + record.getBrageLocation());
    }

    private String getCollectionDirectory(Record record) {
        var brageLocation = record.getBrageLocation().split(PATH_DELIMITER);
        return brageLocation[brageLocation.length - 2];
    }

    private String getResourceDirectory(Record record) {
        var brageLocation = record.getBrageLocation().split(PATH_DELIMITER);
        return brageLocation[brageLocation.length - 1];
    }

    private String createKey(Record record, String filename) {
        if (StringUtils.isBlank(record.getBrageLocation())) {
            return Path.of(customer, record.getCristinId(), filename).toString();
        }
        var collection = getCollectionDirectory(record);
        var bundle = getResourceDirectory(record);
        return Path.of(customer, collection, bundle, filename).toString();
    }

    private void writeRecordToS3(Record record) throws JsonProcessingException {
        var recordToStore = record.toJsonString();
        var recordKey = createKey(record, extractHandleForFilename(record));
        s3Client.putObject(PutObjectRequest
                               .builder()
                               .bucket(bucketName)
                               .contentType(APPLICATION_JSON)
                               .key(recordKey)
                               .build(),
                           RequestBody.fromString(recordToStore));
    }

    private void writeAssociatedFilesToS3(Record record) {
        var filesToStore = getMappedFiles(record);
        for (UUID fileId : filesToStore.keySet()) {
            var fileKey = createKey(record, fileId.toString());
            var file = filesToStore.get(fileId);
            var fileName = record.getContentBundle().getContentFiles().stream()
                               .filter(contentFile -> contentFile.getIdentifier().equals(fileId))
                               .map(ContentFile::getFilename)
                               .findFirst()
                               .orElse(null);

            S3MultipartUploader.fromKey(fileKey)
                .bucket(bucketName)
                .fileName(fileName)
                .file(file)
                .upload(s3Client);
        }
    }

    private void writeLogsToS3(String customer) {
        var errorFile = new File(getPathPrefixString() + String.format(DEFAULT_ERROR_FILENAME, customer));
        var warningFile = new File(getPathPrefixString() + String.format(DEFAULT_WARNING_FILENAME, customer));
        var infoFile = new File(getPathPrefixString() + String.format(DEFAULT_INFO_FILENAME, customer));
        List.of(errorFile, warningFile, infoFile).forEach(this::writeFileToS3);
    }

    private void writeFileToS3(File file) {
        s3Client.putObject(PutObjectRequest
                               .builder()
                               .bucket(bucketName)
                               .key(customer + PATH_DELIMITER + file.getName())
                               .build(),
                           RequestBody.fromFile(file));
    }

    private Map<UUID, File> getMappedFiles(Record record) {
        var resourceFiles = findAssociatedFiles(record);
        return mapFilesToUuid(record, resourceFiles);
    }

    private ConcurrentMap<UUID, File> mapFilesToUuid(Record record, File resourceFiles) {
        ConcurrentMap<UUID, File> map = new ConcurrentHashMap<>();
        var contentFiles = Optional.ofNullable(record.getContentBundle())
                               .map(ResourceContent::getContentFiles)
                                   .orElse(List.of());
        contentFiles.forEach(contentFile -> addToMap(map,
                                                     contentFile,
                                                     resourceFiles,
                                                     record));
        return map;
    }

    private void addToMap(ConcurrentMap<UUID, File> map,
                          ContentFile contentFile,
                          File resourceFiles,
                          Record record) {
        try {
            var filePath = resourceFiles.getPath() + PATH_DELIMITER + contentFile.getFilename();
            addFilePathToMap(map, contentFile, record, filePath);
        } catch (Exception e) {
            logger.error("COULD NOT CRAFT CONTENTS FILE PATH: " + record.getBrageLocation());
        }
    }
}

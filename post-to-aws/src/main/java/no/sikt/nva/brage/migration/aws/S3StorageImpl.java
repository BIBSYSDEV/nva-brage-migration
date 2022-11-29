package no.sikt.nva.brage.migration.aws;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3StorageImpl implements S3Storage {

    public static final String DEFAULT_ERROR_FILENAME = "application-error.log";
    public static final String DEFAULT_WARNING_FILENAME = "application-warn.log";
    public static final String DEFAULT_INFO_FILENAME = "application-info.log";
    public static final String COULD_NOT_WRITE_RECORD_MESSAGE = "Could not write files to s3 for: ";
    public static final String COULD_NOT_WRITE_LOGS_MESSAGE = "Could not write logs to s3: ";
    public static final String JSON_STRING = ".json";
    public static final String APPLICATION_JSON = "application/json";
    public static final String bucketName = "anette-kir-brage-migration-experiment";
    public static final String RECORDS_JSON_FILE_NAME = "records.json";
    public static final String PROBLEM_PUSHING_PROCESSED_RECORDS_TO_S3 = "Problem pushing processed records to S3: ";
    private static final Logger logger = LoggerFactory.getLogger(S3StorageImpl.class);
    private final S3Client s3Client;
    private final String pathPrefixString;
    private final String customer;

    public S3StorageImpl(S3Client s3Client, String pathPrefixString, String customer) {
        this.s3Client = s3Client;
        this.pathPrefixString = pathPrefixString;
        this.customer = customer;
    }

    public S3StorageImpl(S3Client s3Client, String customer) {
        this.s3Client = s3Client;
        this.pathPrefixString = StringUtils.EMPTY_STRING;
        this.customer = customer;
    }

    @Override
    public void storeRecord(Record record) {
        try {
            writeAssociatedFilesToS3(record);
            writeRecordToS3(record);
        } catch (Exception e) {
            logger.info(COULD_NOT_WRITE_RECORD_MESSAGE + record.getBrageLocation() + " " + e.getMessage());
        }
    }

    @Override
    public void storeLogs() {
        try {
            writeLogsToS3();
        } catch (Exception e) {
            logger.info(COULD_NOT_WRITE_LOGS_MESSAGE + e.getMessage());
        }
    }

    @Override
    public void storeProcessedCollections(String[] collections) {
        try {
            var collectionFiles = getCollections(stripZipBundlesToCollections(collections));
            var listOfRecordsCollections = gerRecordsJsonFiles(collectionFiles);
            extractRecords(listOfRecordsCollections).forEach(this::storeRecord);
            writeLogsToS3();
        } catch (Exception e) {
            logger.info(PROBLEM_PUSHING_PROCESSED_RECORDS_TO_S3 + e.getMessage());
        }
    }

    public String getPathPrefixString() {
        return pathPrefixString;
    }

    public List<Record> readRecordsFromFile(File recordsFile) throws IOException {
        var path = getPathPrefixString() + recordsFile.getPath();
        var recordsAsString = Files.readString(Path.of(path));
        Record[] a = JsonUtils.dtoObjectMapper.readValue(recordsAsString, Record[].class);
        return Arrays.asList(a);
    }

    private static List<File> gerRecordsJsonFiles(List<File> collectionFiles) {
        return collectionFiles.stream()
                   .map(file -> new File(file.getName() + "/" + RECORDS_JSON_FILE_NAME))
                   .collect(Collectors.toList());
    }

    private static List<String> stripZipBundlesToCollections(String... collections) {
        return Arrays.stream(collections)
                   .map(collectionString -> collectionString.replaceAll(".zip", ""))
                   .collect(Collectors.toList());
    }

    private static String extractHandleForFilename(Record record) {
        var handleToList = Arrays.asList(record.getId().toString().split("/"));
        return handleToList.get(handleToList.size() - 1) + JSON_STRING;
    }

    private static String getResourceDirectoryName(String brageLocation) {
        return brageLocation.split("/")[1];
    }

    private static UUID getUuidToRecordFile(Record record, File file) {
        return record.getContentBundle().getContentFileByFilename(file.getName()).getIdentifier();
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
        var brageLocation = record.getBrageLocation();
        return new File(getCollectionDirectory(brageLocation) + "/" + getResourceDirectory(brageLocation));
    }

    private String createKey(Record record, String filename) {
        var collection = getCollectionDirectory(record.getBrageLocation());
        var bundle = getResourceDirectoryName(record.getBrageLocation());
        return Path.of(customer, collection.getName(), bundle, filename).toString();
    }

    private File getCollectionDirectory(String brageLocation) {
        return new File(getPathPrefixString() + brageLocation.split("/")[0]);
    }

    private String getResourceDirectory(String brageLocation) {
        return brageLocation.split("/")[1];
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
            var fileName = URLEncoder.encode(filesToStore.get(fileId).getName(), StandardCharsets.UTF_8);
            s3Client.putObject(PutObjectRequest
                                   .builder()
                                   .bucket(bucketName)
                                   .contentDisposition(fileName)
                                   .key(fileKey).build(),
                               RequestBody.fromFile(filesToStore.get(fileId)));
        }
    }

    private void writeLogsToS3() {
        var errorFile = new File(getPathPrefixString() + DEFAULT_ERROR_FILENAME);
        var warningFile = new File(getPathPrefixString() + DEFAULT_WARNING_FILENAME);
        var infoFile = new File(getPathPrefixString() + DEFAULT_INFO_FILENAME);
        List.of(errorFile, warningFile, infoFile).forEach(this::writeLogFileToS3);
    }

    private void writeLogFileToS3(File file) {
        s3Client.putObject(PutObjectRequest
                               .builder()
                               .bucket(bucketName)
                               .key(customer + "/" + file.getName())
                               .build(),
                           RequestBody.fromFile(file));
    }

    private Map<UUID, File> getMappedFiles(Record record) {
        var resourceFiles = findAssociatedFiles(record);
        return mapFilesToUuid(record, resourceFiles);
    }

    private ConcurrentMap<UUID, File> mapFilesToUuid(Record record, File resourceFiles) {
        ConcurrentMap<UUID, File> map = new ConcurrentHashMap<>();
        Arrays.stream(Objects.requireNonNull(resourceFiles.listFiles()))
            .filter(file -> isPartOfRecord(file, record))
            .forEach(file -> map.put(getUuidToRecordFile(record, file), file));
        return map;
    }

    private boolean isPartOfRecord(File file, Record record) {
        var recordFiles = record.getContentBundle()
                              .getContentFiles()
                              .stream()
                              .map(ContentFile::getFilename)
                              .collect(Collectors.toList());
        return recordFiles.contains(file.getName());
    }
}

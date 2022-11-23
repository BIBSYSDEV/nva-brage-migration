package no.sikt.nva.brage.migration.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3RecordStorage implements StoreRecord {

    public static final String COULD_NOT_WRITE_MESSAGE = "Could not write files to s3 for: ";
    public static final String JSON_STRING = ".json";
    public static final String APPLICATION_JSON = "application/json";
    public static final String bucketName = "anette-kir-brage-migration-experiment";
    private static final Logger logger = LoggerFactory.getLogger(S3RecordStorage.class);
    private final S3Client s3Client;
    private final String pathPrefixString;

    public S3RecordStorage(S3Client s3Client, String pathPrefixString) {
        this.s3Client = s3Client;
        this.pathPrefixString = pathPrefixString;
    }

    public S3RecordStorage(S3Client s3Client) {
        this.s3Client = s3Client;
        this.pathPrefixString = StringUtils.EMPTY_STRING;
    }

    @Override
    public void storeRecord(Record record) {
        try {
            writeAssociatedFilesToS3(record);
            writeRecordToS3(record);
        } catch (Exception e) {
            logger.info(COULD_NOT_WRITE_MESSAGE + record.getBrageLocation() + " " + e.getMessage());
        }
    }

    public String getPathPrefixString() {
        return pathPrefixString;
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

    private File findAssociatedFiles(Record record) {
        var brageLocation = record.getBrageLocation();
        return new File(getCollectionDirectory(brageLocation) + "/" + getResourceDirectory(brageLocation));
    }

    private String createKey(Record record, String filename) {
        var collection = getCollectionDirectory(record.getBrageLocation());
        var bundle = getResourceDirectoryName(record.getBrageLocation());
        var hardCodedCustomer = "nve";
        return Path.of(hardCodedCustomer, collection.getName(), bundle, filename).toString();
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
            var fileName = filesToStore.get(fileId).getName();
            s3Client.putObject(PutObjectRequest
                                   .builder()
                                   .bucket(bucketName)
                                   .contentDisposition(fileName)
                                   .key(fileKey).build(),
                               RequestBody.fromFile(filesToStore.get(fileId)));
        }
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

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
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.SingletonCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@JacocoGenerated
public class S3RecordStorage implements StoreRecord {

    public static final String COULD_NOT_WRITE_MESSAGE = "Could not write files to s3 for: ";
    public static final String JSON_STRING = ".json";
    public static final String APPLICATION_JSON = "application/json";
    private static final String bucketName = "anette-kir-brage-migration-experiment";
    private static final Logger logger = LoggerFactory.getLogger(S3RecordStorage.class);
    private final S3Client s3Client;

    public S3RecordStorage(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void storeRecord(Record record) {
        var filesToStore = getFilesToStore(record);
        var collection = getCollectionDirectory(record.getBrageLocation()).getName();
        var bundle = getResourceDirectoryName(record.getBrageLocation());
        var hardCodedCustomer = "nve";
        try {
            var recordToStore = convertSingleRecordToJsonString(record);
            for (UUID fileId : filesToStore.keySet()) {
                var fileKey = Path.of(hardCodedCustomer, collection, bundle, fileId.toString()).toString();
                var fileName = filesToStore.get(fileId).getName();
                s3Client.putObject(PutObjectRequest
                                       .builder()
                                       .bucket(bucketName)
                                       .contentDisposition(fileName)
                                       .key(fileKey).build(),
                                   RequestBody.fromFile(filesToStore.get(fileId)));
            }
            var recordKey = Path.of(hardCodedCustomer, collection, bundle, extractHandleForFilename(record)).toString();
            s3Client.putObject(PutObjectRequest
                                   .builder()
                                   .bucket(bucketName)
                                   .contentType(APPLICATION_JSON)
                                   .key(recordKey)
                                   .build(),
                               RequestBody.fromString(recordToStore));
        } catch (Exception e) {
            logger.error(COULD_NOT_WRITE_MESSAGE + record.getBrageLocation() + " " + e.getMessage());
        }
    }

    private static String extractHandleForFilename(Record record) {
        var handleToList = Arrays.asList(record.getId().toString().split("/"));
        return handleToList.get(handleToList.size() - 1) + JSON_STRING;
    }

    private static String getResourceDirectoryName(String brageLocation) {
        return brageLocation.split("/")[1];
    }

    private static File getCollectionDirectory(String brageLocation) {
        return new File(brageLocation.split("/")[0]);
    }

    private static String convertSingleRecordToJsonString(Record record) throws JsonProcessingException {
        return JsonUtils.dtoObjectMapper.writeValueAsString(record);
    }

    private static UUID getUuid(Record record, File file) {
        return record.getContentBundle().getContentFileByFilename(file.getName()).getIdentifier();
    }

    private Map<UUID, File> getFilesToStore(Record record) {
        var brageLocation = record.getBrageLocation();
        var collectionDirectory = getCollectionDirectory(brageLocation);
        var resourceDirectoryName = brageLocation.split("/")[1];
        var resourceFiles = Arrays.stream(Objects.requireNonNull(collectionDirectory.listFiles()))
                                .filter(resourceDir -> resourceDir.getName().equals(resourceDirectoryName))
                                .collect(SingletonCollector.collect());
        ConcurrentMap<UUID, File> map = new ConcurrentHashMap<>();
        Arrays.stream(Objects.requireNonNull(resourceFiles.listFiles()))
            .filter(file -> isPartOfRecord(file, record))
            .forEach(file -> map.put(getUuid(record, file), file));
        return map;
    }

    private boolean isPartOfRecord(File file, Record record) {
        var originalFiles = record.getContentBundle()
                                .getContentFiles()
                                .stream()
                                .map(ContentFile::getFilename)
                                .collect(Collectors.toList());
        return originalFiles.contains(file.getName());
    }
}

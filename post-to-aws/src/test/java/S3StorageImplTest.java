import static no.sikt.nva.brage.migration.aws.S3StorageImpl.DEFAULT_ERROR_FILENAME;
import static no.sikt.nva.brage.migration.aws.S3StorageImpl.DEFAULT_INFO_FILENAME;
import static no.sikt.nva.brage.migration.aws.S3StorageImpl.DEFAULT_WARNING_FILENAME;
import static no.sikt.nva.brage.migration.aws.S3StorageImpl.PATH_DELIMITER;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.aws.S3StorageImpl;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

public class S3StorageImplTest {

    public static final String TEST_PATH = "src/test/resources/";
    public static final String VALID_TEST_FILE_NAME = "Simulated precipitation fields with variance consistent "
                                                      + "interpolation.pdf";
    public static final String CUSTOMER = "CUSTOMER";
    private static final String EXPERIMENTAL_BUCKET_SETTTING = "experimental";
    public static final String SAMLINGSFIL_TXT = "samlingsfil.txt";

    @Test
    void shouldUploadRecordAndFileToS3() {
        Record testRecord = createValidTestRecord();
        var expectedKeyToRecord = "CUSTOMER/11/1/1.json";
        var fileIdentifier = getFileByName(testRecord,VALID_TEST_FILE_NAME);
        var expectedKeyToFile = "CUSTOMER/11/1/" + fileIdentifier;
        var s3Client = new FakeMultipartUploadS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        storageClient.storeRecord(testRecord);
        var actualRecordKeyFromBucket = s3Client.getKeyByIdentifier(expectedKeyToRecord);
        var actualFileKeyFromBucket = s3Client.getKeyByIdentifier(fileIdentifier);

        assertThat(actualRecordKeyFromBucket, is(equalTo(expectedKeyToRecord)));
        assertThat(actualFileKeyFromBucket, is(equalTo(expectedKeyToFile)));
    }

    private static UUID getFileByName(Record testRecord, String filename) {
        return testRecord.getContentBundle().getContentFileByFilename(filename).getIdentifier();
    }

    @Test
    void shouldUploadLogFilesToS3() {
        var s3Client = new FakeMultipartUploadS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        var customer = "someCustomer";
        storageClient.storeLogs(customer);
        var bucketContent = s3Client.listObjects(CUSTOMER);

        assertThat(bucketContent.size(), is(equalTo(3)));
        assertThat(bucketContent,
                   containsInAnyOrder(Path.of(CUSTOMER, String.format(DEFAULT_ERROR_FILENAME, customer)).toString(),
                                      Path.of(CUSTOMER, String.format(DEFAULT_INFO_FILENAME, customer)).toString(),
                                      Path.of(CUSTOMER, String.format(DEFAULT_WARNING_FILENAME, customer)).toString()));
    }

    @Test
    void shouldThrowExceptionWhenRecordIsNull() {
        var nullRecord = createNullRecord();
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        assertThrows(RuntimeException.class, () -> storageClient.storeRecord(nullRecord));
    }

    @Test
    void shouldPushProcessedRecordsToS3() {
        var expectedKeyFromBucket = "CUSTOMER/2833909/1/2836938.json";
        var s3Client = new FakeMultipartUploadS3Client();
        var storageClient = new S3StorageImpl(s3Client, "src/test/resources/NVE/", CUSTOMER,
                                              EXPERIMENTAL_BUCKET_SETTTING);
        String[] bundles = {"2833909"};
        storageClient.storeProcessedCollections(null,bundles);
        var actualFileKeyFromBucket = s3Client.listObjects(expectedKeyFromBucket).get(0);
        assertThat(actualFileKeyFromBucket, is(equalTo(expectedKeyFromBucket)));
    }

    @Test
    void shouldPushInputFileToS3() {
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        storageClient.storeInputFile(TEST_PATH, Path.of("NVE", SAMLINGSFIL_TXT).toString());
        var request = getObjectRequest(Path.of(CUSTOMER, SAMLINGSFIL_TXT).toString());
        var response = s3Client.getObject(request, ResponseTransformer.toBytes());
        var fileContent = new String(response.asByteArray());
        var expectedContent = "2833909\n";

        assertThat(fileContent, is(equalTo(expectedContent)));

    }

    @Test
    void shouldNotFailWhenRecordDoesNotHaveContent() {
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        var record = new Record();
        record.setBrageLocation(randomString() + "/" + randomString());
        var uniqueHandlePath = randomString();
        record.setId(UriWrapper.fromUri(randomUri()).addChild(uniqueHandlePath).getUri());
        storageClient.storeRecord(record);
        var expectedKey = joinByPathDelimiter(CUSTOMER, record.getBrageLocation(), uniqueHandlePath + ".json");
        var object = s3Client.getObject(getObjectRequest(expectedKey));
        assertThat(object, is(notNullValue()));
    }

    @Test
    void shouldThrowExceptionWhenCouldNotProcessRecordToAws() {
        var s3Client = mock(FakeMultipartUploadS3Client.class);
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        when(s3Client.putObject((PutObjectRequest) any(), (RequestBody) any())).thenThrow(new RuntimeException());
        var record = new Record();
        record.setBrageLocation(randomString() + "/" + randomString());
        var uniqueHandlePath = randomString();
        record.setId(UriWrapper.fromUri(randomUri()).addChild(uniqueHandlePath).getUri());
        assertThrows(RuntimeException.class, () -> storageClient.storeRecord(record));
    }

    @Test
    void shouldUploadDublinCoreToS3() {
        var testRecord = testRecordWithDublinCoreOnly();
        var dublinCoreIdentifier = testRecord.getContentBundle()
                                       .getContentFileByFilename("dublin_core.xml")
                                       .getIdentifier();
        var s3Client = new FakeMultipartUploadS3Client();
        new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING).storeRecord(testRecord);
        var actualFileKeyFromBucket = s3Client.getKeyByIdentifier(dublinCoreIdentifier);
        var expectedKey = "CUSTOMER/11/1/" + dublinCoreIdentifier;

        assertThat(actualFileKeyFromBucket, is(equalTo(expectedKey)));
    }

    private static GetObjectRequest getObjectRequest(String expectedKey) {
        return GetObjectRequest.builder()
                   .bucket(S3StorageImpl.EXPERIMENTAL_BUCKET_NAME)
                   .key(expectedKey).build();
    }

    private String joinByPathDelimiter(String... values) {
        return String.join(PATH_DELIMITER, values);
    }

    private Record createValidTestRecord() {
        var record = new Record();
        record.setType(new Type(Set.of(BrageType.BOOK.getValue()), NvaType.BOOK.getValue()));
        record.setPartOf("partOfSomethingBigger");
        record.setCristinId("cristinId");
        var contentFile = getContentFile(record);
        var dublinCoreFile = getDublinCoreFile();
        record.setContentBundle(new ResourceContent(List.of(contentFile, dublinCoreFile)));
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11/1").getUri());
        return record;
    }

    private Record testRecordWithDublinCoreOnly() {
        var record = new Record();
        record.setType(new Type(Set.of(BrageType.BOOK.getValue()), NvaType.BOOK.getValue()));
        record.setPartOf("partOfSomethingBigger");
        record.setCristinId("cristinId");
        record.setBrageLocation("11/1");
        var dublinCoreFile = getDublinCoreFile();
        record.setContentBundle(new ResourceContent(List.of(dublinCoreFile)));
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11/1").getUri());
        return record;
    }

    private static ContentFile getDublinCoreFile() {
        return new ContentFile("dublin_core.xml", null, null, UUID.randomUUID(), null, null);
    }

    private static ContentFile getContentFile(Record record) {
        record.setBrageLocation("11/1");
        var contentFile = new ContentFile();
        contentFile.setFilename(VALID_TEST_FILE_NAME);
        contentFile.setIdentifier(UUID.randomUUID());
        return contentFile;
    }

    private Record createNullRecord() {
        return new Record();
    }

    private static class FakeMultipartUploadS3Client extends FakeS3Client {

        private final List<String> keys;

        private FakeMultipartUploadS3Client() {
            this.keys = new ArrayList<>();
        }

        @Override
        public CompleteMultipartUploadResponse completeMultipartUpload(CompleteMultipartUploadRequest request) {
            keys.add(request.key());
            return CompleteMultipartUploadResponse.builder().build();
        }

        @Override
        public CreateMultipartUploadResponse createMultipartUpload(CreateMultipartUploadRequest request) {
            return CreateMultipartUploadResponse.builder().uploadId(randomString()).build();
        }

        @Override
        public UploadPartResponse uploadPart(UploadPartRequest request, RequestBody requestBody) {
            return UploadPartResponse.builder().eTag(randomString()).build();
        }

        public String getKeyByIdentifier(Object name) {
            return keys.stream()
                       .filter(key -> key.contains(name.toString()))
                       .findFirst()
                       .orElseThrow();
        }

        public List<String> listObjects(String path) {
            return keys.stream()
                       .filter(key -> key.startsWith(path))
                       .collect(Collectors.toList());
        }

        public PutObjectResponse putObject(PutObjectRequest putObjectRequest, RequestBody requestBody) {
            keys.add(putObjectRequest.key());
            return PutObjectResponse.builder().build();
        }
    }
}

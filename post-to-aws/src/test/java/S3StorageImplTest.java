import static no.sikt.nva.brage.migration.aws.S3StorageImpl.COULD_NOT_WRITE_RECORD_MESSAGE;
import static no.sikt.nva.brage.migration.aws.S3StorageImpl.DEFAULT_ERROR_FILENAME;
import static no.sikt.nva.brage.migration.aws.S3StorageImpl.DEFAULT_INFO_FILENAME;
import static no.sikt.nva.brage.migration.aws.S3StorageImpl.DEFAULT_WARNING_FILENAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.List;
import java.util.UUID;
import no.sikt.nva.brage.migration.aws.S3StorageImpl;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3StorageImplTest {

    public static final String TEST_PATH = "src/test/resources/";
    public static final String VALID_TEST_FILE_NAME = "Simulated precipitation fields with variance consistent "
                                                      + "interpolation.pdf";
    public static final String CUSTOMER = "CUSTOMER";
    private static final String EXPERIMENTAL_BUCKET_SETTTING = "experimental";

    @Test
    void shouldUploadRecordAndFileToS3() {
        Record testRecord = createValidTestRecord();
        var expectedKeyToRecord = "CUSTOMER/11/1/1.json";
        var expectedKeyToFile =
            "CUSTOMER/11/1/" + testRecord.getContentBundle()
                                   .getContentFileByFilename(VALID_TEST_FILE_NAME)
                                   .getIdentifier();
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        storageClient.storeRecord(testRecord);
        var actualRecordKeyFromBucket =
            s3Client.listObjects(createListObjectsRequest(UnixPath.fromString(expectedKeyToRecord)))
                .contents()
                .get(0).key();
        var actualFileKeyFromBucket =
            s3Client.listObjects(createListObjectsRequest(UnixPath.fromString(expectedKeyToFile)))
                .contents()
                .get(0).key();

        assertThat(actualRecordKeyFromBucket, is(equalTo(expectedKeyToRecord)));
        assertThat(actualFileKeyFromBucket, is(equalTo(expectedKeyToFile)));
    }

    @Test
    void shouldUploadLogFilesToS3() {
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        storageClient.storeLogs();
        var bucketContent = s3Client.listObjects(createListObjectsRequest(UnixPath.fromString(CUSTOMER)))
                                .contents();

        assertThat(bucketContent.size(), is(equalTo(3)));
        assertThat(bucketContent,
                   containsInAnyOrder(S3Object.builder().key(CUSTOMER + "/" + DEFAULT_WARNING_FILENAME).build(),
                                      S3Object.builder().key(CUSTOMER + "/" + DEFAULT_ERROR_FILENAME).build(),
                                      S3Object.builder().key(CUSTOMER + "/" + DEFAULT_INFO_FILENAME).build()));
    }

    @Test
    void shouldThrowExceptionWhenRecordIsNull() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var nullRecord = createNullRecord();
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        storageClient.storeRecord(nullRecord);

        assertThat(appender.getMessages(), containsString(COULD_NOT_WRITE_RECORD_MESSAGE));
    }

    @Test
    void shouldPushProcessedRecordsToS3() {
        var expectedKeyFromBucket = "CUSTOMER/2833909/1/2836938.json";
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, "src/test/resources/NVE/", CUSTOMER, EXPERIMENTAL_BUCKET_SETTTING);
        String[] bundles = {"2833909"};
        storageClient.storeProcessedCollections(bundles);
        var actualFileKeyFromBucket =
            s3Client.listObjects(createListObjectsRequest(UnixPath.fromString(expectedKeyFromBucket)))
                .contents()
                .get(0).key();
        assertThat(actualFileKeyFromBucket, is(equalTo(expectedKeyFromBucket)));
    }

    private ListObjectsRequest createListObjectsRequest(UnixPath folder) {
        return ListObjectsRequest.builder()
                   .bucket(S3StorageImpl.EXPERIMENTAL_BUCKET_NAME)
                   .prefix(folder.toString())
                   .maxKeys(10)
                   .build();
    }

    private Record createValidTestRecord() {
        var record = new Record();
        record.setType(new Type(List.of(BrageType.BOOK.getType()), NvaType.BOOK.getValue()));
        record.setPartOf("partOfSomethingBigger");
        record.setCristinId("cristinId");
        record.setBrageLocation("11/1");
        var contentFile = new ContentFile();
        contentFile.setFilename(VALID_TEST_FILE_NAME);
        contentFile.setIdentifier(UUID.randomUUID());
        record.setContentBundle(new ResourceContent(List.of(contentFile)));
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11/1").getUri());
        return record;
    }

    private Record createNullRecord() {
        return new Record();
    }
}

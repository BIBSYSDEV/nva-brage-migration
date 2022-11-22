import static no.sikt.nva.brage.migration.aws.S3StorageImpl.COULD_NOT_WRITE_RECORD_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
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

public class S3StorageImplTest {

    public static final String TEST_PATH = "src/test/resources/";
    public static final String VALID_TEST_FILE_NAME = "Simulated precipitation fields with variance consistent "
                                                      + "interpolation.pdf";
    public static final String CUSTOMER = "CUSTOMER";

    @Test
    void shouldUploadRecordAndFileToS3File() {
        Record testRecord = createValidTestRecord();
        var expectedKeyToRecord = "CUSTOMER/11/1/1.json";
        var expectedKeyToFile =
            "CUSTOMER/11/1/" + testRecord.getContentBundle().getContentFileByFilename(VALID_TEST_FILE_NAME).getIdentifier();
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER);
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
    void shouldThrowExceptionWhenRecordIsNull() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var nullRecord = createNullRecord();
        var s3Client = new FakeS3Client();
        var storageClient = new S3StorageImpl(s3Client, TEST_PATH, CUSTOMER);
        storageClient.storeRecord(nullRecord);

        assertThat(appender.getMessages(), containsString(COULD_NOT_WRITE_RECORD_MESSAGE));
    }

    private static ListObjectsRequest createListObjectsRequest(UnixPath folder) {
        return ListObjectsRequest.builder()
                   .bucket(S3StorageImpl.bucketName)
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

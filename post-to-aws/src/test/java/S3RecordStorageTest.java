import static no.sikt.nva.brage.migration.aws.S3RecordStorage.COULD_NOT_WRITE_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import java.util.List;
import java.util.UUID;
import no.sikt.nva.brage.migration.aws.S3RecordStorage;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class S3RecordStorageTest {

    public static final String TEST_PATH = "src/test/resources/";
    public static final String VALID_TEST_FILE_NAME = "Simulated precipitation fields with variance consistent "
                                                      + "interpolation.pdf";
    public static final String TEST_PATH_TO_STORE = "src/test/resources/testFilesToDelete/";

    //    @Test
    //    void shouldUploadRecordAndFileToS3File() {
    //        Record testRecord = createValidTestRecord();
    //        var expectedKeyToRecord = "nve/11/1/1.json";
    //        var expectedKeyToFile =
    //            "nve/11/1/" + testRecord.getContentBundle().getContentFileByFilename(VALID_TEST_FILE_NAME)
    //            .getIdentifier();
    //        var s3Client = new FakeS3Client();
    //        var storageClient = new S3RecordStorage(s3Client, TEST_PATH);
    //        storageClient.storeRecord(testRecord);
    //        var filenameToStoreRecord = randomString();
    //        var filenameToStoreFile = randomString();
    //        s3Client.getObject(GetObjectRequest.builder().key(expectedKeyToRecord).build(),
    //                           Path.of(TEST_PATH_TO_STORE + filenameToStoreRecord)).requestCharged();
    //        s3Client.getObject(GetObjectRequest.builder().key(expectedKeyToFile).build(),
    //                           Path.of(TEST_PATH_TO_STORE + filenameToStoreFile));
    //
    //        assertTrue(new File(TEST_PATH_TO_STORE + filenameToStoreRecord).exists());
    //        assertTrue(new File(TEST_PATH_TO_STORE + filenameToStoreFile).exists());
    //    }

    @Test
    void shouldThrowExceptionWhenRecordIsNull() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var nullRecord = createNullRecord();
        var s3Client = new FakeS3Client();
        var storageClient = new S3RecordStorage(s3Client, TEST_PATH);
        storageClient.storeRecord(nullRecord);

        assertThat(appender.getMessages(), containsString(COULD_NOT_WRITE_MESSAGE));
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

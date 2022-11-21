import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import java.util.List;
import no.sikt.nva.brage.migration.aws.S3RecordStorage;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import testutils.FakeS3ClientWithPutObjectSupport;

public class S3RecordStorageTest {

    public static final String INVALID_FILE_NAME = "/filename";
    private S3Client s3Client;

    @BeforeEach
    public void init() {
        s3Client = mock(S3Client.class);
    }

    @Test
    void shouldThrowExceptionWhenInvalidPath() {
        var s3Client = new FakeS3ClientWithPutObjectSupport(INVALID_FILE_NAME, "application/json");
        var s = new S3RecordStorage(s3Client);
        assertThrows(Exception.class, () -> s.storeRecord(createTestRecord()));

    }

    private Record createTestRecord() {
        var record = new Record();
        record.setType(new Type(List.of(BrageType.BOOK.getType()), NvaType.BOOK.getValue()));
        record.setPartOf("partOfSomethingBigger");
        record.setCristinId("cristinId");
        return record;
    }
}

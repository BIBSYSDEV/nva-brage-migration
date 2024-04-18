import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import no.sikt.nva.brage.migration.aws.S3StorageValidator;
import no.sikt.nva.brage.migration.aws.StorageValidatorException;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class S3StorageValidatorTest {

    private static final String SOME_CUSTOMER = "uio";
    private static final String AWS_ENVIRONMENT_VARIABLE = "experimental";
    private FakeS3Client s3Client;
    private S3StorageValidator s3StorageValidator;

    @BeforeEach
    void init() {
        s3Client = new FakeS3Client();
        s3StorageValidator = new S3StorageValidator(s3Client, SOME_CUSTOMER, AWS_ENVIRONMENT_VARIABLE);
    }

    @Test
    void shouldThrowExceptionWhenBucketContainsObjectsFromSameCustomer() throws IOException {
        putObjectInS3();
        assertThrows(StorageValidatorException.class, ()-> s3StorageValidator.validateStorage());
    }

    @Test
    void shouldDoNothingWhenBucketDoesNotContainObjectsFromSameCustomer() throws IOException {
        assertDoesNotThrow(()-> s3StorageValidator.validateStorage());
    }

    private void putObjectInS3() throws IOException {
        var driver = new S3Driver(s3Client, "ignoredbucket");
        driver.insertEvent(UnixPath.of(SOME_CUSTOMER), "some_object_content");
    }
}

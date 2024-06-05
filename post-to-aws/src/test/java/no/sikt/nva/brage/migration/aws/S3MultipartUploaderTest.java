package no.sikt.nva.brage.migration.aws;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.io.File;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.Test;

class S3MultipartUploaderTest {

    @Test
    void shouldUploadEmptyFileToS3() {
        var key = randomString();
        var bucket = randomString();
        var file = new File("src/test/resources/empty.pdf");
        var filename = randomString();
        var s3Client = new FakeS3Client();
        S3MultipartUploader.fromKey(key).fileName(filename).bucket(bucket).file(file).upload(s3Client);

        var persistedFile = new S3Driver(s3Client, bucket).getFile(UnixPath.of(key));

        assertThat(persistedFile, is(notNullValue()));
    }
}
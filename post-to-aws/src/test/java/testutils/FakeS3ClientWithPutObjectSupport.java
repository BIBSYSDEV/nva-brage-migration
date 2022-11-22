package testutils;

import java.io.InputStream;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class FakeS3ClientWithPutObjectSupport extends FakeS3Client {

    public static final String PATH_DELIMITER = "/";
    private static final String CONTENT_DISPOSITION = "filename=\"%s\"";
    private final String contentType;
    private final String filename;
    private final InputStream inputStream;

    public FakeS3ClientWithPutObjectSupport(String filename, String path, String contentType) {
        super();
        this.filename = filename;
        this.contentType = contentType;
        this.inputStream = IoUtils.inputStreamFromResources(path + "/" + filename);
    }

    @Override
    public ResponseInputStream getObject(GetObjectRequest getObjectRequest) {
        return new ResponseInputStream<>(
            GetObjectResponse
                .builder()
                .contentDisposition(generateContentDisposition())
                .contentType(contentType)
                .build(),
            AbortableInputStream
                .create(inputStream));
    }

    @Override
    public PutObjectResponse putObject(PutObjectRequest putObjectRequest, RequestBody requestBody)
        throws AwsServiceException, SdkClientException {
        return PutObjectResponse.builder().build();
    }

    private String generateContentDisposition() {
        return String.format(CONTENT_DISPOSITION, filename);
    }
}

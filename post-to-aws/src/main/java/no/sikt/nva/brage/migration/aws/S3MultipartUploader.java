package no.sikt.nva.brage.migration.aws;

import static nva.commons.core.attempt.Try.attempt;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nva.commons.core.attempt.Failure;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.AvoidFileStream", "PMD.AssignmentInOperand"})
public class S3MultipartUploader {

    public static final int END_OF_INPUT_STREAM = 0;
    public static final long PARTITION_SIZE = 5L * 1024 * 1024;
    private static final ColoredLogger logger = ColoredLogger.create(S3MultipartUploader.class);
    private static final String CONTENT_DISPOSITION_FILE_NAME_PATTERN = "filename=\"%s\"";
    public static final String UPLOADING_PART_MESSAGE = "Uploading part %s of file: %s";
    public static final String COULD_NOT_UPLOAD_FILE_MESSAGE = "Could not upload file: %s";
    private final String key;
    private final String bucket;
    private final String filename;
    private final File file;
    private final List<CompletedPart> completedParts;
    private final byte[] buffer;
    private int part;

    public S3MultipartUploader(String key, String bucket, String filename, File file) {
        this.key = key;
        this.bucket = bucket;
        this.filename = filename;
        this.file = file;
        this.completedParts = new ArrayList<>();
        this.buffer = new byte[(int) PARTITION_SIZE];
        this.part = 1;
    }

    public static S3MultipartUploader fromKey(String value) {
        return builder().withKey(value).build();
    }

    public void upload(S3Client s3Client) {
        if (fileIsEmpty()) {
            putFileToS3(s3Client);
        } else {
            attempt(() -> multiPartUploadUsingClient(s3Client)).orElseThrow(this::logError);
        }
    }

    private void putFileToS3(S3Client s3Client)  {
        s3Client.putObject(PutObjectRequest.builder()
                               .bucket(bucket)
                               .key(key)
                               .contentDisposition(createContentDisposition())
                               .build(), RequestBody.fromFile(file));
    }

    private boolean fileIsEmpty() {
        return this.file.length() == 0;
    }

    public S3MultipartUploader file(File file) {
        return copy().withFile(file).build();
    }

    public File getFile() {
        return file;
    }

    public S3MultipartUploader bucket(String bucketName) {
        return this.copy().withBucket(bucketName).build();
    }

    public S3MultipartUploader fileName(String fileName) {
        return this.copy().withFilename(encode(fileName)).build();
    }

    public String getFilename() {
        return filename;
    }

    private static Builder builder() {
        return new Builder();
    }

    private static CompletedPart createCompletedPart(int partNumber, UploadPartResponse uploadPartResponse) {
        return CompletedPart.builder().partNumber(partNumber).eTag(uploadPartResponse.eTag()).build();
    }

    private static String encode(String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    }

    private static CompletedMultipartUpload completeMultipartUpload(List<CompletedPart> completedParts) {
        return CompletedMultipartUpload.builder().parts(completedParts).build();
    }

    private RuntimeException logError(Failure<?> failure) {
        logger.error(failure.getException().toString());
        logger.error(String.format(COULD_NOT_UPLOAD_FILE_MESSAGE, filename));
        return new RuntimeException();
    }

    private CompleteMultipartUploadResponse multiPartUploadUsingClient(S3Client s3Client) throws IOException {
        var request = initiateRequest();
        var response = s3Client.createMultipartUpload(request);

        int bytesRead;

        try (var inputStream = new BufferedInputStream(new FileInputStream(file))) {
            while ((bytesRead = readToBuffer(inputStream)) > END_OF_INPUT_STREAM) {
                var partToUpload = RequestBody.fromBytes(Arrays.copyOf(buffer, bytesRead));
                var uploadPartResponse = s3Client.uploadPart(createUploadPartRequest(response, part), partToUpload);
                completedParts.add(createCompletedPart(part, uploadPartResponse));
                logger.info(String.format(UPLOADING_PART_MESSAGE, part, filename));
                part++;
            }
            var completeMultipartUploadRequest = createCompleteMultipartUploadRequest(response, completedParts);
            return s3Client.completeMultipartUpload(completeMultipartUploadRequest);
        } catch (Exception e) {
            s3Client.abortMultipartUpload(createAbortMultipartUploadRequest(response));
            return null;
        }
    }

    private AbortMultipartUploadRequest createAbortMultipartUploadRequest(CreateMultipartUploadResponse response) {
        return AbortMultipartUploadRequest.builder()
                   .bucket(bucket)
                   .key(key)
                   .uploadId(response.uploadId())
                   .build();
    }

    private int readToBuffer(BufferedInputStream inputStream) throws IOException {
        return inputStream.read(buffer, 0, buffer.length);
    }

    private UploadPartRequest createUploadPartRequest(CreateMultipartUploadResponse response, int partNumber) {
        return UploadPartRequest.builder()
                   .bucket(bucket)
                   .key(key)
                   .uploadId(response.uploadId())
                   .partNumber(partNumber)
                   .build();
    }

    private Builder copy() {
        return builder().withBucket(this.bucket).withKey(this.key).withFilename(this.filename).withFile(this.file);
    }

    private CompleteMultipartUploadRequest createCompleteMultipartUploadRequest(CreateMultipartUploadResponse response,
                                                                                List<CompletedPart> completedParts) {
        return CompleteMultipartUploadRequest.builder()
                   .bucket(bucket)
                   .key(key)
                   .uploadId(response.uploadId())
                   .multipartUpload(completeMultipartUpload(completedParts))
                   .build();
    }

    private CreateMultipartUploadRequest initiateRequest() throws IOException {
        var mimeType = detectMimeType(file);
        return CreateMultipartUploadRequest.builder()
                   .bucket(this.bucket)
                   .key(this.key)
                   .contentDisposition(createContentDisposition())
                   .contentType(mimeType)
                   .build();
    }

    private String detectMimeType(File file) throws IOException {
        return TikaConfig.getDefaultConfig()
                   .getDetector()
                   .detect(new BufferedInputStream(TikaInputStream.get(file)), new Metadata())
                   .toString();
    }

    private String createContentDisposition() {
        return String.format(CONTENT_DISPOSITION_FILE_NAME_PATTERN, filename);
    }

    private static final class Builder {

        private String key;
        private String bucket;
        private String filename;
        private File file;

        private Builder() {
        }

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder withBucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public Builder withFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder withFile(File file) {
            this.file = file;
            return this;
        }

        public S3MultipartUploader build() {
            return new S3MultipartUploader(key, bucket, filename, file);
        }
    }
}

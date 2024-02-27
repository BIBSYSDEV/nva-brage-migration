package no.sikt.nva.brage.migration.aws;

import static nva.commons.core.attempt.Try.attempt;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.AvoidFileStream", "PMD.AssignmentInOperand"})
public class MultiPartUploader {

    private static final String CONTENT_DISPOSITION_FILE_NAME_PATTERN = "filename=\"%s\"";
    public static final long PARTITION_SIZE = 10L * 1024 * 1024;
    private final String key;
    private final String bucket;
    private final String filename;
    private final File file;
    private final List<CompletedPart> completedParts;
    private final byte[] buffer;
    private int part;

    public MultiPartUploader(String key, String bucket, String filename, File file) {
        this.key = key;
        this.bucket = bucket;
        this.filename = filename;
        this.file = file;
        this.completedParts = new ArrayList<>();
        this.buffer = new byte[(int) PARTITION_SIZE];
        this.part = 1;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MultiPartUploader fromKey(String value) {
        return MultiPartUploader.builder().withKey(value).build();
    }

    public void upload(S3Client s3Client) {
        attempt(() -> multiPartUploadUsingClient(s3Client)).orElseThrow();
    }

    private CompleteMultipartUploadResponse multiPartUploadUsingClient(S3Client s3Client) throws IOException {
        var request = initiateRequest();
        var response = s3Client.createMultipartUpload(request);

        int bytesRead;

        try (var inputStream = new BufferedInputStream(new FileInputStream(file))) {
            while ((bytesRead = readToBuffer(inputStream)) > 0) {
                var partToUpload = RequestBody.fromBytes(Arrays.copyOf(buffer, bytesRead));
                var uploadPartResponse = s3Client.uploadPart(createUploadPartRequest(response, part), partToUpload);
                completedParts.add(createCompletedPart(part, uploadPartResponse));
                part++;
            }
        }

        var completeMultipartUploadRequest = createCompleteMultipartUploadRequest(response, completedParts);
        return s3Client.completeMultipartUpload(completeMultipartUploadRequest);
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

    private static CompletedPart createCompletedPart(int partNumber, UploadPartResponse uploadPartResponse) {
        return CompletedPart.builder().partNumber(partNumber).eTag(uploadPartResponse.eTag()).build();
    }

    public MultiPartUploader file(File file) {
        return copy().withFile(file).build();
    }

    public File getFile() {
        return file;
    }

    public Builder copy() {
        return builder().withBucket(this.bucket).withKey(this.key);
    }

    public MultiPartUploader bucket(String bucketName) {
        return this.copy().withBucket(bucketName).build();
    }

    public MultiPartUploader fileName(String fileName) {
        return this.copy().withFilename(fileName).build();
    }

    public String getFilename() {
        return filename;
    }

    private static CompletedMultipartUpload completeMultipartUpload(List<CompletedPart> completedParts) {
        return CompletedMultipartUpload.builder().parts(completedParts).build();
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

    private CreateMultipartUploadRequest initiateRequest() {
        return CreateMultipartUploadRequest.builder()
                   .bucket(this.bucket)
                   .key(this.key)
                   .contentDisposition(createContentDisposition())
                   .build();
    }

    private String createContentDisposition() {
        return String.format(CONTENT_DISPOSITION_FILE_NAME_PATTERN, filename);
    }

    public static final class Builder {

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

        public MultiPartUploader build() {
            return new MultiPartUploader(key, bucket, filename, file);
        }
    }
}

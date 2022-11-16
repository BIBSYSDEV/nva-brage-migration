package no.sikt.nva.brage.migration.awsconnection;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class WriteFileTos3 {

    private static final String bucketName = "anette-kir-brage-migration-experiment";
    private static final Logger logger = LoggerFactory.getLogger(WriteFileTos3.class);

    private final S3Client s3Client;

    public WriteFileTos3(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void writeFileToS3(File file, String objectkey) {
        try {
            s3Client.putObject(PutObjectRequest
                                   .builder()
                                   .bucket(bucketName)
                                   .key(objectkey)
                                   .build(),
                               RequestBody.fromFile(file));
        } catch (Exception e) {
            logger.error("Could not write files to s3");
        }
    }


}

package no.sikt.nva.brage.migration.aws;

import static no.sikt.nva.brage.migration.aws.S3StorageImpl.determineBucketFromAwsEnvironment;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.services.s3.S3Client;

public class S3StorageValidator implements StorageValidator {


    private static final String STORAGE_VALIDATOR_EXCEPTION = "There are input files in s3 bucket already";
    private final S3Client s3Client;
    private final String awsBucket;
    private final String customer;

    public S3StorageValidator(S3Client s3Client,  String customer, String awsEnvironment) {
        this.s3Client = s3Client;
        this.awsBucket = determineBucketFromAwsEnvironment(awsEnvironment);
        this.customer = customer;
    }

    @Override
    public void validateStorage() throws StorageValidatorException{
        if (!hasNotBeenUploadedPreviously()){
            throw new StorageValidatorException(STORAGE_VALIDATOR_EXCEPTION);
        }
    }

    private boolean hasNotBeenUploadedPreviously() {
        var s3driver = new S3Driver(s3Client, awsBucket);
        var files = s3driver.getFiles(UnixPath.of(customer));
        return files.isEmpty();
    }
}

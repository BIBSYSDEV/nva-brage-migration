package no.sikt.nva.brage.migration.aws;

public interface StorageValidator {

    void validateStorage() throws StorageValidatorException;
}

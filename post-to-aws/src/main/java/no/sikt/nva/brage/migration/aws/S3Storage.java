package no.sikt.nva.brage.migration.aws;

import no.sikt.nva.brage.migration.common.model.record.Record;

public interface S3Storage {

    void storeRecord(Record record);

    void storeLogs();
}

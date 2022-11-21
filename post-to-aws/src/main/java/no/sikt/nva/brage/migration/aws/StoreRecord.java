package no.sikt.nva.brage.migration.aws;

import no.sikt.nva.brage.migration.common.model.record.Record;

public interface StoreRecord {

    void storeRecord(Record record);
}

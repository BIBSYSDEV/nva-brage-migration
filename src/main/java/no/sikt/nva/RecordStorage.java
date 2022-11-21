package no.sikt.nva;

import static java.util.Objects.nonNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.record.Record;
import org.apache.commons.lang3.StringUtils;

public class RecordStorage {

    private List<Record> records;

    public RecordStorage() {
        this.records = new ArrayList<>();
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public String getRecordLocationStringById(URI id) {
        var recordFromStorage = records.stream().filter(record -> record.getId().equals(id)).findFirst().orElse(null);
        if (nonNull(recordFromStorage)) {
            return recordFromStorage.getId() + " " + recordFromStorage.getBrageLocation();
        } else {
            return StringUtils.EMPTY;
        }
    }
}

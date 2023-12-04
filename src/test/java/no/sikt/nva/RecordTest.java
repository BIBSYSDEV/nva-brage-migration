package no.sikt.nva;

import static org.junit.jupiter.api.Assertions.assertTrue;
import no.sikt.nva.brage.migration.common.model.record.Record;
import org.junit.jupiter.api.Test;

public class RecordTest {

    public static final String BRAGE_LOCATION = "15828/1";
    public static final String HANDLE = "1956/15828";

    @Test
    void shouldReturnTrueWhenBrageOriginMathcHandle() {
        var record = new Record();
        record.setBrageLocation(BRAGE_LOCATION);

        assertTrue(record.hasOrigin(HANDLE));
    }
}

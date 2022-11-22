import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import java.util.UUID;
import no.sikt.nva.brage.migration.aws.S3RecordStorage;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.Type;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.brage.migration.common.model.record.content.ResourceContent;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;
import testutils.FakeS3ClientWithPutObjectSupport;

public class S3RecordStorageTest {

    public static final String INVALID_FILE_NAME = "/Users/kir.truhacev/IdeaProjects/nva-brage-migration/samlingsfil"
                                                   + ".txt";

    @Test
    void shouldThrowExceptionWhenInvalidPath() {
        var s3Client = new FakeS3ClientWithPutObjectSupport("Simulated precipitation fields with variance consistent interpolation.pdf", "11/1", "text/json");
        var s = new S3RecordStorage(s3Client);
        s.storeRecord(createTestRecord());
        assertThrows(Exception.class, () -> s.storeRecord(createTestRecord()));
    }

    private Record createTestRecord() {
        var record = new Record();
        record.setType(new Type(List.of(BrageType.BOOK.getType()), NvaType.BOOK.getValue()));
        record.setPartOf("partOfSomethingBigger");
        record.setCristinId("cristinId");
        record.setBrageLocation("11/1");
        var contentFile = new ContentFile();
        contentFile.setFilename("Simulated precipitation fields with variance consistent interpolation.pdf");
        contentFile.setIdentifier(UUID.randomUUID());
        record.setContentBundle(new ResourceContent(List.of(contentFile)));
        record.setId(UriWrapper.fromUri("https://hdl.handle.net/11/1").getUri());
        return record;
    }
}

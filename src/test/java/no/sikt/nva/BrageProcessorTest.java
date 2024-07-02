package no.sikt.nva;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.record.EntityDescription;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class BrageProcessorTest {

    @Test
    void shouldRemoveSubjectCodeFromDescriptionsIfPresentInBothDescriptionsAndSubjectCode() {
        var subjectCode = randomString();
        var record = recordWithSubjectCodeInDescriptions(subjectCode);

        assertTrue(record.getEntityDescription().getDescriptions().contains(subjectCode));

        var updatedRecord = BrageProcessor.injectValuesFromFsDublinCore(record, dublinCoreWithSubjectCode(subjectCode));

        assertFalse(updatedRecord.getEntityDescription().getDescriptions().contains(subjectCode));
        assertEquals(updatedRecord.getSubjectCode(), subjectCode);
    }

    private static @NotNull DublinCore dublinCoreWithSubjectCode(String subjectCode) {
        return new DublinCore(List.of(new DcValue(Element.SUBJECT_CODE, null, subjectCode)));
    }

    private static Record recordWithSubjectCodeInDescriptions(String subjectCode) {
        var record = new Record();
        var entityDescription = new EntityDescription();
        entityDescription.setDescriptions(List.of(subjectCode));
        record.setEntityDescription(entityDescription);
        return record;
    }
}
package no.sikt.nva;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.List;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.scrapers.DublinCoreFactory;
import no.sikt.nva.scrapers.DublinCoreScraper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

class BrageProcessorTest {

    @Test
    void shouldExtractSubjectCodeFromFsDublinAndRemoveLocalCodeWithTheSameValueFromDescription() {
        var duplicatedSubjectCode = "JUS123";
        var brageDublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(
            new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, duplicatedSubjectCode),
            new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, "MAT321")
        ));
        var fsDublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(
            new DcValue(Element.SUBJECT_CODE, null, duplicatedSubjectCode)
        ));

        var record = createRecordFromDublinCore(brageDublinCore);
        var updatedRecord = BrageProcessor.injectValuesFromFsDublinCore(record, brageDublinCore, fsDublinCore);

        assertEquals(updatedRecord.getSubjectCode(), duplicatedSubjectCode);
        assertFalse(updatedRecord.getEntityDescription().getDescriptions().contains(duplicatedSubjectCode));
    }

    @Test
    void shouldExtractSubjectCodeFromLocalCodeInBrageXmlWhenFsDublinCoreIsMissingAndRemoveItFromDescriptions() {
        var subjectCode = "JUS123";
        var brageDublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(
            new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, subjectCode)
        ));
        var record = createRecordFromDublinCore(brageDublinCore);
        var updatedRecord = BrageProcessor.injectValuesFromFsDublinCore(record, brageDublinCore, new DublinCore(List.of()));

        assertEquals(updatedRecord.getSubjectCode(), subjectCode);
        assertFalse(updatedRecord.getEntityDescription().getDescriptions().contains(subjectCode));
    }

    @Test
    void shouldNotSetSubjectCodeToValueFromDescriptionLocalCodeWhenStringIsLongerTheMaxSubjectCodeLength() {
        var subjectCode = RandomStringUtils.randomAlphanumeric(20, 30);
        var brageDublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(
            new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, subjectCode)
        ));
        var record = createRecordFromDublinCore(brageDublinCore);
        var updatedRecord = BrageProcessor.injectValuesFromFsDublinCore(record, brageDublinCore, new DublinCore(List.of()));

        assertNull(updatedRecord.getSubjectCode());
    }

    private static Record createRecordFromDublinCore(DublinCore dublinCore) {
        return new DublinCoreScraper(false, false, Map.of())
                   .validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");
    }
}
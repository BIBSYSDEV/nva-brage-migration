package no.sikt.nva;

import static no.sikt.nva.DublinCoreValidator.Error.INVALID_ISBN;
import static no.sikt.nva.DublinCoreValidator.Error.INVALID_ISSN;
import static no.sikt.nva.ResourceNameConstants.INVALID_DUBLIN_CORE_XML_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.ResourceNameConstants.VALID_DUBLIN_CORE_XML_FILE_NAME;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import java.io.File;
import java.util.List;
import no.sikt.nva.DublinCoreValidator.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import org.junit.jupiter.api.Test;

public class DublinCoreValidatorTest {

    @Test
    void validIssnAndIsbnDoesNotAppendProblemsToProblemList() {
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(
            TEST_RESOURCE_PATH + VALID_DUBLIN_CORE_XML_FILE_NAME));
        var actualProblemsList = DublinCoreValidator.getDublinCoreErrors(dublinCore, null);
        assertThat(actualProblemsList, not(contains(INVALID_ISSN, INVALID_ISBN)));
    }

    @Test
    void shouldReturnProblemListContainingInvalidIssnAndInvalidIsbnMessage() {
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(
            TEST_RESOURCE_PATH + INVALID_DUBLIN_CORE_XML_FILE_NAME));
        var actualProblemsList = DublinCoreValidator.getDublinCoreErrors(dublinCore, null);
        assertThat(actualProblemsList, hasItems(INVALID_ISSN, INVALID_ISBN));
    }

    @Test
    void shouldReturnWarningWhenSubjectHasUnrecognizedType() {
        var dcValues = List.of(new DcValue(Element.SUBJECT, Qualifier.SLUG, randomString()),
                               new DcValue(Element.SUBJECT, Qualifier.SLUG, randomString()));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore);
        assertThat(actualWarningList, hasSize(1));
        assertThat(actualWarningList, hasItems(Warning.SUBJECT_WARNING));
    }
}

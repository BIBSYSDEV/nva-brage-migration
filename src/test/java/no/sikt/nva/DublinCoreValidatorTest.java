package no.sikt.nva;

import static no.sikt.nva.DublinCoreParserTest.INVALID_DUBLIN_CORE;
import static no.sikt.nva.DublinCoreParserTest.VALID_DUBLIN_CORE;
import static no.sikt.nva.DublinCoreValidator.Problem.INVALID_ISBN;
import static no.sikt.nva.DublinCoreValidator.Problem.INVALID_ISSN;
import static no.sikt.nva.ResourceNameConstants.INVALID_DUBLIN_CORE_XML_FILE_NAME;
import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.ResourceNameConstants.VALID_DUBLIN_CORE_XML_FILE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import java.io.File;
import org.junit.jupiter.api.Test;

public class DublinCoreValidatorTest {

    @Test
    void validIssnAndIsbnDoesNotAppendProblemsToProblemList() {
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(
            TEST_RESOURCE_PATH + VALID_DUBLIN_CORE_XML_FILE_NAME));
        var actualProblemsList = DublinCoreValidator.getDublinCoreErrors(dublinCore);
        assertThat(actualProblemsList, not(contains(INVALID_ISSN, INVALID_ISBN)));
    }

    @Test
    void shouldReturnProblemListContainingInvalidIssnAndInvalidIsbnMessage() {
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(
            TEST_RESOURCE_PATH + INVALID_DUBLIN_CORE_XML_FILE_NAME));
        var actualProblemsList = DublinCoreValidator.getDublinCoreErrors(dublinCore);
        assertThat(actualProblemsList, hasItems(INVALID_ISSN, INVALID_ISBN));
    }
}

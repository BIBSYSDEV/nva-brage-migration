package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.ResourceNameConstants.VALID_DUBLIN_CORE_XML_FILE_NAME;
import static no.sikt.nva.model.ErrorDetails.Error.INVALID_ISBN;
import static no.sikt.nva.model.ErrorDetails.Error.INVALID_ISSN;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import java.io.File;
import java.util.List;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.WarningDetails;
import no.sikt.nva.model.WarningDetails.Warning;
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
        assertThat(actualProblemsList, not(contains(new ErrorDetails(INVALID_ISSN, List.of()),
                                                    new ErrorDetails(INVALID_ISBN, List.of()))));
    }

    @Test
    void shouldReturnProblemListContainingInvalidIssnAndInvalidIsbnMessage() {

        var dcValues = List.of(new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "invalid_issn"),
                               new DcValue(Element.IDENTIFIER, Qualifier.ISBN, "invalid_isbn"));
        var dublinCore = new DublinCore(dcValues);
        var actualProblemsList = DublinCoreValidator.getDublinCoreErrors(dublinCore, null);
        assertThat(actualProblemsList, hasItems(new ErrorDetails(INVALID_ISSN, List.of()),
                                                new ErrorDetails(INVALID_ISBN, List.of())));
    }

    @Test
    void shouldReturnWarningWhenSubjectHasUnrecognizedType() {
        var dcValues = List.of(new DcValue(Element.SUBJECT, Qualifier.SLUG, randomString()),
                               new DcValue(Element.SUBJECT, Qualifier.SLUG, randomString()));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore);
        assertThat(actualWarningList, hasSize(1));
        assertThat(actualWarningList,
                   hasItems(new WarningDetails(Warning.SUBJECT_WARNING, List.of())));
    }
}

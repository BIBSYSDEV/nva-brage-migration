package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.ResourceNameConstants.VALID_DUBLIN_CORE_XML_FILE_NAME;
import static no.sikt.nva.model.ErrorDetails.Error.INVALID_ISBN;
import static no.sikt.nva.model.ErrorDetails.Error.INVALID_ISSN;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.ErrorDetails;
import no.sikt.nva.model.ErrorDetails.Error;
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
        assertThat(actualWarningList,
                   hasItems(new WarningDetails(Warning.SUBJECT_WARNING, List.of())));
    }

    @Test
    void shouldReturnErrorDetailsWithDoiErrorIfDoiIsInvalid() {
        var dcValues = List.of(new DcValue(Element.IDENTIFIER, Qualifier.DOI, randomString()));
        var dublinCore = new DublinCore(dcValues);
        var brageLocation = new BrageLocation(Path.of("some", "ignored"));
        var actualErrorList = DublinCoreValidator.getDublinCoreErrors(dublinCore, brageLocation);
        assertThat(actualErrorList, hasItems(new ErrorDetails(Error.INVALID_DOI_OFFLINE_CHECK, List.of())));
    }

    @Test
    void shouldNotReturnErrorListWithDoiIfDoiIsValid() {
        var dcValues = List.of(new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://doi.org/10"
                                                                              + ".5194/tc-8-1885-2014"));
        var dublinCore = new DublinCore(dcValues);
        var brageLocation = new BrageLocation(Path.of("some", "ignored"));
        var actualErrorList = DublinCoreValidator.getDublinCoreErrors(dublinCore, brageLocation);
        assertThat(actualErrorList, not(hasItems(new ErrorDetails(Error.INVALID_DOI_OFFLINE_CHECK, List.of()))));
    }

    @Test
    void shouldReturnWarningWhenInvalidDate() {
        var dcValues = List.of(new DcValue(Element.DATE, Qualifier.ISSUED, "someDate"));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore);
        assertThat(actualWarningList, hasItems(new WarningDetails(Warning.INVALID_DATE_WARNING, List.of())));
    }

    @Test
    void shouldReturnWarningDateNotPresent() {
        var dcValues = List.of(new DcValue(Element.CONTRIBUTOR, Qualifier.ADVISOR, "some advisor"));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore);
        assertThat(actualWarningList, hasItems(new WarningDetails(Warning.DATE_NOT_PRESENT_WARNING, List.of())));
    }

    @Test
    void shouldReturnWarningIfLanguageIsUndefined() {
        var dcValues = List.of(new DcValue(Element.LANGUAGE, Qualifier.NONE, "someInvalidLanguage"));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore);
        assertThat(actualWarningList, hasItems(new WarningDetails(Warning.LANGUAGE_MAPPED_TO_UNDEFINED, List.of())));
    }

    @Test
    void shouldReturnMapValidLanguageToNvaLanguage() {
        var dcValues = List.of(new DcValue(Element.LANGUAGE, Qualifier.NONE, "nob"));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore);
        assertThat(actualWarningList, not(hasItems(
            new WarningDetails(Warning.LANGUAGE_MAPPED_TO_UNDEFINED, List.of()))));
    }

    @Test
    void shouldValidateDoiCorrectly() {
        var inputDoi = "https://doi.org/10.1016/j.scitotenv.2021.151958";
        var dcValues = List.of(new DcValue(Element.IDENTIFIER, Qualifier.DOI, inputDoi));
        var dublinCore = new DublinCore(dcValues);
        var actualErrors = DublinCoreValidator.getDublinCoreErrors(dublinCore, new BrageLocation(null));
        assertThat(actualErrors, not(hasItems(new ErrorDetails(Error.INVALID_DOI_ONLINE_CHECK, List.of()))));
    }

    @Test
    void shouldTestDoiOnlineAndReturnError() {
        var inputDoi = "doi.org/10.1016/j.scitotenv.2021.151958.";
        var dcValues = List.of(new DcValue(Element.IDENTIFIER, Qualifier.DOI, inputDoi));
        var dublinCore = new DublinCore(dcValues);
        var actualErrors = DublinCoreValidator.getDublinCoreErrors(dublinCore, new BrageLocation(null));
        assertThat(actualErrors, hasItems(new ErrorDetails(Error.INVALID_DOI_ONLINE_CHECK, List.of())));
    }
}

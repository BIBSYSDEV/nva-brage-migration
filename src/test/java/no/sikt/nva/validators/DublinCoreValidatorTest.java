package no.sikt.nva.validators;

import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.ResourceNameConstants.VALID_DUBLIN_CORE_XML_FILE_NAME;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DATE_NOT_PRESENT_DC_DATE_ISSUED;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DC_JOURNAL_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_DATE_ISSUED;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_TYPE;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DOI_ONLINE_CHECK;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_ISSN;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_DC_ISSN_AND_DC_JOURNAL;
import static no.sikt.nva.validators.DublinCoreValidator.NO_CUSTOMER;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.channelregister.ChannelRegister;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.scrapers.DublinCoreFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DublinCoreValidatorTest {

    private static final String SOME_CUSTOMER = "nve";

    @Test
    void validIssnAndIsbnDoesNotAppendProblemsToProblemList() {
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(
            TEST_RESOURCE_PATH + VALID_DUBLIN_CORE_XML_FILE_NAME));
        var actualProblemsList = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualProblemsList, not(contains(new ErrorDetails(INVALID_ISSN, Set.of()),
                                                    new WarningDetails(Warning.INVALID_ISBN_WARNING, Set.of()))));
    }

    @Test
    void shouldNotAppendEmptyIsbnXmlTagsInDublinCoreToProblemList() {
        var emtpyIsbnTag = new DcValue(Element.IDENTIFIER, Qualifier.ISBN, null);
        var dublinCore = new DublinCore(List.of(emtpyIsbnTag));
        var actualProblemsList = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);
        assertThat(actualProblemsList, not(contains(new WarningDetails(Warning.INVALID_ISBN_WARNING, Set.of()))));
    }

    @Test
    void shouldReturnProblemListContainingInvalidIssnAndInvalidIsbnMessage() {

        var dcValues = List.of(new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "invalid_issn"),
                               new DcValue(Element.IDENTIFIER, Qualifier.ISBN, "invalid_isbn"));
        var dublinCore = new DublinCore(dcValues);
        var actualProblemsList = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);
        assertThat(actualProblemsList, hasItems(new ErrorDetails(INVALID_ISSN, Set.of())));
    }

    @Test
    void shouldReturnWarningWhenSubjectHasUnrecognizedType() {
        var dcValues = List.of(new DcValue(Element.SUBJECT, Qualifier.SLUG, randomString()),
                               new DcValue(Element.SUBJECT, Qualifier.SLUG, randomString()));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore, NO_CUSTOMER);

        assertThat(actualWarningList, hasItems(new WarningDetails(Warning.SUBJECT_WARNING, Set.of())));
    }

    @Test
    void shouldReturnErrorDetailsWithDoiErrorIfDoiIsInvalid() {
        var dcValues = List.of(new DcValue(Element.IDENTIFIER, Qualifier.DOI, randomString()));
        var dublinCore = new DublinCore(dcValues);
        var actualErrorList = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrorList, hasItems(new ErrorDetails(INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK, Set.of())));
    }

    @Test
    void shouldNotReturnErrorListWithDoiIfDoiIsValid() {
        var dcValues = List.of(new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://doi.org/10"
                                                                              + ".5194/tc-8-1885-2014"));
        var dublinCore = new DublinCore(dcValues);
        var actualErrorList = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrorList,
                   not(hasItems(new ErrorDetails(INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK, Set.of()))));
    }

    @Test
    void shouldReturnErrorWhenInvalidDate() {
        var dcValues = List.of(new DcValue(Element.DATE, Qualifier.ISSUED, "someDate"));
        var dublinCore = new DublinCore(dcValues);
        var actualErrorList = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrorList, hasItems(new ErrorDetails(INVALID_DC_DATE_ISSUED, Set.of())));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"1991-02-14", "2008-12-31", "2021-10-16", "2007-08-21T11:17:48Z"})
    void shouldNotReturnDateErrorWhenDateIsValid(String date) {
        var dcValues = List.of(new DcValue(Element.DATE, Qualifier.ISSUED, date));
        var dublinCore = new DublinCore(dcValues);
        var actualErrorList = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrorList, not(hasItems(new ErrorDetails(INVALID_DC_DATE_ISSUED, Set.of()))));
    }

    @Test
    void shouldReturnErrorDateNotPresent() {
        var dcValues = List.of(new DcValue(Element.CONTRIBUTOR, Qualifier.ADVISOR, "someLastName, someFirstName"));
        var dublinCore = new DublinCore(dcValues);
        var actualErrorList = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrorList, hasItems(new ErrorDetails(DATE_NOT_PRESENT_DC_DATE_ISSUED, Set.of())));
    }

    @Test
    void shouldReturnWarningIfLanguageIsUndefined() {
        var dcValues = List.of(new DcValue(Element.LANGUAGE, Qualifier.NONE, "someInvalidLanguage"));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore, NO_CUSTOMER);

        assertThat(actualWarningList, hasItems(new WarningDetails(Warning.LANGUAGE_MAPPED_TO_UNDEFINED, Set.of())));
    }

    @Test
    void shouldMapValidLanguageToNvaLanguage() {
        var dcValues = List.of(new DcValue(Element.LANGUAGE, Qualifier.NONE, "nob"));
        var dublinCore = new DublinCore(dcValues);
        var actualWarningList = DublinCoreValidator.getDublinCoreWarnings(dublinCore, NO_CUSTOMER);

        assertThat(actualWarningList,
                   not(hasItems(new WarningDetails(Warning.LANGUAGE_MAPPED_TO_UNDEFINED, Set.of()))));
    }

    @Test
    void shouldReturnManyInvalidDoiErrorDetailsWhenResourceContainsMultipleInvalidDoi() {
        var invalidDoi1 = "doi.org/10.1016/j.scitotenv.2021.151958.";
        var invalidDoi2 = "doi.org/10.1016/j.scitotenv.2021.984324.";
        var dcValues = List.of(
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, invalidDoi1),
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, invalidDoi2),
            new DcValue(Element.TYPE, null, "Book"));

        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var actualErrors = new ArrayList<>();
        DoiValidator.getDoiErrorDetailsOnline(dublinCore).ifPresent(actualErrors::addAll);

        assertThat(actualErrors, hasItems(new ErrorDetails(INVALID_DOI_ONLINE_CHECK, Set.of())));
    }

    @Test
    void shouldTestDoiOnlineAndReturnError() {
        var inputDoi = "doi.org/10.1016/j.scitotenv.2021.151958.";
        var dcValues = List.of(
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, inputDoi),
            new DcValue(Element.TYPE, null, "Book"));

        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var actualErrors = new ArrayList<>();
        DoiValidator.getDoiErrorDetailsOnline(dublinCore).ifPresent(actualErrors::addAll);

        assertThat(actualErrors, hasItems(new ErrorDetails(INVALID_DOI_ONLINE_CHECK, Set.of())));
    }

    @Test
    void shouldNotReturnDoiErrorWhenDoiHasValidStructureButUriIsInvalid() {
        var inputDoi = "doi.org/10.1016/j.scitotenv.2021.151958.";
        var dcValues = List.of(
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, inputDoi),
            new DcValue(Element.TYPE, null, "Book"));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var actualErrors = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrors, not(hasItems(new ErrorDetails(INVALID_DOI_ONLINE_CHECK, Set.of()))));
    }

    @Test
    void shouldReturnErrorDetailWhenJournalIsNotInChannelRegister() {
        var dcValues = List.of(
            new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-0678"),
            new DcValue(Element.TYPE, null, BrageType.JOURNAL_ARTICLE.getValue()));
        var brageLocation = new BrageLocation(null);
        var actualError =
            ChannelRegister.getRegister().getChannelRegisterErrors(DublinCoreFactory.createDublinCoreWithDcValues(dcValues),
                                                     brageLocation, SOME_CUSTOMER);

        assertThat(actualError.get(),
                   is(equalTo(new ErrorDetails(DC_JOURNAL_NOT_IN_CHANNEL_REGISTER, Set.of("1501-0678")))));
    }

    @Test
    void shouldNotReturnChannelRegisterErrorDetailWhenJournalIssnIsInChannelRegister() {
        var dcValues = List.of(
            new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "2038-324X"),
            new DcValue(Element.TYPE, null, BrageType.JOURNAL_ARTICLE.getValue()));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var actualErrors = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrors, not(hasItems(new ErrorDetails(MISSING_DC_ISSN_AND_DC_JOURNAL, Set.of()))));
    }

    @Test
    void shouldNotReturnChannelRegisterErrorDetailWhenJournalTitleIsInChannelRegister() {
        var dcValues = List.of(
            new DcValue(Element.SOURCE, Qualifier.JOURNAL, "Fisheries management and ecology"),
            new DcValue(Element.TYPE, null, BrageType.JOURNAL_ARTICLE.getValue()));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var actualErrors = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrors, not(hasItems(new ErrorDetails(MISSING_DC_ISSN_AND_DC_JOURNAL, Set.of()))));
    }

    @Test
    void shouldNotLogValidMultipleTypesValues() {
        var dcValues = List.of(new DcValue(Element.TYPE, null, BrageType.JOURNAL_ARTICLE.getValue()),
                               new DcValue(Element.TYPE, null, BrageType.PEER_REVIEWED.getValue()));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var actualErrors = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);

        assertThat(actualErrors, not(hasItems(new ErrorDetails(INVALID_DC_TYPE, Set.of()))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1935-1987", "1906?", "2009 -", "2009-", "1983–1984"})
    void shouldNotLogInvalidDateIssuedWhenDateIsPeriod(String value) {
        var date = new DcValue(Element.DATE, Qualifier.ISSUED, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(date));
        var errors = DublinCoreValidator.getDublinCoreErrors(dublinCore, NO_CUSTOMER);
        assertThat(errors, not(hasItems(new ErrorDetails(INVALID_DC_DATE_ISSUED))));
    }
}

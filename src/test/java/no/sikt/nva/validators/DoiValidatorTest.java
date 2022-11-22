package no.sikt.nva.validators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.List;
import java.util.Optional;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.scrapers.DublinCoreFactory;
import org.junit.jupiter.api.Test;

public class DoiValidatorTest {

    @Test
    void shouldValidateDoiWithoutBaseUrlCorrectly() {
        var doiWithoutBaseUrl = "10.5194/nhess-2018-174";
        var dcType = new DcValue(Element.TYPE, null, "Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, doiWithoutBaseUrl);
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));
        var expectedDoiErrors = Optional.empty();
        var actualDoiErrors = DoiValidator.getDoiErrorDetailsOffline(dublinCoreWithDoi);
        assertThat(actualDoiErrors, is(equalTo(expectedDoiErrors)));
    }

    @Test
    void shouldValidateDoiWithoutHttpsStringCorrectly() {
        var doiWithoutBaseUrl = "doi.org/10.5194/nhess-2018-174";
        var dcType = new DcValue(Element.TYPE, null, "Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, doiWithoutBaseUrl);
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));
        var expectedDoiErrors = Optional.empty();
        var actualDoiErrors = DoiValidator.getDoiErrorDetailsOffline(dublinCoreWithDoi);
        assertThat(actualDoiErrors, is(equalTo(expectedDoiErrors)));
    }

    @Test
    void shouldValidateDoiCorrectly() {
        var doiWithoutBaseUrl = "https://doi.org/10.5194/nhess-2018-174";
        var dcType = new DcValue(Element.TYPE, null, "Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, doiWithoutBaseUrl);
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));
        var expectedDoiErrors = Optional.empty();
        var actualDoiErrors = DoiValidator.getDoiErrorDetailsOffline(dublinCoreWithDoi);
        assertThat(actualDoiErrors, is(equalTo(expectedDoiErrors)));
    }

    @Test
    void shouldReturnEmptyErrorListWhenDoiIsEmpty() {
        var doi = "";
        var dcType = new DcValue(Element.TYPE, null, "Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, doi);
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));
        var expectedDoiErrors = Optional.empty();
        var actualDoiErrorsOffline = DoiValidator.getDoiErrorDetailsOffline(dublinCoreWithDoi);
        var actualDoiErrorsOnline = DoiValidator.getDoiErrorDetailsOnline(dublinCoreWithDoi);

        assertThat(actualDoiErrorsOffline, is(equalTo(expectedDoiErrors)));
        assertThat(actualDoiErrorsOnline, is(equalTo(expectedDoiErrors)));
    }

}

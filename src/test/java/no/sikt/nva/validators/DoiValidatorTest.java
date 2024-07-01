package no.sikt.nva.validators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.scrapers.DublinCoreFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DoiValidatorTest {

    public static Stream<Arguments> doiProvider() {
        return Stream.of(
            Arguments.of("DOI:10.1371/journal.pone.0118594"),
            Arguments.of("doi:10.1371/journal.pone.0118594"),
            Arguments.of("DOI:10.1371/journal.pone.0125743"),
            Arguments.of("https://doi.org/10.5194/nhess-2018-174"),
            Arguments.of("doi.org/10.5194/nhess-2018-174"),
            Arguments.of("10.5194/nhess-2018-174"),
            Arguments.of("https://doi.org/10.1577/1548-8667(1998)010<0056:EOOAFI>2.0.CO;2"),
            Arguments.of("https://doi.org/10.1890/0012-9658(2006)87[2915:DMWITM]2.0.CO;2"),
            Arguments.of("https://doi.org/10.2983/0730-8000(2008)27[525:EOAMRF]2.0.CO;2"),
            Arguments.of("https:/doi.org/10.1080/08039410.2015.1114517"),
            Arguments.of("http://dx.doi.org/10.5334/ah.be"),
            Arguments.of("https://doi.org/10.1155/2021/6684334"),
            Arguments.of("https://doi.org/10.1016/j.isci. 2020.101414"),
            Arguments.of("https://doi.org/10.1007/978-3-031-05276-7_12#DOI"),
            Arguments.of("https://doi.org/10.1007/JHEP05%282024%29260")
        );
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

    @ParameterizedTest
    @MethodSource("doiProvider")
    void shouldNotCreateErrorWhenDoiIsValid(String doi) {
        var dcType = new DcValue(Element.TYPE, null, "Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, doi);
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));

        var doiErrors = DoiValidator.getDoiErrorDetailsOffline(dublinCoreWithDoi);
        assertThat(doiErrors, is(equalTo(Optional.empty())));
    }
}

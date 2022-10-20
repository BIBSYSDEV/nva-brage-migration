package no.sikt.nva.scrapers;

import static no.sikt.nva.scrapers.DublinCoreScraper.ADVISOR;
import static no.sikt.nva.scrapers.DublinCoreScraper.CONTRIBUTOR;
import static no.sikt.nva.scrapers.DublinCoreScraper.FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE;
import static no.unit.nva.testutils.RandomDataGenerator.randomDoi;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.file.Path;
import java.util.List;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.record.Contributor;
import no.sikt.nva.model.record.Identity;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

public class DublinCoreScraperTest {

    @Test
    void shouldReturnExceptionIfResourceIsInCristin() {
        var brageLocation = new BrageLocation(Path.of("somebundle/someindex"));
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var cristinDcValue = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN, "cristinIdentifier");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, cristinDcValue));
        assertThrows(DublinCoreException.class,
                     () -> DublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                        brageLocation));
    }

    @Test
    void shouldLogDcValuesThatAreNotUsedForScraping() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var expectedDcValuedLogged = new DcValue(Element.DESCRIPTION, Qualifier.PROVENANCE,
                                                 "Gurba Gurba gurba gurba gurba gurba "
                                                 + "gurba gurba gurba gurba gurba gurba gurba "
                                                 + "gurba gurba gurba gurba gurba (øæsdfadfåp)");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, expectedDcValuedLogged));
        DublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), containsString(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE));
        assertThat(appender.getMessages(), containsString(expectedDcValuedLogged.toXmlString()));
    }

    @Test
    void shouldConvertValidVersionToPublisherAuthority() {
        var expectedPublisherAuthority = true;
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "publishedVersion");
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(versionDcValue, typeDcValue));
        var record = DublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority();
        assertThat(actualPublisherAuthority, is(equalTo(expectedPublisherAuthority)));
    }

    @Test
    void shouldCreatePublisherAuthorityIfVersionIsNotPresent() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var dublinCoreWithoutVersion = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var record = DublinCoreScraper.validateAndParseDublinCore(dublinCoreWithoutVersion, new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority();
        assertThat(actualPublisherAuthority, is(equalTo(null)));
    }

    @Test
    void shouldCreateContributor() {
        List<Contributor> expectedContributors = List.of(
            new Contributor(CONTRIBUTOR, new Identity("Some Person"), ADVISOR));
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var advisorDcValue = new DcValue(Element.CONTRIBUTOR, Qualifier.ADVISOR, "Some Person");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(advisorDcValue, typeDcValue));
        var record = DublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        var actualContributors = record.getContributors();
        assertThat(actualContributors, is(equalTo(expectedContributors)));
    }

    @Test
    void ifTwoDoiSArePresentOneWillBeScrapedTheOtherLogged() {
        var expectedLogMessage = "<dcValue element=\"identifier\" qualifier=\"doi\">";
        var dcValues = List.of(
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, randomDoi().toString()),
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, randomDoi().toString()),
            new DcValue(Element.TYPE, null, "Book"));
        var brageLocation = new BrageLocation(Path.of("some", "ignoredPath"));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = DublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getDoi(), is(notNullValue()));
        assertThat(appender.getMessages(),
                   allOf(containsString(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE),
                         containsString(expectedLogMessage)));
    }

    @Test
    void shouldScrapeTitleAndAlternativeTitles() {
        var expectedMainTitle = "mainTitle";
        var expectedAlternativeTitle1 = "alternativeTitle1";
        var expectedAlternativeTitle2 = "alternativeTitle2";
        var dcValues = List.of(
            new DcValue(Element.TITLE, Qualifier.NONE, expectedMainTitle),
            new DcValue(Element.TITLE, Qualifier.ALTERNATIVE, expectedAlternativeTitle1),
            new DcValue(Element.TITLE, Qualifier.ALTERNATIVE, expectedAlternativeTitle2),
            new DcValue(Element.TYPE, null, "Book"));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = DublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));

        assertThat(record.getMainTitle(), is(equalTo(expectedMainTitle)));
        assertThat(record.getAlternativeTitles(),
                   containsInAnyOrder(expectedAlternativeTitle1, expectedAlternativeTitle2));
    }
}

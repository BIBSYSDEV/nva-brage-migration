package no.sikt.nva.scrapers;

import static no.sikt.nva.scrapers.DublinCoreScraper.ADVISOR;
import static no.sikt.nva.scrapers.DublinCoreScraper.CONTRIBUTOR;
import static no.sikt.nva.scrapers.DublinCoreScraper.FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE;
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
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        assertThrows(DublinCoreException.class,
                     () -> dublinCoreScraper
                               .validateAndParseDublinCore(dublinCore, brageLocation));
    }

    @Test
    void shouldLogDcValuesThatAreNotUsedForScraping() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var expectedDcValuedLogged = new DcValue(null, null,
                                                 "Gurba Gurba gurba gurba gurba gurba "
                                                 + "gurba gurba gurba gurba gurba gurba gurba "
                                                 + "gurba gurba gurba gurba gurba (øæsdfadfåp)");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, expectedDcValuedLogged));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), containsString(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE));
        assertThat(appender.getMessages(), containsString(expectedDcValuedLogged.toXmlString()));
    }

    @Test
    void shouldConvertValidVersionToPublisherAuthority() {
        var expectedPublisherAuthority = true;
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "publishedVersion");
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(versionDcValue, typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                  new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority();
        assertThat(actualPublisherAuthority, is(equalTo(expectedPublisherAuthority)));
    }

    @Test
    void shouldCreatePublisherAuthorityIfVersionIsNotPresent() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var dublinCoreWithoutVersion = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCoreWithoutVersion,
                                                                  new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority();
        assertThat(actualPublisherAuthority, is(equalTo(null)));
    }

    @Test
    void shouldCreateContributor() {

        List<Contributor> expectedContributors = List.of(
            new Contributor(CONTRIBUTOR, new Identity("Some Person"), ADVISOR, Qualifier.ADVISOR.getValue()));
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var advisorDcValue = new DcValue(Element.CONTRIBUTOR, Qualifier.ADVISOR, "Some Person");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(advisorDcValue, typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                  new BrageLocation(null));
        var actualContributors = record.getEntityDescription().getContributors();
        assertThat(actualContributors, is(equalTo(expectedContributors)));
    }

    @Test
    void ifTwoDoiSArePresentOneWillBeScrapedTheOtherLogged() {
        var expectedLogMessage = "<dcValue element=\"identifier\" qualifier=\"doi\">";
        var dcValues = List.of(
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://doi.org/10.1016/j.scitotenv.2021.151958"),
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://doi.org/10.1016/j.scitotenv.2021.151958"),
            new DcValue(Element.TYPE, null, "Book"));
        var brageLocation = new BrageLocation(Path.of("some", "ignoredPath"));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
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
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));

        assertThat(record.getEntityDescription().getMainTitle(), is(equalTo(expectedMainTitle)));
        assertThat(record.getEntityDescription().getAlternativeTitles(),
                   containsInAnyOrder(expectedAlternativeTitle1, expectedAlternativeTitle2));
    }

    @Test
    void shouldScrapePartOfSeriesDcValue() {
        var partOfSeries = "Part of some series";
        var partOfSeriesDcValue = new DcValue(Element.RELATION, Qualifier.IS_PART_OF_SERIES, partOfSeries);
        var typeDcValue = new DcValue(Element.TYPE, null, "Book");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(partOfSeriesDcValue, typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getPublication().getPartOfSeries(), is(equalTo(partOfSeries)));
    }

    @Test
    void shouldReturnJournalIdFromChannelRegisterForJournalWithValidIssn() {
        var issnToJournalArticle = "2038-324X";
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, issnToJournalArticle);
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var dateDcValue = new DcValue(Element.DATE, Qualifier.ISSUED, "2020");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(issnDcValue, typeDcValue, dateDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getPublication().getId(), is(equalTo("503077/2020")));
    }

    @Test
    void shouldReturnIdFromChannelRegisterForResourceWithValidIssn() {
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "somePublisher");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");
        var typeDcValue = new DcValue(Element.TYPE, null, "Research Report");
        var dateDcValue = new DcValue(Element.DATE, Qualifier.ISSUED, "2020");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(publisherDcValue, issnDcValue, typeDcValue, dateDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getPublication().getId(), is(equalTo("450187/2020")));
    }
}

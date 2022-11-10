package no.sikt.nva.scrapers;

import static no.sikt.nva.channelregister.ChannelRegister.NOT_FOUND_IN_CHANNEL_REGISTER;
import static no.sikt.nva.model.WarningDetails.Warning.MULTIPLE_UNMAPPABLE_TYPES;
import static no.sikt.nva.model.WarningDetails.Warning.PAGE_NUMBER_FORMAT_NOT_RECOGNIZED;
import static no.sikt.nva.model.WarningDetails.Warning.SUBJECT_WARNING;
import static no.sikt.nva.scrapers.DublinCoreScraper.FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.ADVISOR;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.CONTRIBUTOR;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.model.record.Contributor;
import no.sikt.nva.model.record.Identity;
import no.sikt.nva.model.record.Pages;
import no.sikt.nva.model.record.Range;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DublinCoreScraperTest {

    @Test
    void shouldLogDcValuesThatAreNotUsedForScraping() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
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
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
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
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
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
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
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
            new DcValue(Element.TYPE, null, "Others"));
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
            new DcValue(Element.TYPE, null, "Others"));
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
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
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
        assertThat(record.getPublication().getId(), is(equalTo("503077")));
    }

    @Test
    void shouldReturnIdFromChannelRegisterForResourceWithValidIssn() {
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "somePublisher");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");
        var typeDcValue = new DcValue(Element.TYPE, null, "Report");
        var dateDcValue = new DcValue(Element.DATE, Qualifier.ISSUED, "2020");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(publisherDcValue, issnDcValue, typeDcValue, dateDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getPublication().getId(), is(equalTo("450187")));
    }

    @Test
    void shouldLogResourceWithMultipleTypes() {
        var typeDcValue1 = new DcValue(Element.TYPE, null, "Journal Article");
        var typeDcValue2 = new DcValue(Element.TYPE, null, "Book");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue1, typeDcValue2));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), containsString(MULTIPLE_UNMAPPABLE_TYPES.toString()));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogSingleTypeValues() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogConvertiblePeerReviewedValues() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer Reviewed");

        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, peerReviewed));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogConvertiblePeerReviewedValuesV2() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer Reviewed");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(peerReviewed, typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @ParameterizedTest
    @MethodSource("provideDcValueAndExpectedPages")
    void shouldExtractPagesWithDifferentFormats(DcValue pageNumber, Pages expectedPages) {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer Reviewed");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(peerReviewed, typeDcValue, pageNumber));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getEntityDescription().getPublicationInstance().getPages(), is(equalTo(expectedPages)));
        assertThat(appender.getMessages(), not(containsString(PAGE_NUMBER_FORMAT_NOT_RECOGNIZED.toString())));
    }

    @Test
    void shouldLogWarningIfPageNumberIsNotRecognized() {
        var unrecognizedPagenumber = randomString();
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer Reviewed");
        var unrecognizedPageNumber = new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, unrecognizedPagenumber);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(unrecognizedPageNumber, typeDcValue, peerReviewed));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getEntityDescription().getPublicationInstance().getPages(),
                   is(equalTo(new Pages(unrecognizedPagenumber,
                                        null, null))));
        assertThat(appender.getMessages(), containsString(PAGE_NUMBER_FORMAT_NOT_RECOGNIZED.toString()));
    }

    @Test
    void shouldScrapeNormalSubjectsToRecordAndIgnoreNsiTagsSilently() {
        var tag1 = randomString();
        var tag2 = randomString();
        var tag3 = randomString();
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer Reviewed");
        var normalTagWithQualifierNone = new DcValue(Element.SUBJECT, Qualifier.NONE, tag1);
        var normalTagWithQualifierKeyword = new DcValue(Element.SUBJECT, Qualifier.KEYWORD, tag2);
        var nsiTag = new DcValue(Element.SUBJECT, Qualifier.NORWEGIAN_SCIENCE_INDEX, tag3);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(normalTagWithQualifierNone,
                    normalTagWithQualifierKeyword,
                    nsiTag, typeDcValue,
                    peerReviewed));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getEntityDescription().getTags(), containsInAnyOrder(tag1, tag2));
        assertThat(record.getEntityDescription().getTags(), not(contains(tag3)));
        assertThat(appender.getMessages(), not(containsString(SUBJECT_WARNING.toString())));
    }

    @Test
    void shouldScrapeUnrecognizedSubjectsAndWarnAboutUnrecognizedSubject() {
        var tag = randomString();
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer Reviewed");
        var normalTagWithUnrecognizedQualifier = new DcValue(Element.SUBJECT, Qualifier.AGROVOC, tag);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(normalTagWithUnrecognizedQualifier, typeDcValue, peerReviewed));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getEntityDescription().getTags(), contains(tag));
        assertThat(appender.getMessages(), containsString(SUBJECT_WARNING.toString()));
    }

    @Test
    void shouldNotLookInChannelRegisterForOtherType() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "Publisher");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, publisherDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(NOT_FOUND_IN_CHANNEL_REGISTER)));
    }

    private static Stream<Arguments> provideDcValueAndExpectedPages() {
        return Stream.of(
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "96"), new Pages("96", null, "96")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "96 s."), new Pages("96 s.", null, "96")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "s. 96"), new Pages("s. 96", new Range(
                "96", "96"), "1")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "34-89"), new Pages("34-89", new Range(
                "34", "89"), "55"))
        );
    }
}

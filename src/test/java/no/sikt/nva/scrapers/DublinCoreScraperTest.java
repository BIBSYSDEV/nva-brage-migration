package no.sikt.nva.scrapers;

import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_VALUE;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DOI_OFFLINE_CHECK;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_LANGUAGE;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_TYPE;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_LANGUAGES_PRESENT;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_UNMAPPABLE_TYPES;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_VERSIONS;
import static no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning.PAGE_NUMBER_FORMAT_NOT_RECOGNIZED;
import static no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning.SUBJECT_WARNING;
import static no.sikt.nva.channelregister.ChannelRegister.NOT_FOUND_IN_CHANNEL_REGISTER;
import static no.sikt.nva.scrapers.DublinCoreScraper.FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.ADVISOR;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.ErrorDetails.Error;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Identity;
import no.sikt.nva.brage.migration.common.model.record.Pages;
import no.sikt.nva.brage.migration.common.model.record.Range;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class DublinCoreScraperTest {

    private final boolean shouldLookUpInChannelRegister = true;

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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                  new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();
        assertThat(actualPublisherAuthority, is(equalTo(expectedPublisherAuthority)));
    }

    @Test
    void shouldConvertMultipleIdenticalValidVersionsToPublisherAuthority() {
        var expectedPublisherAuthority = true;
        var firstVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "publishedVersion");
        var secondVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "publishedVersion");
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(firstVersionDcValue, secondVersionDcValue, typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                  new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();
        assertThat(actualPublisherAuthority, is(equalTo(expectedPublisherAuthority)));
        assertThat(appender.getMessages(), not(containsString(String.valueOf(MULTIPLE_VERSIONS))));
    }

    @Test
    void shouldConvertDifferentValidVersionsToPublisherAuthority() {
        var expectedPublisherAuthority = true;
        var firstVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "publishedVersion");
        var secondVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "submittedVersion");
        var thirdVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "acceptedVersion");
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(firstVersionDcValue, secondVersionDcValue, thirdVersionDcValue, typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                  new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();
        assertThat(actualPublisherAuthority, is(equalTo(expectedPublisherAuthority)));
        assertThat(appender.getMessages(), not(containsString(String.valueOf(MULTIPLE_VERSIONS))));
    }

    @Test
    void shouldExtractVersionFromDescriptionVersionAndTypeVersionDcValues() {
        var expectedPublisherAuthority = true;
        var firstVersionDcValue = new DcValue(Element.TYPE, Qualifier.VERSION, "publishedVersion");
        var secondVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "submittedVersion");
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(firstVersionDcValue, secondVersionDcValue, typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                  new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();
        assertThat(actualPublisherAuthority, is(equalTo(expectedPublisherAuthority)));
        assertThat(appender.getMessages(), not(containsString(String.valueOf(MULTIPLE_VERSIONS))));
    }

    @Test
    void shouldCreatePublisherAuthorityIfVersionIsNotPresent() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCoreWithoutVersion = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCoreWithoutVersion,
                                                                  new BrageLocation(null));
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();
        assertThat(actualPublisherAuthority, is(equalTo(null)));
    }

    @Test
    void shouldCreateContributor() {

        List<Contributor> expectedContributors = List.of(
            new Contributor(new Identity("Person Some", null), ADVISOR, Qualifier.ADVISOR.getValue(), List.of()));
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var advisorDcValue = new DcValue(Element.CONTRIBUTOR, Qualifier.ADVISOR, "Some, Person");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(advisorDcValue, typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getPublication().getPublicationContext().getJournal().getId(), is(equalTo("503077")));
    }

    @Test
    void shouldReturnIdFromChannelRegisterForResourceWithValidIssn() {
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "somePublisher");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");
        var typeDcValue = new DcValue(Element.TYPE, null, "Report");
        var dateDcValue = new DcValue(Element.DATE, Qualifier.ISSUED, "2020");
        var partOfSeriesDcValue = new DcValue(Element.RELATION, Qualifier.IS_PART_OF_SERIES, "NVE Rapport;2019:1");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(publisherDcValue, issnDcValue, typeDcValue, dateDcValue, partOfSeriesDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        String seriesId = record.getPublication().getPublicationContext().getSeries().getId();
        assertThat(seriesId, is(equalTo("450187")));
    }

    @Test
    void shouldLogResourceWithMultipleTypes() {
        var typeDcValue1 = new DcValue(Element.TYPE, null, "Journal Article");
        var typeDcValue2 = new DcValue(Element.TYPE, null, "Book");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue1, typeDcValue2));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogMultipleSameTypeValues() {
        var firstTypeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var secondTypeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(firstTypeDcValue, secondTypeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogConvertiblePeerReviewedValues() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer reviewed");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");

        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, peerReviewed, issnDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogConvertiblePeerReviewedValuesV2() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer reviewed");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(peerReviewed, typeDcValue, issnDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
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
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getEntityDescription().getTags(), contains(tag));
        assertThat(appender.getMessages(), containsString(SUBJECT_WARNING.toString()));
    }

    @Test
    void nonIsoLanguageShouldBeLoggedAsError() {
        var nonIsoLanguage = randomString();
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer reviewed");
        var nonIsoLanguageDcValue = new DcValue(Element.LANGUAGE, Qualifier.ISO, nonIsoLanguage);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(nonIsoLanguageDcValue, typeDcValue, peerReviewed));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getErrors(), hasItem(new ErrorDetails(INVALID_LANGUAGE, List.of(nonIsoLanguage))));
    }

    @Test
    void shouldApplyIsoLanguageOverLanguageWhenBothArePresentAndDoNotErrorIfSameLanguages() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer reviewed");
        var isoLanguageDcValue = new DcValue(Element.LANGUAGE, Qualifier.ISO, "ger");
        var nonIsoLanguageDcValue = new DcValue(Element.LANGUAGE, null, "ger");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(nonIsoLanguageDcValue, isoLanguageDcValue, typeDcValue, peerReviewed));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var brageLocation = new BrageLocation(null);
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getErrors(), not(hasItem(new ErrorDetails(MULTIPLE_LANGUAGES_PRESENT, List.of()))));
    }

    @Test
    void shouldApplyIsoLanguageOverLanguageWhenBothArePresentAndDoNotLogErrorIfValidIsoLanguage() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer reviewed");
        var isoLanguageDcValue = new DcValue(Element.LANGUAGE, Qualifier.ISO, "ger");
        var nonIsoLanguageDcValue = new DcValue(Element.LANGUAGE, null, "deutsch");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(nonIsoLanguageDcValue, isoLanguageDcValue, typeDcValue, peerReviewed));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var brageLocation = new BrageLocation(null);
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getErrors(), not(hasItem(new ErrorDetails(MULTIPLE_LANGUAGES_PRESENT, List.of()))));
    }

    @Test
    void shouldNotLookInChannelRegisterForOtherType() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "Publisher");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, publisherDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(NOT_FOUND_IN_CHANNEL_REGISTER)));
    }

    @Test
    void shouldSetAccessionedDateAsScraped() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "Publisher");
        var cristinDcValue = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN, "12345");
        var availableDateDcValue = new DcValue(Element.DATE, Qualifier.AVAILABLE, "date");
        var accessDateDcValue = new DcValue(Element.DATE, Qualifier.ACCESSIONED, "date");
        var accessDateDcValue2 = new DcValue(Element.DATE, Qualifier.ACCESSIONED, "date2");

        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, publisherDcValue, accessDateDcValue, availableDateDcValue, cristinDcValue,
                    accessDateDcValue2));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dublinCoreScraper
            .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), not(containsString(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE)));
    }

    @Test
    void shouldConvertTwoDigitYearToFourDigitYear() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var date = new DcValue(Element.DATE, Qualifier.ISSUED, "22");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, date));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getEntityDescription().getPublicationDate().getNva().getYear(), is(equalTo("2022")));
    }

    @Test
    void shouldNotThroughExceptionWhenInvalidDateButLogAsError() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var date = new DcValue(Element.DATE, Qualifier.ISSUED, "222");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, date));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), containsString(Error.INVALID_DATE_ERROR.toString()));
    }

    @Test
    void shouldMapToFirstMappableTypeWhenManyTypesAreUnmappable() {
        var type1DcValue = new DcValue(Element.TYPE, null, "Others");
        var type2DcValue = new DcValue(Element.TYPE, null, "Conference object");
        var type3DcValue = new DcValue(Element.TYPE, null, "Book");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(type1DcValue, type2DcValue, type3DcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dublinCoreScraper
            .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), containsString(MULTIPLE_UNMAPPABLE_TYPES.toString()));
    }

    @Test
    void shouldLogUnmappableType() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Conference object");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dublinCoreScraper
            .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(appender.getMessages(), containsString(INVALID_TYPE.toString()));
    }

    @Test
    void shouldLoggInvalidDoi() {
        var doi = "10.1016/ S0140-6736wefwfg.(20)30045-#%wt3";
        var dcType = new DcValue(Element.TYPE, null, "Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, doi);
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of())
            .validateAndParseDublinCore(dublinCoreWithDoi, new BrageLocation(null));
        assertThat(appender.getMessages(), containsString(INVALID_DOI_OFFLINE_CHECK.toString()));
    }

    @Test
    void shouldNotLoggInvalidDoiWhenDoiIsFixedDuringScraping() {
        var doi = "doi:10.1007/s12062-016-9157-z";
        var dcType = new DcValue(Element.TYPE, null, "Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, doi);
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of())
                         .validateAndParseDublinCore(dublinCoreWithDoi, new BrageLocation(null));
        assertThat(record.getDoi().toString(), is(equalTo("https://doi.org/10.1007/s12062-016-9157-z")));
        assertThat(appender.getMessages(), not(containsString(INVALID_DOI_OFFLINE_CHECK.toString())));
    }

    @Test
    void shouldSetPublisherAuthorityToFalseWhenVersionIsAcceptedVersion() {
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "acceptedVersion");
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, versionDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        assertThat(record.getPublisherAuthority().getNva(), is(false));
    }

    @Test
    void shouldLogUnknownVersionsAndApplyNullValue() {
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "submittedVersion");
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, versionDcValue));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getPublisherAuthority().getNva(), is(nullValue()));
    }

    @Test
    void shouldScrapeIdFromChannelRegisterWhenMapsToScientificArticle() {
        var dcType1 = new DcValue(Element.TYPE, null, "Journal article");
        var dcType2 = new DcValue(Element.TYPE, null, "Peer reviewed");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1664-0640");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType1, dcType2, issnDcValue));
        var brageLocation = new BrageLocation(null);
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        String idFromChannelRegister = "477294";
        var journalId = record.getPublication().getPublicationContext().getJournal().getId();
        assertThat(journalId, is(equalTo(idFromChannelRegister)));
    }

    @Test
    void shouldScrapeIdFromPublishersInChannelRegisterWhenReportFromNVE() {
        var expectedPublisherId = "28065";
        var dcType = new DcValue(Element.TYPE, null, "Report");
        var dcPublisher = new DcValue(Element.PUBLISHER, null, "NVE");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var brageLocation = new BrageLocation(null);
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        var publisherId = record.getPublication().getPublicationContext().getPublisher().getId();
        assertThat(publisherId, is(equalTo(expectedPublisherId)));
    }

    @Test
    void shouldScrapeIdFromPublishersInChannelRegisterWhenReportFromKrus() {
        var expectedPublisherId = "28073";
        var dcType = new DcValue(Element.TYPE, null, "Report");
        var dcPublisher = new DcValue(Element.PUBLISHER, null, "KRUS");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var brageLocation = new BrageLocation(null);
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        var publisherId = record.getPublication().getPublicationContext().getPublisher().getId();
        assertThat(publisherId, is(equalTo(expectedPublisherId)));
    }

    @Test
    void shouldScrapeIdFromPublishersInChannelRegisterWhenReportFromFHS() {
        var expectedPublisherId = "14088";
        var dcType = new DcValue(Element.TYPE, null, "Report");
        var dcPublisher = new DcValue(Element.PUBLISHER, null, "Orkana Forlag");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var brageLocation = new BrageLocation(null);
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        var publisherId = record.getPublication().getPublicationContext().getPublisher().getId();
        assertThat(publisherId, is(equalTo(expectedPublisherId)));
    }

    @Test
    void shouldScrapeIdFromPublishersAndJournalsWhenReportAndPartOfSeries() {
        var expectedPublisherId = "28073";
        var expectedSeriesId = "450187";
        var dcType = new DcValue(Element.TYPE, null, "Report");
        var dcPublisher = new DcValue(Element.PUBLISHER, null, "KRUS");
        var dcIssn = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");
        var dcPartOfSeries = new DcValue(Element.RELATION, Qualifier.IS_PART_OF_SERIES, "NVE Rapport;2022:13");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(dcType, dcPublisher, dcIssn, dcPartOfSeries));
        var brageLocation = new BrageLocation(null);
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        var publisherId = record.getPublication().getPublicationContext().getPublisher().getId();
        var seriesId = record.getPublication().getPublicationContext().getSeries().getId();

        assertThat(publisherId, is(equalTo(expectedPublisherId)));
        assertThat(seriesId, is(equalTo(expectedSeriesId)));
    }

    @Test
    void shouldApplyFirstIssueValueIfManyAndLog() {
        var type = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        var issue1 = new DcValue(Element.SOURCE, Qualifier.ISSUE, "21");
        var issue2 = new DcValue(Element.SOURCE, Qualifier.ISSUE, "21/2017");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(type, issue1, issue2));
        var dublinCoreScraper = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(appender.getMessages(), containsString(String.valueOf(DUPLICATE_VALUE)));
    }

    @Test
    void shouldApplyFirstCristinValueIfManyAndLog() {
        var type = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        String expectedCristinId = "12498235";
        var cristin1 = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN, "someValue");
        var cristin2 = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN, expectedCristinId);
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(type, cristin1, cristin2));
        var dublinCoreScraper = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(appender.getMessages(), containsString(String.valueOf(DUPLICATE_VALUE)));
        assertThat(record.getCristinId(), is(equalTo(expectedCristinId)));
    }

    @Test
    void shouldLogWhenMultipleSearchResultsInChannelRegister() {
        var type = new DcValue(Element.TYPE, null, "Journal article");
        var journal = new DcValue(Element.SOURCE, Qualifier.JOURNAL, "Earth System Science Data");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(type, journal));
        var dublinCoreScraper = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(appender.getMessages(), containsString(
            String.valueOf(DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER)));
    }

    @Test
    void shouldRemoveContributorsWithoutNameFromContributors() {
        var type = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        var contributor1 = new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, "");
        var contributor2 = new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, null);
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(type, contributor1, contributor2));
        var dublinCoreScraper = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getEntityDescription().getContributors(), is(empty()));
    }

    @Test
    void shouldMapTypesContainingMultipleLanguagesTypesAndPeerReviewedToValidType() {
        var type0 = new DcValue(Element.TYPE, null, "Peer reviewed");
        var type1 = new DcValue(Element.TYPE, null, "Tidsskriftartikkel");
        var type2 = new DcValue(Element.TYPE, null, "Journal article");
        var type3 = new DcValue(Element.TYPE, Qualifier.VERSION, "publishedVersion");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(type0, type1, type2, type3));
        var dublinCoreScraper = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getType().getNva(), is(equalTo(NvaType.SCIENTIFIC_ARTICLE.getValue())));

    }
    @Test
    void shouldExtractCristinIdContainingFridaIdIdentifier() {
        var type = new DcValue(Element.TYPE, null, "Tidsskriftartikkel");
        var cristinId = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN_ID_MUNIN, "FRIDAID 932785");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(cristinId, type));
        var dublinCoreScraper = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getCristinId(), is(equalTo("932785")));
    }

    @Test
    void shouldRepairDoiWithoutSlash() {
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var date = new DcValue(Element.DATE, Qualifier.ISSUED, "2010");
        var doi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://doi.org/10.1177%2F1757975910383936");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, date, doi));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getDoi().toString(), is(equalTo("https://doi.org/10.1177/1757975910383936")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"9788293091172(PDF)", "9788293091172(trykt)", "ISBN9788293091172"})
    void shouldRemoveAllSpecialCharactersAndLettersFromIsbn(String isbn) {
        var typeDcValue = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        var isbnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISBN, isbn);
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, isbnDcValue));
        var dublinCoreScraper = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getPublication().getIsbnList().get(0), is(equalTo("9788293091172")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1502-007x", "1502-007x (online)", "1502007x"})
    void shouldRemoveAllLettersAndInvalidSpecialCharactersFromIssn(String isbn) {
        var typeDcValue = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        var isbnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, isbn);
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, isbnDcValue));
        var dublinCoreScraper = new DublinCoreScraper(false, shouldLookUpInChannelRegister, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, brageLocation);
        assertThat(record.getPublication().getIssnList().get(0), is(equalTo("1502-007X")));
    }

    @ParameterizedTest
    @MethodSource("provideDcValueAndExpectedPages")
    void shouldExtractPagesWithDifferentFormats(DcValue pageNumber, Pages expectedPages) {
        var typeDcValue = new DcValue(Element.TYPE, null, "Journal Article");
        var peerReviewed = new DcValue(Element.TYPE, null, "Peer reviewed");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(peerReviewed, typeDcValue, pageNumber));
        var onlineValidationDisabled = false;
        var dublinCoreScraper = new DublinCoreScraper(onlineValidationDisabled, shouldLookUpInChannelRegister,
                                                      Map.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dublinCoreScraper
                         .validateAndParseDublinCore(dublinCore, new BrageLocation(null));
        var actualPages = record.getEntityDescription().getPublicationInstance().getPages();
        assertThat(actualPages, is(equalTo(expectedPages)));
        assertThat(appender.getMessages(), not(containsString(PAGE_NUMBER_FORMAT_NOT_RECOGNIZED.toString())));
    }

    private static Stream<Arguments> provideDcValueAndExpectedPages() {
        return Stream.of(
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "96"),
                         new Pages("96", null, "96")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "96 s."),
                         new Pages("96 s.", null, "96")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "s. 96"),
                         new Pages("s. 96", new Range("96", "96"), "1")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "34-89"),
                         new Pages("34-89", new Range("34", "89"), "55")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "[86] s."),
                         new Pages("[86] s.", null, "86")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "34–89"),
                         new Pages("34–89", new Range("34", "89"), "55")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "34 - 89"),
                         new Pages("34 - 89", new Range("34", "89"), "55"))

        );
    }
}

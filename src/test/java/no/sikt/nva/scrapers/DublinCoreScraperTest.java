package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DC_PUBLISHER_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_RIGHTS_URI;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_DC_TYPE;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.INVALID_ISSN;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_DC_LANGUAGES_PRESENT;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_DC_VERSION_VALUES;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_UNMAPPABLE_TYPES;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_VALUES;
import static no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties.ABSTRACT;
import static no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties.ALTERNATIVE_ABSTRACTS;
import static no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties.ALTERNATIVE_TITLES;
import static no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties.FUNDINGS;
import static no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties.MAIN_TITLE;
import static no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties.PUBLISHER;
import static no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties.REFERENCE;
import static no.sikt.nva.brage.migration.common.model.record.PrioritizedProperties.TAGS;
import static no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning.LANGUAGE_MAPPED_TO_UNDEFINED;
import static no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning.PAGE_NUMBER_FORMAT_NOT_RECOGNIZED;
import static no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning.SUBJECT_WARNING;
import static no.sikt.nva.channelregister.ChannelRegister.NOT_FOUND_IN_CHANNEL_REGISTER;
import static no.sikt.nva.scrapers.DublinCoreScraper.FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.OTHER_CONTRIBUTOR;
import static no.sikt.nva.scrapers.EntityDescriptionExtractor.SUPERVISOR;
import static no.sikt.nva.scrapers.LicenseScraper.DEFAULT_LICENSE;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.language.LanguageMapper.LEXVO_URI_UNDEFINED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.ErrorDetails.Error;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Identity;
import no.sikt.nva.brage.migration.common.model.record.Pages;
import no.sikt.nva.brage.migration.common.model.record.PartOfSeries;
import no.sikt.nva.brage.migration.common.model.record.PublisherVersion;
import no.sikt.nva.brage.migration.common.model.record.Range;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails;
import no.sikt.nva.brage.migration.common.model.record.WarningDetails.Warning;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class DublinCoreScraperTest {

    private static final boolean SHOULD_LOOKUP_IN_CHANNEL_REGISTER = true;
    private static final String SOME_CUSTOMER = "nve";
    public static final String METADATA_FS_XML = "metadata_fs.xml";

    private static final boolean SHOULD_VALIDATE_ONLINE = false;
    private DublinCoreScraper dcScraper;

    public static Stream<Arguments> isPartOfSeriesProvider() {
        return Stream.of(
            Arguments.of(List.of(partOfSeries("SeriesName")),
                         new PartOfSeries("SeriesName", null)),
            Arguments.of(List.of(partOfSeries("SeriesName;   ")),
                         new PartOfSeries("SeriesName", null)),
            Arguments.of(List.of(partOfSeries("SeriesName"), partOfSeries("23")),
                         new PartOfSeries("SeriesName", "23")),
            Arguments.of(List.of(partOfSeries("SeriesName"), partOfSeries("23:2022")),
                         new PartOfSeries("SeriesName", "23:2022")),
            Arguments.of(List.of(partOfSeries("SeriesName; 23")),
                         new PartOfSeries("SeriesName", "23")),
            Arguments.of(List.of(partOfSeries("SeriesName; 23:2022")),
                         new PartOfSeries("SeriesName", "23:2022")),
            Arguments.of(List.of(partOfSeries("SeriesName; 23/2022")),
                         new PartOfSeries("SeriesName", "23/2022")),
            Arguments.of(List.of(partOfSeries("SeriesName; 23/2022")),
                         new PartOfSeries("SeriesName", "23/2022"))
        );
    }

    public static Stream<Arguments> doiProvider() {
        return Stream.of(
            Arguments.of("https://doi.org/10.1577/1548-8667(1998)010<0056:EOOAFI>2.0.CO;2","https://doi.org/10.1577/1548-8667%281998%29010%3C0056%3AEOOAFI%3E2.0.CO%3B2"),
            Arguments.of("https://doi.org/10.1890/0012-9658(2006)87[2915:DMWITM]2.0.CO;2","https://doi.org/10.1890/0012-9658%282006%2987%5B2915%3ADMWITM%5D2.0.CO%3B2"),
            Arguments.of("https://doi.org/10.2983/0730-8000(2008)27[525:EOAMRF]2.0.CO;2","https://doi.org/10.2983/0730-8000%282008%2927%5B525%3AEOAMRF%5D2.0.CO%3B2"),
            Arguments.of("https://doi.org/10.2983/0730-8000(2008)27[525:EOAMRF]2.0.CO;2/","https://doi.org/10.2983/0730-8000%282008%2927%5B525%3AEOAMRF%5D2.0.CO%3B2"),
            Arguments.of("http://dx.doi.org/10.5334/ah.be","https://doi.org/10.5334/ah.be"),
            Arguments.of("10.5194/tc-8-1885-2014","https://doi.org/10.5194/tc-8-1885-2014"),
            Arguments.of("doi.org/10.5194/tc-8-1885-2014", "https://doi.org/10.5194/tc-8-1885-2014"),
            Arguments.of("doi:10.5194/tc-8-1885-2014", "https://doi.org/10.5194/tc-8-1885-2014"),
            Arguments.of("DOI:10.1371/journal.pone.0125743", "https://doi.org/10.1371/journal.pone.0125743"),
            Arguments.of("https://doi.org/10.1177%2F1757975910383936", "https://doi.org/10.1177/1757975910383936"),
            Arguments.of("https://doi.org/10.1155/2021/6684334", "https://doi.org/10.1155/2021/6684334"),
            Arguments.of("https://doi.org/10.1016/j.isci. 2020.101414", "https://doi.org/10.1016/j.isci.2020.101414"),
            Arguments.of("http://doi.acm.org/10.1145/2530539", "https://doi.org/10.1145/2530539")
        );
    }

    public static Stream<Arguments> seriesSupplier() {
        return Stream.of(Arguments.of("Cicero Working papers", "2010:03"),
                         Arguments.of("2010:03", "Cicero Working papers"));
    }

    private static DcValue partOfSeries(String value) {
        return new DcValue(Element.RELATION, Qualifier.IS_PART_OF_SERIES, value);
    }

    @BeforeEach
    void init() {
        dcScraper = new DublinCoreScraper(
            SHOULD_VALIDATE_ONLINE,
            SHOULD_LOOKUP_IN_CHANNEL_REGISTER,
            Map.of()
        );
    }

    @Test
    void shouldLogDcValuesThatAreNotUsedForScraping() {
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var typeDcValue = toDcType("Others");
        var expectedDcValuedLogged = new DcValue(null, null, "Gurba Gurba gurba gurba gurba gurba "
                                                             + "gurba gurba gurba gurba gurba gurba gurba "
                                                             + "gurba gurba gurba gurba gurba (øæsdfadfåp)");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, expectedDcValuedLogged));
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), containsString(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE));
        assertThat(appender.getMessages(), containsString(expectedDcValuedLogged.toXmlString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"publishedVersion", "acceptedVersion"})
    void shouldConvertValidVersionToPublisherAuthority(String value) {
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, value);
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(versionDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();

        assertThat(actualPublisherAuthority, is(equalTo(PublisherVersion.fromValue(value))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"draft", "submittedVersion", "updatedVersion", "abcde"})
    void shouldMapNotSupportedVersionsToNull(String value) {
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, value);
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(versionDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();

        assertThat(actualPublisherAuthority, is(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"publishedVersion", "acceptedVersion", "abcd"})
    void shouldNotAddVersionToDescriptionList(String value) {
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, value);
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(versionDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        assertThat(record.getEntityDescription().getDescriptions(), not(contains(value)));
    }

    @Test
    void shouldConvertMultipleIdenticalValidVersionsToPublisherAuthority() {
        var expectedPublisherAuthority = PublisherVersion.PUBLISHED_VERSION;
        var firstVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "publishedVersion");
        var secondVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "publishedVersion");
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(firstVersionDcValue, secondVersionDcValue, typeDcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();
        assertThat(actualPublisherAuthority, is(equalTo(expectedPublisherAuthority)));
        assertThat(appender.getMessages(), not(containsString(String.valueOf(MULTIPLE_DC_VERSION_VALUES))));
    }

    @Test
    void shouldConvertDifferentValidVersionsToPublisherAuthority() {
        var firstVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "publishedVersion");
        var secondVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "submittedVersion");
        var thirdVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "acceptedVersion");
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(firstVersionDcValue, secondVersionDcValue, thirdVersionDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();

        assertThat(actualPublisherAuthority, is(equalTo(PublisherVersion.ACCEPTED_VERSION)));
    }

    @Test
    void shouldExtractVersionFromDescriptionVersionAndTypeVersionDcValues() {
        var firstVersionDcValue = new DcValue(Element.TYPE, Qualifier.VERSION, "publishedVersion");
        var secondVersionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "submittedVersion");
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(firstVersionDcValue, secondVersionDcValue, typeDcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();
        assertThat(actualPublisherAuthority, is(equalTo(PublisherVersion.PUBLISHED_VERSION)));
        assertThat(appender.getMessages(), not(containsString(String.valueOf(MULTIPLE_DC_VERSION_VALUES))));
    }

    @Test
    void shouldCreatePublisherAuthorityIfVersionIsNotPresent() {
        var typeDcValue = toDcType("Others");
        var dublinCoreWithoutVersion = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCoreWithoutVersion, new BrageLocation(null),
                                                          SOME_CUSTOMER);
        var actualPublisherAuthority = record.getPublisherAuthority().getNva();
        assertThat(actualPublisherAuthority, is(equalTo(null)));
    }

    @Test
    void shouldCreateContributor() {

        var contributor = new Contributor(new Identity("Person Some", null), SUPERVISOR,
                                                 Qualifier.ADVISOR.getValue(), Set.of());
        contributor.setSequence(1);
        var expectedContributors = List.of(
            contributor);
        var typeDcValue = toDcType("Others");
        var advisorDcValue = new DcValue(Element.CONTRIBUTOR, Qualifier.ADVISOR, "Some, Person");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(advisorDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualContributors = record.getEntityDescription().getContributors();
        assertThat(actualContributors, is(equalTo(expectedContributors)));
    }

    @Test
    void shouldCreateContributorWithoutAuthorStringFromContributorName() {
        var contributor = new Contributor(new Identity("Torbjørn Hanson", null), "Creator",
                                          Qualifier.AUTHOR.getValue(), Set.of());
        contributor.setSequence(1);
        var typeDcValue = toDcType("Others");
        var advisorDcValue = new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, "Author::Hanson, Torbjørn");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(advisorDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualContributors = record.getEntityDescription().getContributors();

        assertThat(actualContributors, is(equalTo(List.of(contributor))));
    }

    @Test
    void shouldCreateContributorFromDcElementCreator() {
        var contributor = new Contributor(new Identity("Person Some", null), SUPERVISOR,
                                                  Qualifier.ADVISOR.getValue(), Set.of());
        contributor.setSequence(1);
        var expectedContributors = List.of(contributor);
        var typeDcValue = toDcType("Others");
        var advisorDcValue = new DcValue(Element.CREATOR, Qualifier.ADVISOR, "Some, Person");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(advisorDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualContributors = record.getEntityDescription().getContributors();
        assertThat(actualContributors, is(equalTo(expectedContributors)));
    }

    @Test
    void shouldExtractContributorsInRightOrderAndInjectSequenceNumber() {
        var dc = DublinCoreFactory.createDublinCoreFromXml(new File(TEST_RESOURCE_PATH + "dc_with_duplicated_values.xml"));
        var record = dcScraper.validateAndParseDublinCore(dc, new BrageLocation(null), SOME_CUSTOMER);

        var expectedContributors = Arrays.asList(
            Map.entry("Jo Rune Ugulen", 1),
            Map.entry("Tone Merete Bruvik", 2),
            Map.entry("Rannveig Fluge", 3),
            Map.entry("Håkon Haugland", 4),
            Map.entry("Geir Atle Ersland", 5),
            Map.entry("Arne Solli", 6)
        );

        var actualContributors = record.getEntityDescription().getContributors();

        for (int i = 0; i < actualContributors.size(); i++) {
            var expectedSequence = i + 1;
            var contributor = actualContributors.stream()
                                  .filter(c -> c.getSequence().equals(expectedSequence))
                                  .findFirst()
                                  .orElseThrow();
            assertEquals(expectedContributors.get(i).getKey(), contributor.getIdentity().getName());
        }
    }

    @Test
    void shouldCreateContributorFromContributorOnly() {
        var contributor = new Contributor(new Identity("Person Some", null), OTHER_CONTRIBUTOR, null, Set.of());
        contributor.setSequence(1);
        var expectedContributors = List.of(
            contributor);
        var typeDcValue = toDcType("Others");
        var advisorDcValue = new DcValue(Element.CONTRIBUTOR, null, "Some, Person");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(advisorDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualContributors = record.getEntityDescription().getContributors();
        assertThat(actualContributors, is(equalTo(expectedContributors)));
    }

    @Test
    void ifTwoDoiSArePresentOneWillBeScrapedTheOtherLogged() {
        var expectedLogMessage = "<dcValue element=\"identifier\" qualifier=\"doi\">";
        var dcValues = List.of(
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://doi.org/10.1016/j.scitotenv.2021.151958"),
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://doi.org/10.1016/j.scitotenv.2021.151958"),
            toDcType("Others"));
        var brageLocation = new BrageLocation(Path.of("some", "ignoredPath"));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getDoi(), is(notNullValue()));
        assertThat(appender.getMessages(),
                   allOf(containsString(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE), containsString(expectedLogMessage)));
    }

    @Test
    void shouldScrapeTitleAndAlternativeTitles() {
        var expectedMainTitle = "mainTitle";
        var expectedAlternativeTitle1 = "alternativeTitle1";
        var expectedAlternativeTitle2 = "alternativeTitle2";
        var dcValues = List.of(new DcValue(Element.TITLE, Qualifier.NONE, expectedMainTitle),
                               new DcValue(Element.TITLE, Qualifier.ALTERNATIVE, expectedAlternativeTitle1),
                               new DcValue(Element.TITLE, Qualifier.ALTERNATIVE, expectedAlternativeTitle2),
                               toDcType("Others"));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore,
                                                          new BrageLocation(null),
                                                          SOME_CUSTOMER);

        assertThat(record.getEntityDescription().getMainTitle(), is(equalTo(expectedMainTitle)));
        assertThat(record.getEntityDescription().getAlternativeTitles(),
                   containsInAnyOrder(expectedAlternativeTitle1, expectedAlternativeTitle2));
    }

    @Test
    void shouldScrapePartOfSeriesDcValue() {
        var partOfSeries = new PartOfSeries("Part of some series", null);
        var partOfSeriesDcValue = partOfSeries(partOfSeries.getName());
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(partOfSeriesDcValue, typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getPublication().getPartOfSeries(), is(equalTo(partOfSeries)));
    }

    @Test
    void shouldReturnJournalIdFromChannelRegisterForJournalWithValidIssn() {
        var issnToJournalArticle = "2038-324X";
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, issnToJournalArticle);
        var typeDcValue = toDcType("Journal Article");
        var dateDcValue = new DcValue(Element.DATE, Qualifier.ISSUED, "2020");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(issnDcValue, typeDcValue, dateDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getPublication().getPublicationContext().getJournal().getPid(),
                   is(equalTo("70196DF2-7107-40F2-B6DF-045F3FAED38D")));
    }

    @Test
    void shouldReturnIdFromChannelRegisterForResourceWithValidIssn() {
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "somePublisher");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");
        var typeDcValue = toDcType("Report");
        var dateDcValue = new DcValue(Element.DATE, Qualifier.ISSUED, "2020");
        var partOfSeriesDcValue = partOfSeries("NVE Rapport;2019:1");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(publisherDcValue, issnDcValue, typeDcValue, dateDcValue, partOfSeriesDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        String seriesId = record.getPublication().getPublicationContext().getSeries().getPid();
        assertThat(seriesId, is(equalTo("7907D9CB-E44D-4CC0-9F1E-F67595F67AFE")));
    }

    @Test
    void shouldNotLogSingleTypeValues() {
        var typeDcValue = toDcType("Journal Article");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogMultipleSameTypeValues() {
        var firstTypeDcValue = toDcType("Journal Article");
        var secondTypeDcValue = toDcType("Journal Article");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(firstTypeDcValue, secondTypeDcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogConvertiblePeerReviewedValues() {
        var typeDcValue = toDcType("Journal article");
        var peerReviewed = toDcType("Peer reviewed");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "-1501-2832-");

        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, peerReviewed, issnDcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldNotLogConvertiblePeerReviewedValuesV2() {
        var typeDcValue = toDcType("Journal article");
        var peerReviewed = toDcType("Peer reviewed");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(peerReviewed, typeDcValue, issnDcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(record.getType(), is(notNullValue()));
    }

    @Test
    void shouldLogWarningIfPageNumberIsNotRecognized() {
        var unrecognizedPagenumber = randomString();
        var typeDcValue = toDcType("Journal Article");
        var peerReviewed = toDcType("Peer Reviewed");
        var unrecognizedPageNumber = new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, unrecognizedPagenumber);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(unrecognizedPageNumber, typeDcValue, peerReviewed));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getEntityDescription().getPublicationInstance().getPages(),
                   is(equalTo(new Pages(unrecognizedPagenumber, null, null))));
        assertThat(appender.getMessages(), containsString(PAGE_NUMBER_FORMAT_NOT_RECOGNIZED.toString()));
    }

    @Test
    void shouldScrapeNormalSubjectsToRecordAndIgnoreNsiTagsSilently() {
        var tag1 = randomString();
        var tag2 = randomString();
        var tag3 = randomString();
        var typeDcValue = toDcType("Journal Article");
        var peerReviewed = toDcType("Peer Reviewed");
        var normalTagWithQualifierNone = new DcValue(Element.SUBJECT, Qualifier.NONE, tag1);
        var normalTagWithQualifierKeyword = new DcValue(Element.SUBJECT, Qualifier.KEYWORD, tag2);
        var nsiTag = new DcValue(Element.SUBJECT, Qualifier.NORWEGIAN_SCIENCE_INDEX, tag3);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(normalTagWithQualifierNone, normalTagWithQualifierKeyword, nsiTag, typeDcValue, peerReviewed));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getEntityDescription().getTags(), containsInAnyOrder(tag1, tag2));
        assertThat(record.getEntityDescription().getTags(), not(contains(tag3)));
        assertThat(appender.getMessages(), not(containsString(SUBJECT_WARNING.toString())));
    }

    @Test
    void shouldScrapeSubjectAndRemoveTermsetEmneordFromItsValue() {
        var value = "TermSet Emneord::Kommunikasjon";
        var normalTagWithQualifierNone = new DcValue(Element.SUBJECT, null, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(normalTagWithQualifierNone, toDcType("Journal Article")));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var expectedTag = "Kommunikasjon";

        assertEquals(expectedTag, record.getEntityDescription().getTags().get(0));
    }


    @Test
    void shouldScrapeUnrecognizedSubjectsAndWarnAboutUnrecognizedSubject() {
        var tag = randomString();
        var typeDcValue = toDcType("Journal Article");
        var peerReviewed = toDcType("Peer Reviewed");
        var normalTagWithUnrecognizedQualifier = new DcValue(Element.SUBJECT, Qualifier.AGROVOC, tag);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(normalTagWithUnrecognizedQualifier, typeDcValue, peerReviewed));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getEntityDescription().getTags(), contains(tag));
        assertThat(appender.getMessages(), containsString(SUBJECT_WARNING.toString()));
    }

    @Test
    void nonIsoLanguageShouldLoggedAsWarning() {
        var nonIsoLanguage = randomString();
        var typeDcValue = toDcType("Journal Article");
        var peerReviewed = toDcType("Peer reviewed");
        var nonIsoLanguageDcValue = new DcValue(Element.LANGUAGE, Qualifier.ISO, nonIsoLanguage);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(nonIsoLanguageDcValue, typeDcValue, peerReviewed));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getWarnings(), hasItem(new WarningDetails(LANGUAGE_MAPPED_TO_UNDEFINED, Set.of(nonIsoLanguage))));
    }

    @Test
    void shouldApplyIsoLanguageOverLanguageWhenBothArePresentAndDoNotErrorIfSameLanguages() {
        var typeDcValue = toDcType("Journal Article");
        var peerReviewed = toDcType("Peer reviewed");
        var isoLanguageDcValue = new DcValue(Element.LANGUAGE, Qualifier.ISO, "ger");
        var nonIsoLanguageDcValue = new DcValue(Element.LANGUAGE, null, "ger");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(nonIsoLanguageDcValue, isoLanguageDcValue, typeDcValue, peerReviewed));
        var brageLocation = new BrageLocation(null);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getErrors(), not(hasItem(new ErrorDetails(MULTIPLE_DC_LANGUAGES_PRESENT, Set.of()))));
    }

    @Test
    void shouldApplyIsoLanguageOverLanguageWhenBothArePresentAndDoNotLogErrorIfValidIsoLanguage() {
        var typeDcValue = toDcType("Journal Article");
        var peerReviewed = toDcType("Peer reviewed");
        var isoLanguageDcValue = new DcValue(Element.LANGUAGE, Qualifier.ISO, "ger");
        var nonIsoLanguageDcValue = new DcValue(Element.LANGUAGE, null, "deutsch");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(nonIsoLanguageDcValue, isoLanguageDcValue, typeDcValue, peerReviewed));
        var brageLocation = new BrageLocation(null);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getErrors(), not(hasItem(new ErrorDetails(MULTIPLE_DC_LANGUAGES_PRESENT, Set.of()))));
    }

    @Test
    void shouldNotLookInChannelRegisterForOtherType() {
        var typeDcValue = toDcType("Others");
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "Publisher");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, publisherDcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), not(containsString(NOT_FOUND_IN_CHANNEL_REGISTER)));
    }

    @Test
    void shouldSetAccessionedDateAsScraped() {
        var typeDcValue = toDcType("Others");
        var publisherDcValue = new DcValue(Element.PUBLISHER, null, "Publisher");
        var cristinDcValue = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN, "12345");
        var availableDateDcValue = new DcValue(Element.DATE, Qualifier.AVAILABLE, "date");
        var accessDateDcValue = new DcValue(Element.DATE, Qualifier.ACCESSIONED, "date");
        var accessDateDcValue2 = new DcValue(Element.DATE, Qualifier.ACCESSIONED, "date2");

        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, publisherDcValue, accessDateDcValue, availableDateDcValue, cristinDcValue,
                    accessDateDcValue2));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), not(containsString(FIELD_WAS_NOT_SCRAPED_LOG_MESSAGE)));
    }

    @Test
    void shouldConvertTwoDigitYearToFourDigitYear() {
        var typeDcValue = toDcType("Others");
        var date = new DcValue(Element.DATE, Qualifier.ISSUED, "22");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, date));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getEntityDescription().getPublicationDate().getNva().getYear(), is(equalTo("2022")));
    }

    @Test
    void shouldNotThroughExceptionWhenInvalidDateButLogAsError() {
        var typeDcValue = toDcType("Others");
        var date = new DcValue(Element.DATE, Qualifier.ISSUED, "222");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, date));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), containsString(Error.INVALID_DC_DATE_ISSUED.toString()));
    }

    @Test
    void shouldMapToFirstMappableTypeWhenManyTypesAreUnmappable() {
        var type1DcValue = toDcType("Others");
        var type2DcValue = toDcType("Conference object");
        var type3DcValue = toDcType("Book");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(type1DcValue, type2DcValue, type3DcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getType().getNva(), is(not(nullValue())));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_UNMAPPABLE_TYPES.toString())));
        assertThat(appender.getMessages(), not(containsString(INVALID_DC_TYPE.toString())));
    }

    @Test
    void shouldLogWarningWhenConferenceObjectOrLecture() {
        var typeDcValue = toDcType("Conference object");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(),
                   containsString(Warning.CONFERENCE_OBJECT_OR_LECTURE_WILL_BE_MAPPED_TO_CONFERENCE_REPORT.toString()));
    }

    @Test
    void shouldScrapeAccessCode() {
        var typeDcValue = toDcType("Conference object");
        var accessCodeValue =
            "KLAUSULERING: Dokumentet er klausulert grunnet lovpålagt taushetsplikt. Tilgangskode/Access code C";
        var accessCode = new DcValue(Element.RIGHTS, Qualifier.TERMS, accessCodeValue);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, accessCode));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var expectedAccessCode = "Dokumentet er klausulert grunnet lovpålagt taushetsplikt";
        assertThat(record.getAccessCode(), is(equalTo(expectedAccessCode)));
    }

    @Test
    void shouldLoggInvalidDoi() {
        var dcType = toDcType("Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, "0.1016/S0140-6736wefwfg.(20)30045-#%wt3");
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dcScraper.validateAndParseDublinCore(
            dublinCoreWithDoi, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(appender.getMessages(), containsString(INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK.toString()));
    }

    @Test
    void shouldNotLoggInvalidDoiWhenDoiIsFixedDuringScraping() {
        var doi = "doi:10.1007/s12062-016-9157-z";
        var dcType = toDcType("Book");
        var dcDoi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, doi);
        var dublinCoreWithDoi = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcDoi));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(
            dublinCoreWithDoi, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getDoi().toString(), is(equalTo("https://doi.org/10.1007/s12062-016-9157-z")));
        assertThat(appender.getMessages(), not(containsString(INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK.toString())));
    }

    @Test
    void shouldSetPublisherAuthorityToAcceptedVersionWhenVersionIsAcceptedVersion() {
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "acceptedVersion");
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, versionDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getPublisherAuthority().getNva(), is(PublisherVersion.ACCEPTED_VERSION));
    }

    @Test
    void shouldLogUnknownVersionsAndApplyNullValue() {
        var versionDcValue = new DcValue(Element.DESCRIPTION, Qualifier.VERSION, "submittedVersion");
        var typeDcValue = toDcType("Others");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, versionDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getPublisherAuthority().getNva(), is(nullValue()));
    }

    @Test
    void shouldScrapeIdFromChannelRegisterWhenMapsToScientificArticle() {
        var dcType1 = toDcType("Journal article");
        var dcType2 = toDcType("Peer reviewed");
        var issnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1664-0640");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType1, dcType2, issnDcValue));
        var brageLocation = new BrageLocation(null);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        String pidFromChannelRegister = "80B8F122-9C53-4BB1-88EC-3F14BD361E52";
        var journalId = record.getPublication().getPublicationContext().getJournal().getPid();
        assertThat(journalId, is(equalTo(pidFromChannelRegister)));
    }

    @Test
    void shouldScrapeIdFromPublishersInChannelRegisterWhenReportFromNVE() {
        var expectedPublisherPid = "E0964486-164E-4ADE-AB57-23F9780B7EF1";
        var dcType = toDcType("Report");
        var dcPublisher = new DcValue(Element.PUBLISHER, null, "NVE");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var brageLocation = new BrageLocation(null);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        var publisherId = record.getPublication().getPublicationContext().getPublisher().getPid();
        assertThat(publisherId, is(equalTo(expectedPublisherPid)));
    }

    @Test
    void shouldScrapeIdFromPublishersInChannelRegisterWhenReportFromKrus() {
        var expectedPublisherPid = "AB2ED6B5-10A9-496B-A21C-DD695C44C846";
        var dcType = toDcType("Report");
        var dcPublisher = new DcValue(Element.PUBLISHER, null, "KRUS");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var brageLocation = new BrageLocation(null);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        var publisherId = record.getPublication().getPublicationContext().getPublisher().getPid();
        assertThat(publisherId, is(equalTo(expectedPublisherPid)));
    }

    @Test
    void shouldScrapeIdFromPublishersInChannelRegisterWhenReportFromFHS() {
        var expectedPublisherPid = "B620D836-BB3A-4480-9E0B-794B3E84A2C7";
        var dcType = toDcType("Report");
        var dcPublisher = new DcValue(Element.PUBLISHER, null, "Orkana Forlag");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var brageLocation = new BrageLocation(null);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        var publisherId = record.getPublication().getPublicationContext().getPublisher().getPid();
        assertThat(publisherId, is(equalTo(expectedPublisherPid)));
    }

    @Test
    void shouldScrapeIdFromPublishersAndJournalsWhenReportAndPartOfSeries() {
        var expectedPublisherPid = "AB2ED6B5-10A9-496B-A21C-DD695C44C846";
        var expectedSeriesPid = "7907D9CB-E44D-4CC0-9F1E-F67595F67AFE";
        var dcType = toDcType("Report");
        var dcPublisher = new DcValue(Element.PUBLISHER, null, "KRUS");
        var dcIssn = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1501-2832");
        var dcPartOfSeries = partOfSeries("NVE Rapport;2022:13");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(dcType, dcPublisher, dcIssn, dcPartOfSeries));
        var brageLocation = new BrageLocation(null);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        var publisherId = record.getPublication().getPublicationContext().getPublisher().getPid();
        var seriesId = record.getPublication().getPublicationContext().getSeries().getPid();

        assertThat(publisherId, is(equalTo(expectedPublisherPid)));
        assertThat(seriesId, is(equalTo(expectedSeriesPid)));
    }

    @Test
    void shouldApplyFirstIssueValueIfManyAndLog() {
        var type = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        var issue1 = new DcValue(Element.SOURCE, Qualifier.ISSUE, "21");
        var issue2 = new DcValue(Element.SOURCE, Qualifier.ISSUE, "21/2017");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, issue1, issue2));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(appender.getMessages(), containsString(String.valueOf(MULTIPLE_VALUES)));
    }

    @Test
    void shouldApplyFirstCristinValueIfManyAndLog() {
        var type = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        String expectedCristinId = "12498235";
        var cristin1 = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN, "someValue");
        var cristin2 = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN, expectedCristinId);
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, cristin1, cristin2));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(appender.getMessages(), containsString(String.valueOf(MULTIPLE_VALUES)));
        assertThat(record.getCristinId(), is(equalTo(expectedCristinId)));
    }

    @Test
    void shouldLogWhenMultipleSearchResultsInChannelRegister() {
        var type = toDcType("Journal article");
        var journal = new DcValue(Element.SOURCE, Qualifier.JOURNAL, "Earth System Science Data");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, journal));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(appender.getMessages(), containsString(String.valueOf(DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER)));
    }

    @Test
    void shouldRemoveContributorsWithoutNameFromContributors() {
        var type = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        var contributor1 = new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, "");
        var contributor2 = new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, null);
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, contributor1, contributor2));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getEntityDescription().getContributors(), is(empty()));
    }

    @Test
    void shouldMapTypesContainingMultipleLanguagesTypesAndPeerReviewedToValidType() {
        var type0 = toDcType("Peer reviewed");
        var type1 = toDcType("Tidsskriftartikkel");
        var type2 = toDcType("Journal article");
        var type3 = new DcValue(Element.TYPE, Qualifier.VERSION, "publishedVersion");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type0, type1, type2, type3));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getType().getNva(), is(equalTo(NvaType.SCIENTIFIC_ARTICLE.getValue())));
    }

    @Test
    void shouldExtractCristinIdContainingFridaIdIdentifier() {
        var type = toDcType("Tidsskriftartikkel");
        var cristinId = new DcValue(Element.IDENTIFIER, Qualifier.CRISTIN_ID_MUNIN, "FRIDAID 932785");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(cristinId, type));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getCristinId(), is(equalTo("932785")));
    }

    @Test
    void shouldNotSwitchNamesWhenComaIsTheLastCharInTheString() {
        var typeDcValue = toDcType("Others");
        var author = new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, "Audun Vognild,");
        var pageNumber = new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "245|-257");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, author, pageNumber));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        var expectedName = "Audun Vognild";
        var actualName = record.getEntityDescription().getContributors().iterator().next().getIdentity().getName();
        assertThat(expectedName, is(equalTo(actualName)));
    }

    @Test
    void shouldMapHardcodedValues() {
        var typeDcValue = toDcType("Others");
        var issn = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "1502-8190");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, issn));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        var expectedIssn = "1502-8143";
        var actualIssn = record.getPublication().getIssnSet().iterator().next();
        assertThat(expectedIssn, is(equalTo(actualIssn)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://doi.org/10.5194/tc-8-1885-2014", "doi:10.5194/tc-8-1885-2014",
        "10.5194/tc-8-1885-2014", "doi.org/10.5194/tc-8-1885-2014"})
    void shouldDetectDifferencesBetweenDoiAndLinkAndMapDoiCorrectly(String value) {
        var expectedDoi = URI.create("https://doi.org/10.5194/tc-8-1885-2014");
        var doi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, value);
        var brageLocation = new BrageLocation(null);
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, doi));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getDoi(), is(equalTo(expectedDoi)));
        assertThat(record.getLink(), is(nullValue()));
    }

    @ParameterizedTest
    @MethodSource("doiProvider")
    void shouldCreateDoi(String value, String expectedValue) {
        var doi = new DcValue(Element.IDENTIFIER, Qualifier.DOI, value);
        var brageLocation = new BrageLocation(null);
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, doi));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);

        assertThat(record.getDoi().toString(), is(equalTo(expectedValue)));
    }

    @Test
    void shouldDetectDifferencesBetweenDoiAndLinkAndMapLinkCorrectly() {
        var appender = LogUtils.getTestingAppender(DublinCoreScraper.class);
        var link = new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://www.hindawi.com/journals/nrp/2012/690348/");
        var brageLocation = new BrageLocation(null);
        var typeDcValue = toDcType("Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, link));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(appender.getMessages(), not(containsString(INVALID_DC_IDENTIFIER_DOI_OFFLINE_CHECK.toString())));
        assertThat(record.getLink(), is(equalTo(URI.create(link.getValue()))));
        assertThat(record.getDoi(), is(nullValue()));
    }

    @Test
    void shouldNotLogDcTypeErrorWhenMultipleTypesCanBeMapped() {
        var type1 = new DcValue(Element.TYPE, null, "Journal article");
        var type2 = toDcType("Article");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type1, type2));

        var appender = LogUtils.getTestingAppender(DublinCoreScraper.class);
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var messages = appender.getMessages();
        assertThat(messages, not(containsString(MULTIPLE_UNMAPPABLE_TYPES.name())));
        assertThat(messages, not(containsString(INVALID_DC_TYPE.name())));
        assertThat(messages, not(containsString(INVALID_DC_TYPE.name())));
    }

    @Test
    void shouldConvertLocalCodeToSubjectAndDoNotConvertLocalCodesThatAreNotUri() {
        var localCodeUri = new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, randomUri().toString());
        var localCodeString = new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, randomString());
        var brageLocation = new BrageLocation(null);
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(typeDcValue, localCodeString, localCodeUri));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);

        assertThat(record.getSubjects(), contains(URI.create(localCodeUri.getValue())));
        assertThat(record.getSubjects(), hasSize(1));
    }

    @Test
    void shouldPrettifyTypeWhenConvertingToRecord() {
        var type = "\"Conference report\"";
        var brageLocation = new BrageLocation(null);
        var typeDcValue = new DcValue(Element.TYPE, null, type);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);

        assertThat(record.getType().getNva(), is(equalTo(NvaType.CONFERENCE_REPORT.getValue())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"9788293091172(PDF)", "9788293091172(trykt)", "ISBN9788293091172"})
    void shouldRemoveAllSpecialCharactersAndLettersFromIsbn(String isbn) {
        var typeDcValue = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        var isbnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISBN, isbn);
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, isbnDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getPublication().getIsbnSet().iterator().next(), is(equalTo("9788293091172")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1502-007x", "1502-007x (online)", "1502007x", "1502-007x", "1502-007x (e-utg)",
        "1502-007x (e-utg), 0048-5829 (trykt utg)"})
    void shouldRemoveAllLettersAndInvalidSpecialCharactersFromIssn(String isbn) {
        var typeDcValue = new DcValue(Element.TYPE, Qualifier.NONE, "Journal article");
        var isbnDcValue = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, isbn);
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, isbnDcValue));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        assertThat(record.getPublication().getIssnSet().iterator().next(), is(equalTo("1502-007X")));
    }

    @ParameterizedTest
    @MethodSource("provideDcValueAndExpectedPages")
    void shouldExtractPagesWithDifferentFormats(DcValue pageNumber, Pages expectedPages) {
        var typeDcValue = toDcType("Journal Article");
        var peerReviewed = toDcType("Peer reviewed");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(peerReviewed, typeDcValue, pageNumber));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var actualPages = record.getEntityDescription().getPublicationInstance().getPages();
        assertThat(actualPages, is(equalTo(expectedPages)));
        assertThat(appender.getMessages(), not(containsString(PAGE_NUMBER_FORMAT_NOT_RECOGNIZED.toString())));
    }

    @Test
    void shouldLookupInChannelRegisterAliasesForJournals() {
        var expectedJournalPid = "B5CABA4E-8C50-42E8-81BE-0C85D8CC00B8";
        var typeDcValue = toDcType("Journal article");
        var journalName = "Dronning Mauds Minne Høgskole";
        var journal = new DcValue(Element.SOURCE, Qualifier.JOURNAL, journalName);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(typeDcValue, journal));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var journalPid = record.getPublication().getPublicationContext().getJournal().getPid();
        assertThat(journalPid, is(equalTo(expectedJournalPid)));
        assertThat(appender.getMessages(), not(containsString(journalName)));
    }

    @Test
    void shouldLookupInChannelRegisterAliasesForPublisher() {
        var expectedPublisherPid = "A2096FDB-2B23-4AF8-8E32-ACEF53E0D3EE";
        var dcType = toDcType("Report");
        var publisherName = "uis";
        var dcPublisher = new DcValue(Element.PUBLISHER, null, publisherName);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var publisherPid = record.getPublication().getPublicationContext().getPublisher().getPid();
        assertThat(publisherPid, is(equalTo(expectedPublisherPid)));
        assertThat(appender.getMessages(), not(containsString(publisherName)));
    }

    @Test
    void shouldLookupInChannelRegisterAliasesForNtnuPublishers() {
        var expectedPublisherPid = "D61B0D47-C78A-48DC-8537-3AD87DEF4D5B";
        var dcType = toDcType("Report");
        var publisherName = "Institutt for teknisk kybernetikk";
        var dcPublisher = new DcValue(Element.PUBLISHER, null, publisherName);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");
        var publisherPid = record.getPublication().getPublicationContext().getPublisher().getPid();
        assertThat(publisherPid, is(equalTo(expectedPublisherPid)));
        assertThat(appender.getMessages(), not(containsString(publisherName)));
    }

    @Test
    void shouldLookupInChannelRegisterAliasesForUibPublishers() {
        var expectedPublisherPid = "CBCE38D7-C6C6-4CE9-BCED-D64610033E9B";
        var dcType = toDcType("Report");
        var publisherName = "Universitetet i Bergen, HF fakultetet";
        var dcPublisher = new DcValue(Element.PUBLISHER, null, publisherName);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "bora");
        var publisherPid = record.getPublication().getPublicationContext().getPublisher().getPid();
        assertThat(publisherPid, is(equalTo(expectedPublisherPid)));
        assertThat(appender.getMessages(), not(containsString(publisherName)));
    }

    @Test
    void shouldLookupInChannelRegisterAliasesForNmbuPublishers() {
        var expectedPublisherPid = "8B9EC1FA-3C4A-4875-AA0A-C1E0219C840E";
        var dcType = toDcType("Report");
        var publisherName = "Norwegian University og Life Sciences, Ås";
        var dcPublisher = new DcValue(Element.PUBLISHER, null, publisherName);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "nmbu");
        var publisherPid = record.getPublication().getPublicationContext().getPublisher().getPid();
        assertThat(publisherPid, is(equalTo(expectedPublisherPid)));
        assertThat(appender.getMessages(), not(containsString(publisherName)));
    }

    @Test
    void shouldLookupInChannelRegisterWhenPublisherTitleContainsSpecialCharacters() {
        var expectedPublisherPid = "8B9EC1FA-3C4A-4875-AA0A-C1E0219C840E";
        var dcType = toDcType("Report");
        var publisherName = "\nNorwegian \bUniversity og Life\u200b Sciences, Ås\t";
        var dcPublisher = new DcValue(Element.PUBLISHER, null, publisherName);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "nmbu");
        var publisherPid = record.getPublication().getPublicationContext().getPublisher().getPid();
        assertThat(publisherPid, is(equalTo(expectedPublisherPid)));
        assertThat(appender.getMessages(), not(containsString(publisherName)));
    }

    @Test
    void shouldLogPublisherThatDoesNotExistInChannelRegistry() {
        var dcType = toDcType("Report");
        var publisherName = "Gurba that is not in any channel registry csv's";
        var dcPublisher = new DcValue(Element.PUBLISHER, null, publisherName);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcPublisher));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");
        assertThat(appender.getMessages(), containsString(publisherName));
        assertThat(appender.getMessages(), containsString(DC_PUBLISHER_NOT_IN_CHANNEL_REGISTER.toString()));
    }

    @Test
    void shouldSetTypeToResearchReportAndDoNotLogWhenPostIsMissingTypePropertyWhenCustomerHasAgreedToMapTypelessPostAsReport() {
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ffi");

        assertThat(appender.getMessages(), not(containsString(INVALID_DC_TYPE.toString())));
        assertThat(record.getType().getNva(), is(equalTo(NvaType.RESEARCH_REPORT.getValue())));
    }

    @Test
    void shouldConvertNvaTypeToNvaTypeAndDoNotCreateError() {
        var dcType = toDcType("Other report");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");

        assertThat(record.getType().getNva(), is(equalTo(NvaType.REPORT.getValue())));
        assertThat(appender.getMessages(), not(containsString(INVALID_DC_TYPE.toString())));
    }

    @Test
    void shouldBeAbleToConsumeRandomizedIssn() {
        var dcType = toDcType("Other report");
        var dsIssn = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "ISBN");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dsIssn));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");

        assertThat(record.getErrors().toString(), containsString(INVALID_ISSN.name()));
    }

    @Test
    void shouldScrapeIsmnFieldI() {
        var dcType = toDcType("Musical score");
        var ismn = new DcValue(Element.IDENTIFIER, Qualifier.ISMN, "979-0-9005146-0-8");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, ismn));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");
        assertThat(record.getType().getNva(), is(equalTo(NvaType.RECORDING_MUSICAL.getValue())));

    }

    @Test
    void shouldExtractIssnFromDcSourceIssn() {
        var dcType = toDcType("Musical score");
        var value = randomIssn();
        var issn = new DcValue(Element.SOURCE, Qualifier.ISSN, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, issn));

        assertThat(DublinCoreScraper.extractIssn(dublinCore), is(equalTo(Set.of(issn.getValue()))));
    }

    @Test
    void shouldMapDoctoralThesisInNorwegianToDoctoralThesis() {
        var dcType = toDcType("Doktoravhandling");
        var dcType2 = toDcType("Doctoral dissertation");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, dcType2));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");

        assertThat(record.getType().getNva(), is(equalTo(NvaType.DOCTORAL_THESIS.getValue())));
        assertThat(appender.getMessages(), not(containsString(INVALID_DC_TYPE.name())));
    }

    @Test
    void shouldMapSecondMainTitleToAlternativeTitleWhenTwoMainTitlesAndDoNotLogMultipleValuesError() {
        var dcType = toDcType("Doktoravhandling");
        var mainTitle = new DcValue(Element.TITLE, Qualifier.NONE, "mainTitle1");
        var secondTitle = new DcValue(Element.TITLE, Qualifier.NONE, "mainTitle2");

        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcType, mainTitle, secondTitle));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");

        assertThat(record.getEntityDescription().getMainTitle(), is(not(nullValue())));
        assertThat(record.getEntityDescription().getAlternativeTitles(), hasSize(1));
        assertThat(appender.getMessages(), not(containsString(MULTIPLE_VALUES.name())));
    }

    @Test
    void shouldExtractJournalFromJournalTitleDcValue() {
        var journalValue = randomString();
        var journalTitle = new DcValue(Element.IDENTIFIER, Qualifier.JOURNAL_TITLE, journalValue);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(journalTitle));

        assertThat(DublinCoreScraper.extractJournal(dublinCore, randomString()), is(equalTo(journalValue)));
    }

    @Test
    void shouldNotLogLicenseErrorWhenRightsStatementsLicenseHasBeenMappedToDefaultLicense() {
        var uri = URI.create("http://rightsstatements.org/page/InC/1.0/");
        var license = new DcValue(Element.RIGHTS, Qualifier.URI, uri.toString());
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(license));
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");
        var actualLicense = new LicenseScraper(dublinCore).generateLicense();

        assertThat(appender.getMessages(), not(containsString(INVALID_DC_RIGHTS_URI.toString())));
        assertThat(actualLicense.getNvaLicense().getLicense(), is(equalTo(DEFAULT_LICENSE)));
    }

    @Test
    void channelRegisterLookUpShouldNotBeCaseSensitive() {
        var type = toDcType("Journal article");
        var journal = new DcValue(Element.SOURCE, Qualifier.JOURNAL, "PlAnT eCoLoGy");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, journal));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, SOME_CUSTOMER);
        var expectedPid = "9DD7452E-5FF9-48BA-BF09-6648C103CDFD";
        assertThat(record.getPublication().getPublicationContext().getJournal().getPid(), is(equalTo(expectedPid)));
    }

    @Test
    void shouldReadPublisherPidForInstitutionIssuingDegree(){
        var type = toDcType("Bachelor thesis");
        var publisher = new DcValue(Element.PUBLISHER, null, "gurba");
        var brageLocation = new BrageLocation(null);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, publisher));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, brageLocation, "ntnu");
        var expectedPid = "D61B0D47-C78A-48DC-8537-3AD87DEF4D5B";
        assertThat(record.getPublication().getPublicationContext().getPublisher().getPid(), is(equalTo(expectedPid)));
    }

    @Test
    void shouldExtractEmbargoWhenCustomerIsUio() {
        var embargoDate = "2024-08-26";
        var dcValue = new DcValue(Element.DATE, Qualifier.fromValue("embargoEndDate"), embargoDate);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcValue));
        var embargo = DublinCoreScraper.extractEmbargo(dublinCore, CustomerMapper.UIO);
        assertThat(embargo, is(equalTo(embargoDate)));
    }

    @Test
    void shouldNotExtractEmbargoWhenCustomerIsNotUio() {
        var embargoDate = "2024-08-26";
        var dcValue = new DcValue(Element.DATE, Qualifier.fromValue("embargoEndDate"), embargoDate);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcValue));
        var embargo = DublinCoreScraper.extractEmbargo(dublinCore, CustomerMapper.NTNU);
        assertThat(embargo, is(nullValue()));
    }

    @Test
    void shouldPickMostRecentEmbargoDate(){
        var invalidEmbargoDate = "";
        var embargoDate = "2024-08-26";
        var embargoDateMostRecent = "2025-08-26";
        var dcValueInvalidEmbargoDate = new DcValue(Element.DATE, Qualifier.EMBARGO_DATE, invalidEmbargoDate);
        var dcValueEmbargoDate = new DcValue(Element.DATE, Qualifier.EMBARGO_DATE, embargoDate);
        var dvValueEmbargoMostRecent = new DcValue(Element.DATE, Qualifier.EMBARGO_DATE, embargoDateMostRecent);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(dcValueInvalidEmbargoDate,
                                                                                dcValueEmbargoDate,
                                                                                dvValueEmbargoMostRecent));
        var embargo = DublinCoreScraper.extractEmbargo(dublinCore, "uio");
        assertThat(embargo, is(equalTo(embargoDateMostRecent)));
    }

    @Test
    void shouldReturnInvalidLicenseErrorWhenFailingToExtractLicense() {
        var licenseValue = "http://creativecommons.org/licenses/by/4.0\"";
        var invalidLicense = new DcValue(Element.RIGHTS, Qualifier.URI, licenseValue);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(invalidLicense));
        dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");

        assertThat(appender.getMessages(), containsString(Error.INVALID_DC_RIGHTS_URI.name()));
    }

    @Test
    void shouldExtractJournalFromCitationWhenCustomerIsUioAndMissingJournalIssnAndJournalTitleAndNotLogError() {
        var type = toDcType("Journal article");
        var citationContainingJournalName = new DcValue(Element.IDENTIFIER, Qualifier.CITATION,
                                                        "EGU Geodynamics Blog." + randomString());
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, citationContainingJournalName));
        var appender = LogUtils.getTestingAppenderForRootLogger();
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "uio");

        assertThat(appender.getMessages(), not(containsString(Error.MISSING_DC_ISSN_AND_DC_JOURNAL.toString())));
        assertThat(record.getPublication().getJournal(), is(notNullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"EC/H2020/727610", "EC/H2020: 727610"})
    void shouldScrapeProject() {
        var type = toDcType("Journal article");
        var citationContainingJournalName = new DcValue(Element.RELATION, Qualifier.PROJECT, "EC/H2020/727610");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, citationContainingJournalName));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "uit");
        var project = record.getProjects().iterator().next();
        assertThat(project.getIdentifier(), is(equalTo("727610")));
        assertThat(project.getName(), is(equalTo("EC/H2020")));
    }

    @Test
    void shouldScrapeProjectSeparatedBySlashAndColon() {
        var type = toDcType("Journal article");
        var citationContainingJournalName = new DcValue(Element.RELATION, Qualifier.PROJECT, "Klima- og miljødepartementet: 22/3615-4");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, citationContainingJournalName));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ntnu");
        var project = record.getProjects().iterator().next();
        assertThat(project.getIdentifier(), is(equalTo("22/3615-4")));
        assertThat(project.getName(), is(equalTo("Klima- og miljødepartementet")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/727610", "EC/H2020: ", "12345"})
    void shouldNotCreateProjectWhenProjectIsMissingANameOrdIdentifier(String value) {
        var type = toDcType("Journal article");
        var citationContainingJournalName = new DcValue(Element.RELATION, Qualifier.PROJECT, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, citationContainingJournalName));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "sintef");
        var projects = record.getProjects();
        assertThat(projects, is(emptyIterable()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotFailWhenProjectCanNotBeConverted(String value) {
        var type = toDcType("Journal article");
        var citationContainingJournalName = new DcValue(Element.RELATION, Qualifier.PROJECT, value);
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, citationContainingJournalName));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "sintef");
        var projects = record.getProjects();
        assertThat(projects, is(emptyIterable()));
    }

    @Test
    void shouldNotFailWhenScrapingInvalidProjectForSintef() {
        var type = toDcType("Journal article");
        var citationContainingJournalName = new DcValue(Element.RELATION, Qualifier.PROJECT, randomString());
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, citationContainingJournalName));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "uio");
       assertThat(record.getProjects(), is(emptyIterable()));
    }

    @ParameterizedTest
    @MethodSource("isPartOfSeriesProvider")
    void shouldMapDifferentVersionsOfPartOfSeriesToSinglePartOfSeriesObject(List<DcValue> dcValues,
                                                                            PartOfSeries expected) {
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "sintef");
        var partOfSeries = record.getPublication().getPartOfSeries();
        assertThat(partOfSeries, is(equalTo(expected)));
    }

    @Test
    void shouldSetPrioritizePublisherWhenTheDublinCoreIsDegreeAndCustomerIssuesDegrees(){
        var dcValues = List.of(
            new DcValue(Element.TYPE, null, BrageType.MASTER_THESIS.getValue()),
            new DcValue(Element.PUBLISHER, null, randomString())
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var customerIssuingDegrees = "ntnu";
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), customerIssuingDegrees);
        var actualPrioritizedProperties = record.getPrioritizedProperties();
        assertThat(actualPrioritizedProperties, hasItem(PUBLISHER.getValue()));
        assertThat(record.getPublication().getPublicationContext().getPublisher().getPid(), is(notNullValue()));
    }

    @Test
    void shouldNotPrioritizePublisherWhenCustomerIsNotIssuingDegrees(){
        var dcValues = List.of(
            new DcValue(Element.TYPE, null, BrageType.MASTER_THESIS.getValue()),
            new DcValue(Element.PUBLISHER, null, randomString())
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var customerNotIssuingDegrees = "nve";
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), customerNotIssuingDegrees);
        var actualPrioritizedProperties = record.getPrioritizedProperties();
        assertThat(actualPrioritizedProperties, not(hasItem(PUBLISHER.getValue())));
    }

    @Test
    void shouldPrioritizeCertainMetadataFieldsWhenDublinCoreIsDegreeAndCustomerIssuesDegrees(){
        var dcValues = List.of(
            new DcValue(Element.TYPE, null, BrageType.MASTER_THESIS.getValue()));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var customerIssuingDegrees = "ntnu";
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), customerIssuingDegrees);
        var actualPrioritizedProperties = record.getPrioritizedProperties();
        assertThat(actualPrioritizedProperties, hasItem(MAIN_TITLE.getValue()));
        assertThat(actualPrioritizedProperties, hasItem(ALTERNATIVE_TITLES.getValue()));
        assertThat(actualPrioritizedProperties, hasItem(ABSTRACT.getValue()));
        assertThat(actualPrioritizedProperties, hasItem(ALTERNATIVE_ABSTRACTS.getValue()));
        assertThat(actualPrioritizedProperties, hasItem(FUNDINGS.getValue()));
        assertThat(actualPrioritizedProperties, hasItem(REFERENCE.getValue()));
        assertThat(actualPrioritizedProperties, hasItem(TAGS.getValue()));
    }

    @ParameterizedTest
    @EnumSource(value = BrageType.class, mode = Mode.INCLUDE, names =  {"REPORT", "RESEARCH_REPORT", "OTHER_TYPE_OF_REPORT"})
    void shouldMapProvidedReportTypesFromBrageAsResearchReportWhenCustomerIsFFI(BrageType brageType) {
        var dcValues = List.of(new DcValue(Element.TYPE, null, brageType.getValue()));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ffi");

        assertThat(record.getType().getNva(), equalTo(NvaType.RESEARCH_REPORT.getValue()));
    }

    @Test
    void shouldCreateRecordWithUndefinedLanguageWhenLanguageIsUndefined() {
        var dcValues = List.of(
            new DcValue(Element.TYPE, null, BrageType.MASTER_THESIS.getValue()),
            new DcValue(Element.LANGUAGE, null, randomString()));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var customerIssuingDegrees = "ntnu";
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), customerIssuingDegrees);

        assertThat(record.getEntityDescription().getLanguage().getNva(),
                   is(equalTo(LEXVO_URI_UNDEFINED)));
    }

    @Test
    void shouldScrapeMainTitleWithPresentValue() {
        var title = randomString();
        var dcValues = List.of(
            new DcValue(Element.TYPE, null, BrageType.MASTER_THESIS.getValue()),
            new DcValue(Element.TITLE, Qualifier.NONE, ""),
            new DcValue(Element.TITLE, Qualifier.NONE, null),
            new DcValue(Element.TITLE, Qualifier.NONE, title));
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var customerIssuingDegrees = "ntnu";
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), customerIssuingDegrees);

        assertThat(record.getEntityDescription().getMainTitle(), is(equalTo(title)));
    }

    @Test
    void shouldScrapeSamiLanguageCodeSmiToNorthernSami(){
        var dcValues = List.of(
            new DcValue(Element.LANGUAGE, Qualifier.ISO, "smi"),
            toDcType("Journal article")
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        assertThat(record.getEntityDescription().getLanguage().getNva().toString(), containsString("sme"));
    }

    @Test
    void shouldLookUpProjectInFundingsSources(){
        var dcValues = List.of(
            toDcType("Journal article"),
            new DcValue(Element.LANGUAGE, Qualifier.ISO, "smi"),
            new DcValue(Element.RELATION, Qualifier.PROJECT, "Norges forskningsråd: 237906")
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var project = record.getProjects().iterator().next();

        assertThat(project.getFundingSource().getIdentifier(), is(equalTo("NFR")));
    }

    @Test
    void shouldNotCreateContributorFromDcQualifierDepartment(){
        var dcValues = List.of(
            toDcType("Journal article"),
            new DcValue(Element.CONTRIBUTOR, Qualifier.DEPARTMENT, randomString())
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        assertThat(record.getEntityDescription().getContributors(), is(emptyIterable()));
    }

    @Test
    void shouldCreateProjectWithoutFundingsSourcesWhenProjectIsNotPresentInFundingSourcesAndLogError(){
        var dcValues = List.of(
            toDcType("Journal article"),
            new DcValue(Element.LANGUAGE, Qualifier.ISO, "smi"),
            new DcValue(Element.RELATION, Qualifier.PROJECT, "EU/Horizon 2020, No 679266 (GRACE)")
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var project = record.getProjects().iterator().next();

        assertThat(project.getFundingSource(), is(nullValue()));
        assertThat(record.getWarnings().toString(), containsString(Warning.UNKNOWN_PROJECT.name()));
    }

    @Test
    void shouldScrapeWiseflowAndInsperaIdentifiers(){
        var insperaIdentifier = "no.ntnu:inspera:22222:22222";
        var wiseFlowIdentifier = "no.usn:wiseflow:11111:11111";
        var dcValues = List.of(
            toDcType("Journal article"),
            new DcValue(Element.IDENTIFIER, null, insperaIdentifier),
            new DcValue(Element.IDENTIFIER, null, wiseFlowIdentifier),
            new DcValue(Element.IDENTIFIER, Qualifier.DOI, "https://doi.org/10.1016/j.scitotenv.2021.151958"),
            new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "2038-324X"),
            new DcValue(Element.IDENTIFIER, Qualifier.CITATION, randomString())
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        assertThat(record.getInsperaIdentifier(), is(equalTo(insperaIdentifier)));
        assertThat(record.getWiseflowIdentifier(), is(equalTo(wiseFlowIdentifier)));
    }

    @DisplayName("When brage record has type Book, Textbook or Book of abstract, and " +
                 "publication has partOfSeries field which is present, looking up for series name" +
                 "in channel register csv file and if we get a match we are setting series pid in publication context")
    @ParameterizedTest
    @ValueSource(strings = {"Book", "Textbook", "Book of abstracts"})
    void shouldLookUpSeriesInChannelRegisterWhenPublicationHasPartOfSeriesWithPartOfSeries(String type){
        var dcValues = List.of(
            toDcType(type),
            new DcValue(Element.RELATION, Qualifier.IS_PART_OF_SERIES, "The Bryggen Papers;9")
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        assertThat(record.getPublication().getPublicationContext().getSeries().getPid(),
                   is(equalTo("5FCE7321-320B-4EB6-B890-A2AD85B1BFC1")));
    }

    @ParameterizedTest
    @MethodSource("seriesSupplier")
    void shouldConvertPartOfAndPartOfSeriesFieldsToPartOfSeriesAndLookUpForSeriesInChannelRegister(String firstValue,
                                                                                                   String secondValue) {
        var seriesName = "Cicero Working papers";
        var seriesNumber = "2010:03";
        var dcValues = List.of(
            toDcType("Working paper"),
            new DcValue(Element.RELATION, Qualifier.IS_PART_OF, firstValue),
            new DcValue(Element.RELATION, Qualifier.IS_PART_OF_SERIES, secondValue)
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var expectedPartOfSeries = new PartOfSeries(seriesName, seriesNumber);

        assertNotNull(record.getPublication().getPublicationContext().getSeries().getPid());
        assertTrue(record.getPublication().getPartOfSeries().equals(expectedPartOfSeries));
    }

    @Test
    void lookUpSeriesByIssnWhenPartOfSeriesIsPresentButNotInChannelRegisterAndIssnIsPresent() {
        var dcValues = List.of(
            toDcType("Working paper"),
            new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "0804-4511")
        );
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var expectedPid = "72896F98-72CB-47F9-89A8-922CB9F52FC2";

        assertEquals(expectedPid, record.getPublication().getPublicationContext().getSeries().getPid());
    }

    @Test
    void shouldSetFFIAsPublisherForAllFFIRecordsMissingPublisher() {
        var type = toDcType("Report");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ffi");

        var expectedPid = "39D2B1A7-091E-44C5-9579-65A75C6A6F01";

        assertEquals(expectedPid, record.getPublication().getPublicationContext().getPublisher().getPid());
    }

    @Test
    void shouldScrapeJournalFromRelationJournalField() {
        var type = toDcType("Journal article");
        var relationJournal = new DcValue(Element.RELATION, Qualifier.JOURNAL, "Underwater Acoustics Conference & Exhibition (UACE)");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, relationJournal));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ffi");

        var expectedPid = "9E7821F8-B0B5-4686-B8E3-6F13F96D84C0";


        assertEquals(expectedPid, record.getPublication().getPublicationContext().getJournal().getPid());
    }

    @Test
    void shouldLookUpJournalIssueInChannelRegister() {
        var type = toDcType("Viten");
        var issn = new DcValue(Element.IDENTIFIER, Qualifier.ISSN, "2535-2687");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, issn));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ffi");

        var expectedPid = "E91978F6-BB4F-44CC-B8A6-9A02E69B4A3C";


        assertEquals(expectedPid, record.getPublication().getPublicationContext().getJournal().getPid());
    }

    @Test
    void shouldMapReportToResearchReportWhenCustomerIsFFI() {
        var type = toDcType("Rapport");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ffi");


        assertEquals(NvaType.RESEARCH_REPORT.getValue(), record.getType().getNva());
    }

    @Test
    void shouldSetContributorsAffiliationToFfiWhenReportAndPublisherIsFfi() {
        var type = toDcType("Rapport");
        var contributor = new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, "Some, Person");
        var subject = new DcValue(Element.SUBJECT, null, "tag");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(type, contributor, subject));
        var record = dcScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), "ffi");

        var affiliation = record.getEntityDescription().getContributors().get(0).getAffiliations().iterator().next();
        var tag = record.getEntityDescription().getTags();
        assertEquals("7428.0.0.0", affiliation.getIdentifier());
    }

    private static DcValue toDcType(String t) {
        return new DcValue(Element.TYPE, null, t);
    }

    private static Stream<Arguments> provideDcValueAndExpectedPages() {
        return Stream.of(
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "96"), new Pages("96", null, "96")),
            Arguments.of(new DcValue(Element.SOURCE, Qualifier.PAGE_NUMBER, "96 s."), new Pages("96 s.", null, "96")),
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

package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.record.PublisherVersion;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class EntityDescriptionExtractorTest {

    private static final String SOME_CUSTOMER = "nve";
    private static final boolean ONLINE_VALIDATION_DISABLED = false;

    private static final boolean LOOKUP_IN_CHANNEL_REGISTER = false;
    public static final String ORCID_VALUE = "0000-0000-0000-0000";
    public static final URI ORCID_URI = URI.create("https://orcid.org/0000-0000-0000-0000");

    public static Stream<Arguments> contributorAndOrcIdProvider() {
        return Stream.of(arguments(named("Multiple contributors and single orcId",
                                         List.of(randomContributor(), randomContributor(),
                                                 orcIdWithValue(randomString())))), arguments(
                             named("Multiple orcIds and single contributor",
                                   List.of(randomContributor(), orcIdWithValue(randomString()),
                                           orcIdWithValue(randomString())))),
                         arguments(named("Multiple contributors and multiple orcIds",
                                         List.of(randomContributor(), randomContributor(),
                                                 orcIdWithValue(randomString()), orcIdWithValue(randomString())))));
    }

    public static Stream<Arguments> orcIdProvider() {
        return Stream.of(arguments(named("OrcId Uri", "https://orcid.org/" + ORCID_VALUE)),
                         arguments(named("OrcId Uri", "http://orcid.org/" + ORCID_VALUE)),
                         arguments(named("OrcId String", ORCID_VALUE)),
                         arguments(named("OrcId String", "http://www.orcid.org/" + ORCID_VALUE)));
    }

    @Test
    void shouldMapLocalCodeToDescription() {
        var someLocalCode = "someLocalCode";
        var localCodeValue = new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, someLocalCode);
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(localCodeValue, typeDcValue));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var descriptions = record.getEntityDescription().getDescriptions();
        assertThat(descriptions, hasItem(someLocalCode));
    }

    @Test
    void shouldKeepOriginalOrderOfDescriptions() {
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(
            new File(TEST_RESOURCE_PATH + "dc_with_duplicated_values.xml"));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var descriptions = record.getEntityDescription().getDescriptions();
        assertThat(descriptions, is(equalTo(List.of("First description", "Second description"))));
    }

    @Test
    void shouldCreateContributorWithOrcidWhenBrageRecordHasSingleContributorAndSingleOrcId() {
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(randomContributor(), orcIdWithValue(ORCID_VALUE)));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var contributor = record.getEntityDescription().getContributors().get(0);

        assertThat(contributor.getIdentity().getOrcId(), is(equalTo(ORCID_URI)));
    }

    @ParameterizedTest()
    @MethodSource("contributorAndOrcIdProvider")
    void shouldCreateContributorWithoutOrcidWhenBrageRecordHasOrcId(List<DcValue> dcValues) {
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        record.getEntityDescription().getContributors().forEach(contributor -> {
            assertThat(contributor.getIdentity().getOrcId(), is(nullValue()));
        });
    }

    @ParameterizedTest
    @MethodSource("orcIdProvider")
    void shouldCreateOrcIdUriFromValue(String value) {
        var dublinCore = new DublinCore(List.of(new DcValue(Element.CONTRIBUTOR, Qualifier.ORCID, value)));
        var orcId = DublinCoreScraper.extractOrcIds(dublinCore).get(0);

        assertThat(orcId, is(equalTo(ORCID_URI)));
    }

    @Test
    void shouldNotThrowExceptionWhenUnableToParseOrcidToUri() {
        var dublinCore = new DublinCore(List.of(new DcValue(Element.CONTRIBUTOR, Qualifier.ORCID, "")));
        var orcIdList = DublinCoreScraper.extractOrcIds(dublinCore);

        assertThat(orcIdList, is(emptyIterable()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1967-1989", "1967–1989"})
    void shouldMepFirstYearInPeriodToPublicationDate(String date) {
        var dublinCore = new DublinCore(List.of(new DcValue(Element.DATE, Qualifier.ISSUED, date)));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var year = record.getEntityDescription().getPublicationDate().getNva().getYear();

        assertEquals("1967", year);
    }

    @Test
    void shouldMapNotSupportedPublisherVersionToDescription() {
        var publisherVersion = randomString();
        var dublinCore = new DublinCore(List.of(new DcValue(Element.DESCRIPTION, Qualifier.VERSION, publisherVersion)));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var descriptions = record.getEntityDescription().getDescriptions();

        assertTrue(descriptions.contains(publisherVersion));
    }

    @Test
    void shouldMapNotSupportedPublisherVersionFromDcTypeVersionToDescription() {
        var publisherVersion = randomString();
        var dublinCore = new DublinCore(List.of(new DcValue(Element.TYPE, Qualifier.VERSION, publisherVersion)));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var descriptions = record.getEntityDescription().getDescriptions();

        assertTrue(descriptions.contains(publisherVersion));
    }

    @ParameterizedTest
    @EnumSource(value = PublisherVersion.class)
    void shouldNotMapSupportedPublisherVersionToDescription(PublisherVersion publisherVersion) {
        var dublinCore = new DublinCore(List.of(new DcValue(Element.DESCRIPTION, Qualifier.VERSION,
                                                            publisherVersion.getValue())));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var descriptions = record.getEntityDescription().getDescriptions();

        assertFalse(descriptions.contains(publisherVersion.getValue()));
    }

    @Test
    void shouldRemoveNewLineWithWhitespaces() {
        var firstPart = randomString();
        var secondPart = randomString();
        var value = new DcValue(Element.DESCRIPTION, Qualifier.ABSTRACT, String.format("%s\r\n%s", firstPart, secondPart));
        var dublinCore = new DublinCore(List.of(value));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        var actualAbstract = record.getEntityDescription().getAbstracts().stream().findAny().orElseThrow();
        var expected = String.format("%s %s", firstPart, secondPart);

        assertEquals(expected, actualAbstract);
    }

    private static DcValue orcIdWithValue(String orcId) {
        return new DcValue(Element.CONTRIBUTOR, Qualifier.ORCID, orcId);
    }

    private static DcValue randomContributor() {
        return new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, randomString());
    }
}

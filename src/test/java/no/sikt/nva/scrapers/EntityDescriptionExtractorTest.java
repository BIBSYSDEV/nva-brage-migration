package no.sikt.nva.scrapers;

import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class EntityDescriptionExtractorTest {

    private static final String SOME_CUSTOMER = "nve";
    private static final boolean ONLINE_VALIDATION_DISABLED = false;

    private static final boolean LOOKUP_IN_CHANNEL_REGISTER = false;

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
        var orcId = randomString();
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(
            List.of(randomContributor(), orcIdWithValue(orcId)));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);
        var contributor = record.getEntityDescription().getContributors().get(0);

        assertThat(contributor.getIdentity().getOrcId(), is(equalTo(orcId)));
    }

    @ParameterizedTest()
    @MethodSource("contributorAndOrcIdProvider")
    void shouldCreateContributorWithoutOrcidWhenBrageRecordHas(List<DcValue> dcValues) {
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(dcValues);
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED, LOOKUP_IN_CHANNEL_REGISTER, Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore, new BrageLocation(null), SOME_CUSTOMER);

        record.getEntityDescription().getContributors().forEach(contributor -> {
            assertThat(contributor.getIdentity().getOrcId(), is(nullValue()));
        });
    }

    private static DcValue orcIdWithValue(String orcId) {
        return new DcValue(Element.CONTRIBUTOR, Qualifier.ORCID, orcId);
    }

    private static DcValue randomContributor() {
        return new DcValue(Element.CONTRIBUTOR, Qualifier.AUTHOR, randomString());
    }
}

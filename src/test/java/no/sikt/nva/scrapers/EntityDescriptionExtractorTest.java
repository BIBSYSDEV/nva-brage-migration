package no.sikt.nva.scrapers;

import java.io.File;
import java.util.Set;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.model.dublincore.Qualifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static no.sikt.nva.ResourceNameConstants.TEST_RESOURCE_PATH;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class EntityDescriptionExtractorTest {

    private static final String SOME_CUSTOMER = "nve";
    private static final boolean ONLINE_VALIDATION_DISABLED = false;

    private static final boolean LOOKUP_IN_CHANNEL_REGISTER = false;

    @Test
    void shouldMapLocalCodeToDescription() {
        var someLocalCode = "someLocalCode";
        var localCodeValue = new DcValue(Element.DESCRIPTION, Qualifier.LOCAL_CODE, someLocalCode);
        var typeDcValue = new DcValue(Element.TYPE, null, "Others");
        var dublinCore = DublinCoreFactory.createDublinCoreWithDcValues(List.of(localCodeValue, typeDcValue));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED,
                                                      LOOKUP_IN_CHANNEL_REGISTER,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                  new BrageLocation(null),
                                                                  SOME_CUSTOMER);
        var descriptions = record.getEntityDescription().getDescriptions();
        assertThat(descriptions, hasItem(someLocalCode));
    }

    @Test
    void shouldKeepOriginalOrderOfDescriptions() {
        var dublinCore = DublinCoreFactory.createDublinCoreFromXml(new File(TEST_RESOURCE_PATH +
                                                                            "dc_with_duplicated_values.xml"));
        var dublinCoreScraper = new DublinCoreScraper(ONLINE_VALIDATION_DISABLED,
                                                      LOOKUP_IN_CHANNEL_REGISTER,
                                                      Map.of());
        var record = dublinCoreScraper.validateAndParseDublinCore(dublinCore,
                                                                  new BrageLocation(null),
                                                                  SOME_CUSTOMER);
        var descriptions = record.getEntityDescription().getDescriptions();
        assertThat(descriptions, is(equalTo(Set.of("First description", "Second description"))));
    }
}

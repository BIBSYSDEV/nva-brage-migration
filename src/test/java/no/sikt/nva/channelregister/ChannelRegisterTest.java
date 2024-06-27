package no.sikt.nva.channelregister;

import static no.sikt.nva.brage.migration.common.model.NvaType.BACHELOR_THESIS;
import static no.sikt.nva.brage.migration.common.model.NvaType.DOCTORAL_THESIS;
import static no.sikt.nva.brage.migration.common.model.NvaType.MASTER_THESIS;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.util.Set;
import java.util.stream.Stream;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.record.Customer;
import no.sikt.nva.brage.migration.common.model.record.Publication;
import no.sikt.nva.brage.migration.common.model.record.PublicationContext;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.ResourceOwner;
import no.sikt.nva.brage.migration.common.model.record.Type;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ChannelRegisterTest {

    public static final String PRINT_ISSN = "2038-324X";
    public static final String ONLINE_ISSN = "2279-7084";

    public static Stream<Arguments> provideDegrees() {
        return Stream.of(Arguments.of(BACHELOR_THESIS.getValue()),
                         Arguments.of(MASTER_THESIS.getValue()),
                         Arguments.of(DOCTORAL_THESIS.getValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {PRINT_ISSN, ONLINE_ISSN})
    void shouldReturnJournalIdWhenIssnIsFound(String issn) {
        var register = ChannelRegister.getRegister();
        var brageLocation = new BrageLocation(null);
        var publication = new Publication();
        publication.setIssnList(Set.of(issn));
        publication.setJournal("someTitle");
        var actual = register.lookUpInJournal(publication, brageLocation);
        var expectedIdentifier = "70196DF2-7107-40F2-B6DF-045F3FAED38D";

        assertThat(actual, is(equalTo(expectedIdentifier)));
    }

    @Test
    void shouldReturnNullWhenIssnIsNotFound() {
        var issn = "dalksldaf";
        var register = ChannelRegister.getRegister();
        var brageLocation = new BrageLocation(null);
        var publication = new Publication();
        publication.setIssnList(Set.of(issn));
        publication.setJournal("someTitle");
        var actual = register.lookUpInJournal(publication, brageLocation);

        assertThat(actual, is(nullValue()));
    }

    @Test
    void shouldReturnNullWhenIssnIsNull() {
        var register = ChannelRegister.getRegister();
        var brageLocation = new BrageLocation(null);
        var publication = new Publication();
        publication.setIssnList(Set.of());
        publication.setJournal("someTitle");
        var actual = register.lookUpInJournal(publication, brageLocation);

        assertThat(actual, is(nullValue()));
    }

    @Test
    void shouldLookupPublisherInWildcards() {
        var record = new Record();

        var register = ChannelRegister.getRegister();
        var publication = new Publication();
        publication.setIssnList(Set.of());
        publication.setPublicationContext(new PublicationContext());
        publication.getPublicationContext()
            .setBragePublisher(randomString() + "Høgskolen i Oslo og Akershus" + randomString());
        record.setPublication(publication);
        var actual = register.lookUpInChannelRegisterForPublisher(record, "ntnu");

        assertThat(actual, is(equalTo("64780E54-AF46-4F0F-805F-D644500AFF92")));
    }

    @ParameterizedTest
    @MethodSource("provideDegrees")
    void shouldUseInstitutionPidWhenTheInstitutionIssuesDegrees(String nvaType) {
        var record = new Record();
        var resourceOwnerIssuingDegrees = "bi@158.0.0.0";
        record.setCustomer(new Customer("bi",
                                        UriWrapper.fromUri("https://api.test"
                                                           + ".nva.aws.unit"
                                                           + ".no/customer"
                                                           + "/some-customer").getUri()));
        record.setResourceOwner(new ResourceOwner(resourceOwnerIssuingDegrees,
                                                  UriWrapper.fromUri("https://api.test"
                                                                     + ".nva.aws.unit"
                                                                     + ".no/cristin"
                                                                     + "/organization"
                                                                     + "/some"
                                                                     + "-institution").getUri()));
        var publication = new Publication();
        var publicationContext = new PublicationContext();
        publicationContext.setBragePublisher("something not in channel registered and will be ignored");
        publication.setPublicationContext(publicationContext);
        record.setPublication(publication);
        record.setType(new Type(Set.of("Bachelor thesis"), nvaType));

        var expected = "E20D6091-159B-404E-B742-7B4D7D64C685";
        var register = ChannelRegister.getRegister();
        var actual = register.lookUpInChannelRegisterForPublisher(record, "bi");

        assertThat(actual, is(equalTo(expected)));
    }

    @ParameterizedTest
    @MethodSource("provideDegrees")
    void shouldNotUseInstitionPidWhenTheInstitutionDoesNotIssueDegrees(String nvaType) {
        var record = new Record();
        var resourceOwnerIssuingDegrees = "vegvesenet@158.0.0.0";
        record.setResourceOwner(new ResourceOwner(resourceOwnerIssuingDegrees, UriWrapper.fromUri("https://api.test"
                                                                                                  + ".nva.aws.unit"
                                                                                                  + ".no/customer"
                                                                                                  + "/some-customer").getUri()));
        var publication = new Publication();
        var publicationContext = new PublicationContext();
        publicationContext.setBragePublisher("something not in channel registered and will be ignored");
        publication.setPublicationContext(publicationContext);
        record.setPublication(publication);
        record.setType(new Type(Set.of("Bachelor thesis"), nvaType));


        var register = ChannelRegister.getRegister();
        var actual = register.lookUpInChannelRegisterForPublisher(record, "vegvesenet");

        assertThat(actual, is(nullValue()));
    }
}

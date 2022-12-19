package no.sikt.nva.channelregister;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import java.util.List;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.record.Publication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ChannelRegisterTest {

    public static final String PRINT_ISSN = "2038-324X";
    public static final String ONLINE_ISSN = "2279-7084";

    @ParameterizedTest
    @ValueSource(strings = {PRINT_ISSN, ONLINE_ISSN})
    void shouldReturnJournalIdWhenIssnIsFound(String issn) {
        var register = ChannelRegister.getRegister();
        var brageLocation = new BrageLocation(null);
        var publication = new Publication();
        publication.setIssnList(List.of(issn));
        publication.setJournal("someTitle");
        var actual = register.lookUpInJournal(publication, brageLocation);
        var expectedIdentifier = "503077";

        assertThat(actual, is(equalTo(expectedIdentifier)));
    }

    @Test
    void shouldReturnNullWhenIssnIsNotFound() {
        var issn = "dalksldaf";
        var register = ChannelRegister.getRegister();
        var brageLocation = new BrageLocation(null);
        var publication = new Publication();
        publication.setIssnList(List.of(issn));
        publication.setJournal("someTitle");
        var actual = register.lookUpInJournal(publication, brageLocation);

        assertThat(actual, is(nullValue()));
    }

    @Test
    void shouldReturnNullWhenIssnIsNull() {
        var register = ChannelRegister.getRegister();
        var brageLocation = new BrageLocation(null);
        var publication = new Publication();
        publication.setIssnList(List.of());
        publication.setJournal("someTitle");
        var actual = register.lookUpInJournal(publication, brageLocation);

        assertThat(actual, is(nullValue()));
    }
}

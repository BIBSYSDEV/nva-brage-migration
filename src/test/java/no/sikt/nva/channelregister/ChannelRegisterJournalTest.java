package no.sikt.nva.channelregister;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ChannelRegisterJournalTest {

    public static final String PRINT_ISSN = "2038-324X";
    public static final String ONLINE_ISSN = "2279-7084";

    @ParameterizedTest
    @ValueSource(strings = {PRINT_ISSN, ONLINE_ISSN})
    void shouldReturnJournalIdWhenIssnIsFound(String issn) {
        var register = ChannelRegister.getRegister();

        var actual = register.lookUpInJournalByIssn(issn);
        var expectedIdentifier = "503077";

        assertThat(actual, is(equalTo(expectedIdentifier)));
    }

    @Test
    void shouldReturnNullWhenIssnIsNotFound() {
        var issn = "dalksldaf";
        var register = ChannelRegister.getRegister();
        var actual = register.lookUpInJournalByIssn(issn);

        assertThat(actual, is(nullValue()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnNullWhenIssnIsNull(String issn) {
        var register = ChannelRegister.getRegister();
        var actual = register.lookUpInJournalByIssn(issn);

        assertThat(actual, is(nullValue()));
    }
}

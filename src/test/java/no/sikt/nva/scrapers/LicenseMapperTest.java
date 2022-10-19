package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.net.URI;
import org.junit.jupiter.api.Test;

public class LicenseMapperTest {

    @Test
    void shouldReturnTrueWhenUriIsValid() {
        var expected = true;
        var actual = LicenseMapper.isValidUri("http://creativecommons.org/licenses/by-nc-sa/4.0/deed.no");
        var uri = URI.create("http://creativecommons.org/licenses/by-nc-sa/4.0/deed.no");
        var host = uri.getHost();
        var some = uri.getUserInfo();
        assertThat(actual, is(equalTo(expected)));
    }

}

package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.io.File;
import java.util.List;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.record.Affiliation;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Identity;
import org.junit.jupiter.api.Test;

public class ContributorScraperTest {

    public static final String TEST_FILE_LOCATION = "src/test/resources/contributors.txt";

    @Test
    void shouldCreateContributorFromFile() {
        var expectedContributors = Map.of("Skaugen, Thomas", new Contributor(
            new Identity("Skaugen, Thomas", "22761"), null, null, List.of(new Affiliation("5948.0.0.0", null, null))));
        var actualContributors = ContributorScraper.getContributors(new File(TEST_FILE_LOCATION));
        assertThat(actualContributors, equalTo(expectedContributors));
    }
}

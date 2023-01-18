package no.sikt.nva.scrapers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.io.File;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.record.Affiliation;
import org.junit.jupiter.api.Test;

public class AffiliationsScraperTest {

    public static final String TEST_FILE_LOCATION = "src/test/resources/affiliations.txt";

    @Test
    void shouldCreateAffiliationsFromFile() {
        var expectedAffiliations = Map.of("1956/19599",
                                          new Affiliation("184.12.11.0", "Master theses (Department of Mathematics)",
                                                          "1956/19599"),
                                          "1956/939",
                                          new Affiliation("184.13.26.0",
                                                          "Department of Global Public Health and Primary Care",
                                                          "1956/939"));
        var actualAffiliations = AffiliationsScraper.getAffiliations(new File(TEST_FILE_LOCATION));
        assertThat(actualAffiliations, equalTo(expectedAffiliations));
    }
}

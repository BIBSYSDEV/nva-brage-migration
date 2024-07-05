package no.sikt.nva.scrapers.embargo;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class OnlineEmbargoCheckerImplTest {

    /**
     * Tests for OnlineEmbargoCheckerImpl, validating that it correctly identifies whether files in various Brage
     * archives are locked or unlocked.
     */

    private OnlineEmbargoCheckerImpl onlineEmbargoChecker;

    @BeforeEach
    void setUp() {
        onlineEmbargoChecker = new OnlineEmbargoCheckerImpl();
        onlineEmbargoChecker.setOutputDirectory(randomString());
    }

    @Tag("RemoteTest")
    @Test
    void shouldNotBeLockedUIO() {
        onlineEmbargoChecker.calculateCustomerAddress("uio");
        var locked = onlineEmbargoChecker.fileIsLockedOnline(
            "https://hdl.handle.net/10852/50021", "Masteroppgave-i-materialkjemi-Olav-Thorsen.pdf");

        assertFalse(locked);
    }

    @Tag("RemoteTest")
    @Test
    void shouldBeLockedUIO() {
        onlineEmbargoChecker.calculateCustomerAddress("uio");
        var locked = onlineEmbargoChecker.fileIsLockedOnline(
            "http://hdl.handle.net/10852/41496", "MasterMikaelSteen.pdf");

        assertTrue(locked);
    }

    @Tag("RemoteTest")
    @Test
    void shouldNotBeLockedNTNU() {
        onlineEmbargoChecker.calculateCustomerAddress("ntnu");
        var locked = onlineEmbargoChecker.fileIsLockedOnline(
            "https://hdl.handle.net/11250/3138417", "no.ntnu:inspera:187895219:131843768.pdf");

        assertFalse(locked);
    }

    @Tag("RemoteTest")
    @Test
    void shouldBeLockedNTNU() {
        onlineEmbargoChecker.calculateCustomerAddress("ntnu");
        var locked = onlineEmbargoChecker.fileIsLockedOnline(
            "http://hdl.handle.net/11250/231031", "659943_FULLTEXT01.pdf");

        assertTrue(locked);
    }

    @Tag("RemoteTest")
    @Test
    void shouldNotBeLockedUIT() {
        onlineEmbargoChecker.calculateCustomerAddress("uit");
        var locked = onlineEmbargoChecker.fileIsLockedOnline("https://hdl.handle.net/10037/19539", "article.pdf");

        assertFalse(locked);
    }

    @Tag("RemoteTest")
    @Test
    void shouldBeLockedUIT() {
        onlineEmbargoChecker.calculateCustomerAddress("uit");
        var locked = onlineEmbargoChecker.fileIsLockedOnline("https://hdl.handle.net/10037/18406", "thesis.pdf");

        assertTrue(locked);
    }

    @Tag("RemoteTest")
    @Test
    void shouldNotBeLockedBORA() {
        onlineEmbargoChecker.calculateCustomerAddress("bora");
        var locked = onlineEmbargoChecker.fileIsLockedOnline("https://hdl.handle.net/1956/1833", "Landoy.pdf");

        assertFalse(locked);
    }

    @Tag("RemoteTest")
    @Test
    void shouldBeLockedBORA() {
        onlineEmbargoChecker.calculateCustomerAddress("bora");
        var locked = onlineEmbargoChecker.fileIsLockedOnline("https://hdl.handle.net/1956/9372", "45429617.pdf");

        assertTrue(locked);
    }

    @Tag("RemoteTest")
    @Test
    void shouldNotBeLockedNR() {
        onlineEmbargoChecker.calculateCustomerAddress("nr");
        var locked = onlineEmbargoChecker.fileIsLockedOnline(
            "https://hdl.handle.net/11250/3076509", "Fuglerud+et+al+2023+iSt%C3%B8tet+sluttrapport-t.pdf");

        assertFalse(locked);
    }
}
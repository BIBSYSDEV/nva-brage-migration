package no.sikt.nva;

import no.sikt.nva.Main;
import org.junit.jupiter.api.Test;

public class MainTest {

    @Test
    void runningMain() {
        var main = new Main();
        var arguments = new String[]{};
        main.main(arguments);
    }
}

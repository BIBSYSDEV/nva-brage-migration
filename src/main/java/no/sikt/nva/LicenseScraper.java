package no.sikt.nva;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LicenseScraper {



    public String scrapeLicense(File file) throws IOException {
        return Files.readString(Path.of(file.getAbsolutePath()))
                   .replace("\n", "")
                   .replace("\r", "");
    }

}

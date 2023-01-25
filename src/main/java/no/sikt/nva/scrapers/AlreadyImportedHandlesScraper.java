package no.sikt.nva.scrapers;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nva.commons.core.paths.UriWrapper;

public final class AlreadyImportedHandlesScraper {
    public static final URI HANDLE_DOMAIN = UriWrapper.fromHost("https://hdl.handle.net").getUri();

    public static List<String> scrapeHandlesFromSuppliedExternalFile(File file) {
        try {
            return convertFileToHandlelist(Files.readString(file.toPath()));
        } catch (Exception e) {
            return List.of();
        }
    }

    private static List<String> convertFileToHandlelist(String string) {
        return Arrays.stream(string.split("\n"))
                   .map(AlreadyImportedHandlesScraper::constructHandle)
                   .collect(Collectors.toList());
    }

    private static String constructHandle(String line) {
        return UriWrapper.fromUri(HANDLE_DOMAIN).addChild(line.split(",")[0]).toString();
    }
}

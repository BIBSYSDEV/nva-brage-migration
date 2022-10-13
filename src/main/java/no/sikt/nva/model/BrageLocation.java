package no.sikt.nva.model;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

public class BrageLocation {

    public static final String ORIGIN_INFORMATION_STRING_TEMPLATE = "Bundle location: %s, Handle: %s";
    public static final String ORIGIN_INFORMATION = "Bundle location: %s";
    private final Path brageBundlePath;
    private URI handle;


    public BrageLocation(Path brageBundlePath) {
        this.brageBundlePath = brageBundlePath;

    }

    public Path getBrageBundlePath() {
        return brageBundlePath;
    }

    public URI getHandle() {
        return handle;
    }

    public void setHandle(URI handle) {
        this.handle = handle;
    }

    public String getOriginInformation() {
        return Objects.nonNull(handle)
                   ? String.format(ORIGIN_INFORMATION_STRING_TEMPLATE, getBrageBundlePath(), getHandle())
                   : String.format(ORIGIN_INFORMATION, getBrageBundlePath());
    }
}

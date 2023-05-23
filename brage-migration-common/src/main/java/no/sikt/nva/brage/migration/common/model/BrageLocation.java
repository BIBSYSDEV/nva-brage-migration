package no.sikt.nva.brage.migration.common.model;

import static java.util.Objects.nonNull;
import java.net.URI;
import java.nio.file.Path;

public class BrageLocation {

    public static final String ORIGIN_INFORMATION_STRING_TEMPLATE = "Handle: %s, brageLocation: %s";
    public static final String ORIGIN_INFORMATION = "Title: %s";
    private final Path brageBundlePath;
    private URI handle;
    private String title;

    public BrageLocation(Path brageBundlePath) {
        this.brageBundlePath = brageBundlePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Path getBrageBundlePath() {
        return nonNull(brageBundlePath)
                   ? Path.of(getCollectionDirectory(), getResourceDirectory())
                   : null;
    }

    public URI getHandle() {
        return handle;
    }

    public void setHandle(URI handle) {
        this.handle = handle;
    }

    public String getOriginInformation() {
        return nonNull(handle)
                   ? String.format(ORIGIN_INFORMATION_STRING_TEMPLATE, getHandle(), getBrageBundlePath())
                   : String.format(ORIGIN_INFORMATION, getTitle());
    }

    private String getCollectionDirectory() {

        var brageLocation = brageBundlePath.toString().split("/");
        return brageLocation[brageLocation.length - 2].replaceAll(" ", "");
    }

    private String getResourceDirectory() {
        var brageLocation = brageBundlePath.toString().split("/");
        return brageLocation[brageLocation.length - 1].replaceAll(" ", "");
    }
}

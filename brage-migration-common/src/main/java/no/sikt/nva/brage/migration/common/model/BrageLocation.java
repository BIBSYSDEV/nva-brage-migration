package no.sikt.nva.brage.migration.common.model;

import static java.util.Objects.nonNull;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import nva.commons.core.StringUtils;

public class BrageLocation {

    public static final String ORIGIN_INFORMATION_STRING_TEMPLATE = "Handle: %s";
    public static final String ORIGIN_INFORMATION = "Title: %s";

    public static final String ZIP_FILE_PATH_INFORMATION = "Zipfile location: %s";
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

    public String getZipFilePathInformation() {
        return Optional.ofNullable(getBrageBundlePath())
                   .map(Path::toString)
                   .map(information -> String.format(ZIP_FILE_PATH_INFORMATION, information))
                   .orElse(StringUtils.EMPTY_STRING);
    }

    public URI getHandle() {
        return handle;
    }

    public void setHandle(URI handle) {
        this.handle = handle;
    }

    public String getOriginInformation() {
        return nonNull(handle)
                   ? String.format("%s\t\t%s", getHandleInformation(), getZipFilePathInformation())
                   : String.format("%s\t\t%s", getTitleInformation(), getZipFilePathInformation());
    }

    private String getTitleInformation() {
        return String.format(ORIGIN_INFORMATION, getTitle());
    }

    private String getHandleInformation() {
        return String.format(ORIGIN_INFORMATION_STRING_TEMPLATE, getHandle());
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

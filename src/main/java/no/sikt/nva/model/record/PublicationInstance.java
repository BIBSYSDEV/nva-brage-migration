package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;

public class PublicationInstance {

    private String volume;
    private String issue;
    private Pages pages;

    @JacocoGenerated
    @JsonProperty("volume")
    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    @JacocoGenerated
    @JsonProperty("issue")
    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    @JacocoGenerated
    @JsonProperty("pages")
    public Pages getPages() {
        return pages;
    }

    public void setPageNumber(Pages pages) {
        this.pages = pages;
    }
}

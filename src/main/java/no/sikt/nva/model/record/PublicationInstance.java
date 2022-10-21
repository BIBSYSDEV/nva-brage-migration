package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;

public class PublicationInstance {

    private String volume;
    private String issue;
    private String pageNumber;

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
    @JsonProperty("pageNumber")
    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }
}

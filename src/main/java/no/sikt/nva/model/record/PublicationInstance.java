package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicationInstance {

    private String volume;
    private String issue;
    private String pageNumber;

    @JsonProperty("volume")
    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    @JsonProperty("issue")
    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    @JsonProperty("pageNumber")
    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }
}

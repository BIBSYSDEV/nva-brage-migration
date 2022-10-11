package no.sikt.nva.model.publisher;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public class Publication {

    private String journals;
    private String issn;
    private String publishers;

    @Override
    public int hashCode() {
        return Objects.hash(journals, issn, publishers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Publication publication = (Publication) o;
        return Objects.equals(journals, publication.journals)
               && Objects.equals(issn, publication.issn)
               && Objects.equals(publishers, publication.publishers);
    }

    @JacocoGenerated
    @JsonProperty("journals")
    public String getJournals() {
        return journals;
    }

    public void setJournals(String journals) {
        this.journals = journals;
    }

    @JacocoGenerated
    @JsonProperty("issns")
    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    @JacocoGenerated
    @JsonProperty("publishers")
    public String getPublishers() {
        return publishers;
    }

    public void setPublishers(String publishers) {
        this.publishers = publishers;
    }
}

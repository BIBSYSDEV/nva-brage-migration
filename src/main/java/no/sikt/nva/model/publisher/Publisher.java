package no.sikt.nva.model.publisher;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public class Publisher {

    private List<String> journals;
    private List<String> issn;
    private List<String> publishers;

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
        Publisher publisher = (Publisher) o;
        return Objects.equals(journals, publisher.journals)
               && Objects.equals(issn, publisher.issn)
               && Objects.equals(publishers, publisher.publishers);
    }

    @JsonProperty("journals")
    public List<String> getJournals() {
        return journals;
    }

    public void setJournals(List<String> journals) {
        this.journals = journals;
    }
    @JsonProperty("issns")
    public List<String> getIssn() {
        return issn;
    }

    public void setIssn(List<String> issn) {
        this.issn = issn;
    }

    @JsonProperty("publishers")
    public List<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }
}

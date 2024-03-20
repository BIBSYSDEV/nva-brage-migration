package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import nva.commons.core.JacocoGenerated;

public class PublisherAuthority {

    private Set<String> brage;
    private PublisherVersion nva;

    @JsonCreator
    public PublisherAuthority(@JsonProperty("brage") Set<String> brage,
                              @JsonProperty("nva") PublisherVersion nva) {
        this.nva = nva;
        this.brage = brage;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(brage, nva);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PublisherAuthority that = (PublisherAuthority) o;
        return Objects.equals(brage, that.brage) && Objects.equals(nva, that.nva);
    }

    @JsonProperty("brage")
    public Set<String> getBrage() {
        return brage;
    }

    public void setBrage(Set<String> brage) {
        this.brage = brage;
    }

    @JsonProperty("nva")
    public PublisherVersion getNva() {
        return nva;
    }

    public void setNva(PublisherVersion nva) {
        this.nva = nva;
    }
}

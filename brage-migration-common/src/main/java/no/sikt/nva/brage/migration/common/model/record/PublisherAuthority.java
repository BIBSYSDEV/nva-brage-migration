package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import nva.commons.core.JacocoGenerated;

public class PublisherAuthority {

    private String brage;
    private Optional<Boolean> nva;

    public PublisherAuthority() {
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

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(brage, nva);
    }

    @JsonProperty("brage")
    public String getBrage() {
        return brage;
    }

    public void setBrage(String brage) {
        this.brage = brage;
    }

    public void setNva(Optional<Boolean> nva) {
        this.nva = nva;
    }

    @JsonProperty("nva")
    public Optional<Boolean> getNva() {
        return nva;
    }
}

package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Set;

public class Language {

    private Set<String> brage;
    private URI nva;

    @JsonCreator
    public Language(@JsonProperty("brage") Set<String> brage,
                    @JsonProperty("nva") URI nva) {
        this.brage = brage;
        this.nva = nva;
    }

    @JsonProperty("brage")
    public Set<String> getBrage() {
        return brage;
    }

    public void setBrage(Set<String> brage) {
        this.brage = brage;
    }

    @JsonProperty("nva")
    public URI getNva() {
        return nva;
    }

    public void setNva(URI nva) {
        this.nva = nva;
    }
}

package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public class Language {

    private String brage;
    private URI nva;

    @JsonCreator
    public Language(@JsonProperty("brage") String brage,
                    @JsonProperty("nva") URI nva) {
        this.brage = brage;
        this.nva = nva;
    }

    @JsonProperty("brage")
    public String getBrage() {
        return brage;
    }

    public void setBrage(String brage) {
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

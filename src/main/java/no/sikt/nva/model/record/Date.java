package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("PMD.ShortClassName")
public class Date {

    private final String brage;
    private final String nva;

    @JacocoGenerated
    @JsonCreator
    public Date(@JsonProperty("brage") String brage,
                @JsonProperty("nva") String nva) {
        this.brage = brage;
        this.nva = nva;
    }

    public String getNva() {
        return nva;
    }

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
        Type type = (Type) o;
        return Objects.equals(getBrage(), type.getBrage()) && Objects.equals(getNva(), type.getNva());
    }

    public String getBrage() {
        return brage;
    }
}

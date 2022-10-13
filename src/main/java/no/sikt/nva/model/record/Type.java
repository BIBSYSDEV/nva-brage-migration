package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("PMD.ShortClassName")
public class Type {

    private final List<String> brage;
    private final String nva;

    @JsonCreator
    public Type(@JsonProperty("brage") List<String> brage,
                @JsonProperty("nva") String nva) {
        this.brage = brage;
        this.nva = nva;
    }

    public String getNva() {
        return nva;
    }

    public List<String> getBrage() {
        return brage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(brage, nva);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Type type = (Type) o;
        return Objects.equals(brage, type.brage) && Objects.equals(nva, type.nva);
    }
}

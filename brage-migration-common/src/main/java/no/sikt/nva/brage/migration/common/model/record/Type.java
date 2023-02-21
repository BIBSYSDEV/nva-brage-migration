package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("PMD.ShortClassName")
public class Type {

    private final Set<String> brage;
    private final String nva;

    @JacocoGenerated
    @JsonCreator
    public Type(@JsonProperty("brage") Set<String> brage,
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

    public Set<String> getBrage() {
        return brage;
    }
}

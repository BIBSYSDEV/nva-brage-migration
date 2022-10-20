package no.sikt.nva.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public class EntityDescription {

    private List<String> descriptions;
    private List<String> abstracts;

    @JsonProperty
    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    @JsonProperty
    public List<String> getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(List<String> abstracts) {
        this.abstracts = abstracts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptions,
                            abstracts);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof EntityDescription)) {
            return false;
        }
        var entityDescription = (EntityDescription) obj;
        return Objects.equals(descriptions, entityDescription.descriptions)
               && Objects.equals(abstracts, entityDescription.abstracts);
    }
}

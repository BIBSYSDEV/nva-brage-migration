package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.Objects;

@JsonTypeInfo(use = Id.NAME, property = "type")
public class Identity {

    private static final String IDENTITY_TYPE = "Identity";
    private final String type;
    @JsonProperty("name")
    private final String name;

    public Identity(@JsonProperty("name") String name) {
        this.name = name;
        this.type = IDENTITY_TYPE;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }


    public String getName() {
        return name;
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Identity identity = (Identity) o;
        return Objects.equals(name, identity.name) && Objects.equals(type, identity.type);
    }
}

package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Identity {

    private final String identifier;
    private String name;
    private URI orcId;

    public Identity(@JsonProperty("name") String name,
                    @JsonProperty("identifier") String identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Identity)) {
            return false;
        }
        Identity identity = (Identity) o;
        return Objects.equals(identifier, identity.identifier) && Objects.equals(name, identity.name) &&
               Objects.equals(orcId, identity.orcId);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(identifier, name, orcId);
    }
    @JsonProperty("orcId")
    public URI getOrcId() {
        return orcId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrcId(URI orcId) {
        this.orcId = orcId;
    }
}

package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import nva.commons.core.JacocoGenerated;

@JsonPropertyOrder({"brageRole", "role", "identity", "affiliations"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Contributor {

    private Set<Affiliation> affiliations;
    private Identity identity;
    private String role;
    private String brageRole;
    private Integer sequence;

    @JsonCreator
    public Contributor(@JsonProperty("identity") Identity identity,
                       @JsonProperty("role") String role,
                       @JsonProperty("brageRole") String brageRole,
                       @JsonProperty("affiliations") Set<Affiliation> affiliations) {
        this.identity = identity;
        this.role = role;
        this.brageRole = brageRole;
        this.affiliations = affiliations;
        this.sequence = null;
    }

    @JsonProperty("affiliations")
    public Set<Affiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(Set<Affiliation> affiliations) {
        this.affiliations = affiliations;
    }

    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @JsonProperty("identity")
    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public String getBrageRole() {
        return brageRole;
    }

    public void setBrageRole(String brageRole) {
        this.brageRole = brageRole;
    }

    public void addAffiliation(Affiliation affiliation) {
        affiliations.add(affiliation);
    }

    public boolean hasName(String name) {
        return name.equals(this.getIdentity().getName());
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getAffiliations(), getIdentity(), getRole(), getBrageRole(), getSequence());
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
        Contributor that = (Contributor) o;
        return Objects.equals(getAffiliations(), that.getAffiliations())
               && Objects.equals(getIdentity(), that.getIdentity())
               && Objects.equals(getRole(), that.getRole())
               && Objects.equals(getBrageRole(), that.getBrageRole())
               && Objects.equals(getSequence(), that.getSequence());
    }
}

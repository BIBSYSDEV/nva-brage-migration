package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class Project {

    public static final String COLON = ":";
    public static final String SLASH = "/";
    private final String identifier;
    private final String name;

    @JsonCreator
    private Project(@JsonProperty("identifier") String identifier, @JsonProperty("name") String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public static Project fromBrageValue(String value) {
        try {
            var colonIndex = value.lastIndexOf(COLON);
            var slashIndex = value.lastIndexOf(SLASH);

            var index = getSeparatorIndexPrioritizingColon(colonIndex, slashIndex);

            return extractProject(value, index);
        } catch (Exception e) {
            return null;
        }
    }

    private static int getSeparatorIndexPrioritizingColon(int colonIndex, int slashIndex) {
        return colonIndex > 0 ? colonIndex : slashIndex;
    }

    private static Project extractProject(String value, int index) {
        var identifier = value.substring(index + 1).trim();
        var name = value.substring(0, index).trim();
        return !identifier.isEmpty() && !name.isEmpty() ? new Project(identifier, name) : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier(), getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        return Objects.equals(getIdentifier(), project.getIdentifier()) && Objects.equals(getName(), project.getName());
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }
}

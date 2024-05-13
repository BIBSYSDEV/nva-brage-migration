package no.sikt.nva.brage.migration.common.model.record;

import java.util.Objects;

public final class Project {

    private final String identifier;
    private final String name;

    private Project(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public static Project fromBrageValue(String value) {
        var arrayOfValues = value.split(":");
        if (hasTwoEntries(arrayOfValues)) {
            var name = arrayOfValues[0].trim();
            var identifier = arrayOfValues[1].trim();
            return new Project(identifier, name);
        } else {
            return null;
        }
    }

    private static boolean hasTwoEntries(String[] arrayOfValues) {
        return arrayOfValues.length >= 2;
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

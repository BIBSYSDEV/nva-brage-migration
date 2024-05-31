package no.sikt.nva.brage.migration.common.model.record;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Project {

    public static final String COLON = ":";
    public static final String SLASH = "/";
    private final String identifier;
    private final String name;

    private Project(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public static Project fromBrageValue(String value) {
        try {
            if (value.contains(COLON)) {
                return extractProjectWithColon(value);
            }
            if (value.contains(SLASH)) {
                return extractProjectWithSlash(value);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static Project extractProjectWithSlash(String value) {
        var arrayOfValues = value.split(SLASH);
        if (hasTwoOrMoreEntries(arrayOfValues)) {
            var name = Arrays.stream(Arrays.copyOfRange(arrayOfValues, 0, arrayOfValues.length - 1))
                           .map(String::valueOf)
                           .map(String::trim)
                           .collect(Collectors.joining(SLASH));
            var identifier = arrayOfValues[arrayOfValues.length -1].trim();
            return new Project(identifier, name);
        } else {
            return null;
        }
    }

    private static Project extractProjectWithColon(String value) {
        var arrayOfValues = value.split(COLON);
        if (hasTwoOrMoreEntries(arrayOfValues)) {
            var name = arrayOfValues[0].trim();
            var identifier = arrayOfValues[1].trim();
            return new Project(identifier, name);
        } else {
            return null;
        }
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

    private static boolean hasTwoOrMoreEntries(String... values) {
        return values.length >= 2;
    }
}

package no.sikt.nva.brage.migration.common.model.record;

public class Project {

    private final String identifier;
    private final String name;

    private Project(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public static Project fromBrageValue(String value) {
        var arrayOfValues = value.split(":");

        return new Project(arrayOfValues[1].trim(), arrayOfValues[0].trim());
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }
}

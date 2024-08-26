package no.sikt.nva.brage.migration.common.model.record;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public final class Project {

    public static final String COLON = ":";
    public static final String SLASH = "/";
    public static final String COMMA = ",";
    private final String identifier;
    private final String name;
    private FundingSource fundingSource;

    @JsonCreator
    private Project(@JsonProperty("identifier") String identifier, @JsonProperty("name") String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public static Project fromBrageValue(String value, FundingSources fundingSources) {
        try {
            var colonIndex = value.lastIndexOf(COLON);
            var slashIndex = value.lastIndexOf(SLASH);
            var commaIndex = value.lastIndexOf(COMMA);

            var index = getSeparatorIndex(colonIndex, slashIndex, commaIndex);
            var project = extractProject(value, index);
            return nonNull(project) ? injectFundingSource(fundingSources, project) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Project injectFundingSource(FundingSources fundingSources, Project project) {
        return project.copy()
                   .withFundingSource(findFundingsSource(fundingSources, project))
                   .build();
    }

    public FundingSource getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(FundingSource fundingSource) {
        this.fundingSource = fundingSource;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder copy() {
        return builder().withIdentifier(this.identifier).withName(this.name).withFundingSource(this.fundingSource);
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

    private static FundingSource findFundingsSource(FundingSources fundingSources, Project project) {
        return fundingSources.getSources()
                   .stream()
                   .filter(source -> source.getName().containsValue(project.name))
                   .findFirst()
                   .orElse(null);
    }

    private static int getSeparatorIndex(int colonIndex, int slashIndex, int commaIndex) {
        if (commaIndex > 0) {
            return commaIndex;
        } else if (colonIndex > 0) {
            return colonIndex;
        } else {
            return slashIndex;
        }
    }

    private static Project extractProject(String value, int index) {
        var identifier = value.substring(index + 1).trim();
        var name = value.substring(0, index).trim();
        return !identifier.isEmpty() && !name.isEmpty() ? new Project(identifier, name) : null;
    }

    public static final class Builder {

        private String identifier;
        private String name;
        private FundingSource fundingSource;

        private Builder() {
        }

        public static Builder aProject() {
            return new Builder();
        }

        public Builder withIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withFundingSource(FundingSource fundingSource) {
            this.fundingSource = fundingSource;
            return this;
        }

        public Project build() {
            Project project = new Project(identifier, name);
            project.setFundingSource(fundingSource);
            return project;
        }
    }
}

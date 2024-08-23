package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class FundingSource {

    private final String identifier;
    private final Map<String, String> name;

    @JsonCreator
    public FundingSource(@JsonProperty("identifier") String identifier,
                         @JsonProperty("name") Map<String, String> name) {
        this.identifier = identifier;
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getName() {
        return name;
    }
}

package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class FundingSources {

    private final List<FundingSource> sources;

    @JsonCreator
    public FundingSources(@JsonProperty("sources") List<FundingSource> sources) {
        this.sources = sources;
    }

    public List<FundingSource> getSources() {
        return sources;
    }
}

package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"type", "id"})
public class Journal {

    public static final String DEFAULT_JOURNAL_TYPE = "Journal";
    private String id;
    private String type;

    @JsonCreator
    public Journal(@JsonProperty("id") String id) {
        this.id = id;
        this.type = DEFAULT_JOURNAL_TYPE;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

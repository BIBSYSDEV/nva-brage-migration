package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"type", "id"})
public class Series {

    public static final String DEFAULT_SERIES_TYPE = "Series";
    private String id;
    private String type;

    public Series(@JsonProperty("id") String id) {
        this.id = id;
        this.type = DEFAULT_SERIES_TYPE;
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

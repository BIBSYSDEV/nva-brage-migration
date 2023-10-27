package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Series {

    private String pid;

    public Series(@JsonProperty("pid") String pid) {
        this.pid = pid;
    }

    @JsonProperty("pid")
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}

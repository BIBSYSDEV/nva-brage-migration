package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;

public class PublishedDate {

    private Set<String> brageDates;
    private String nvaDate;

    public PublishedDate() {
    }

    @JsonProperty("brage")
    public Set<String> getBrageDates() {
        return brageDates;
    }

    public void setBrageDates(Set<String> brageDates) {
        this.brageDates = brageDates;
    }

    @Override
    public int hashCode() {
        return Objects.hash(brageDates, nvaDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PublishedDate that = (PublishedDate) o;
        return Objects.equals(brageDates, that.brageDates) && Objects.equals(nvaDate, that.nvaDate);
    }

    @JsonProperty("nva")
    public String getNvaDate() {
        return nvaDate;
    }

    public void setNvaDate(String nvaDate) {
        this.nvaDate = nvaDate;
    }
}

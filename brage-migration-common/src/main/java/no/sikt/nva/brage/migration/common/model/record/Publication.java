package no.sikt.nva.brage.migration.common.model.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import nva.commons.core.JacocoGenerated;

public class Publication {

    private String journal;
    private Set<String> issnList;
    private Set<String> isbnList;
    private Set<String> ismnList;
    private PublicationContext publicationContext;
    private String partOfSeries;

    public Publication() {
    }

    @JsonProperty("partOfSeries")
    public String getPartOfSeries() {
        return partOfSeries;
    }

    public void setPartOfSeries(String partOfSeries) {
        this.partOfSeries = partOfSeries;
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(journal,
                            issnList,
                            isbnList,
                            publicationContext,
                            ismnList,
                            partOfSeries);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Publication that = (Publication) o;
        return Objects.equals(journal, that.journal)
               && Objects.equals(issnList, that.issnList)
               && Objects.equals(isbnList, that.isbnList)
               && Objects.equals(publicationContext, that.publicationContext)
               && Objects.equals(ismnList, that.ismnList)
               && Objects.equals(partOfSeries, that.partOfSeries);
    }

    @JacocoGenerated
    @JsonProperty("journal")
    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    @JacocoGenerated
    @JsonProperty("issnList")
    public Set<String> getIssnSet() {
        return !issnList.isEmpty() ? issnList : Collections.emptySet();
    }

    public void setIssnList(Set<String> issnList) {
        this.issnList = issnList;
    }

    @JacocoGenerated
    @JsonProperty("isbnList")
    public Set<String> getIsbnSet() {
        return isbnList;
    }

    public void setIsbnList(Set<String> isbnList) {
        this.isbnList = isbnList;
    }

    @JacocoGenerated
    @JsonProperty("publicationContext")
    public PublicationContext getPublicationContext() {
        return publicationContext;
    }

    public void setPublicationContext(PublicationContext publicationContext) {
        this.publicationContext = publicationContext;
    }

    public void isIssmList(Set<String> ismnList) {
        this.ismnList = ismnList;
    }

    @JacocoGenerated
    public Set<String> getIsmnList() {
        return ismnList;
    }
}

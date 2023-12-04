package no.sikt.nva.scrapers;

import java.util.List;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.record.Affiliation;

public class AffiliationType {

    private final Map<String, Affiliation> affiliations;
    private final List<String> types;

    public AffiliationType(Map<String, Affiliation> affiliations, List<String> types) {
        this.affiliations = affiliations;
        this.types = types;
    }

    public Map<String, Affiliation> getAffiliations() {
        return affiliations;
    }

    public List<String> getTypes() {
        return types;
    }
}

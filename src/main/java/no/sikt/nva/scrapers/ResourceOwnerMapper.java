package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.record.ResourceOwner;
import nva.commons.core.paths.UriWrapper;

public final class ResourceOwnerMapper {

    public static final String TEST = "https://api.test.nva.aws.unit.no/cristin/organization/test";
    public static final String NVE = "https://api.test.nva.aws.unit.no/cristin/organization/5948.0.0.0";

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private static final Map<String, ResourceOwner> RESOURCE_OWNER_MAP = Map.ofEntries(
        entry("TEST", new ResourceOwner("TestOwner", UriWrapper.fromUri(TEST).getUri())),
        entry("NVE", new ResourceOwner(null, UriWrapper.fromUri(NVE).getUri()))
    );

    private ResourceOwnerMapper() {
    }

    public static ResourceOwner getResourceOwner(String customerShortName) {
        return RESOURCE_OWNER_MAP.getOrDefault(customerShortName, null);
    }
}

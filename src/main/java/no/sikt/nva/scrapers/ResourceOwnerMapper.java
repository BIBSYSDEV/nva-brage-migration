package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.net.URI;
import java.util.Map;
import no.sikt.nva.brage.migration.common.model.record.ResourceOwner;
import nva.commons.core.paths.UriWrapper;

public final class ResourceOwnerMapper {

    public static final String NVE = "NVE";
    public static final String NVE_OWNER_VALUE = "nve@5948.0.0.0";
    public static final URI NVE_CRISTIN_ORGANIZATION_URI_DEV =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/organization/5948.0.0.0").getUri();
    public static final String KRUS = "KRUS";
    public static final String KRUS_OWNER_VALUE = "krus@1661.0.0.0";
    public static final URI KRUS_CRISTIN_ORGANIZATION_URI_DEV =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/organization/1661.0.0.0").getUri();
    public static final String KRISTIANIA = "KRISTIANIA";
    public static final String KRISTIANIA_OWNER_VALUE = "kristiania@1615.0.0.0";
    public static final URI KRISTIANIA_CRISTIN_ORGANIZATION_URI_DEV =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/organization/1615.0.0.0").getUri();
    public static final String FHS = "FHS";
    public static final String FHS_OWNER_VALUE = "fhs@1627.0.0.0";
    public static final URI FHS_CRISTIN_ORGANIZATION_URI_DEV =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/organization/1627.0.0.0").getUri();
    public static final String HIOF = "HIOF";
    public static final String HIOF_OWNER_VALUE = "hiof@224.0.0.0";
    public static final URI HIOF_CRISTIN_ORGANIZATION_URI_DEV =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/organization/224.0.0.0").getUri();
    public static final String NGI = "NGI";
    public static final String NGI_OWNER_VALUE = "ngi@7452.0.0.0";
    public static final URI NGI_CRISTIN_ORGANIZATION_URI_DEV =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/organization/7452.0.0.0").getUri();
    public static final String NIBIO = "NIBIO";
    public static final String NIBIO_OWNER_VALUE = "nibio@7677.0.0.0";
    public static final URI NIBIO_CRISTIN_ORGANIZATION_URI_DEV =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/organization/7677.0.0.0").getUri();
    public static final String NIH = "NIH";
    public static final String NIH_OWNER_VALUE = "nih@150.0.0.0";
    public static final URI NIH_CRISTIN_ORGANIZATION_URI_DEV =
        UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/organization/150.0.0.0").getUri();

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private static final Map<String, ResourceOwner> RESOURCE_OWNER_MAP = Map.ofEntries(
        entry(NVE, new ResourceOwner(NVE_OWNER_VALUE, NVE_CRISTIN_ORGANIZATION_URI_DEV)),
        entry(KRUS, new ResourceOwner(KRUS_OWNER_VALUE, KRUS_CRISTIN_ORGANIZATION_URI_DEV)),
        entry(KRISTIANIA, new ResourceOwner(KRISTIANIA_OWNER_VALUE, KRISTIANIA_CRISTIN_ORGANIZATION_URI_DEV)),
        entry(FHS, new ResourceOwner(FHS_OWNER_VALUE, FHS_CRISTIN_ORGANIZATION_URI_DEV)),
        entry(HIOF, new ResourceOwner(HIOF_OWNER_VALUE, HIOF_CRISTIN_ORGANIZATION_URI_DEV)),
        entry(NGI, new ResourceOwner(NGI_OWNER_VALUE, NGI_CRISTIN_ORGANIZATION_URI_DEV)),
        entry(NIBIO, new ResourceOwner(NIBIO_OWNER_VALUE, NIBIO_CRISTIN_ORGANIZATION_URI_DEV)),
        entry(NIH, new ResourceOwner(NIH_OWNER_VALUE, NIH_CRISTIN_ORGANIZATION_URI_DEV))
    );

    private ResourceOwnerMapper() {
    }

    public static ResourceOwner getResourceOwner(String customerShortName) {
        return RESOURCE_OWNER_MAP.getOrDefault(customerShortName, null);
    }
}

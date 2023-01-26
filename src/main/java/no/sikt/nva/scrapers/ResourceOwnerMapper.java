package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.brage.migration.common.model.record.ResourceOwner;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public final class ResourceOwnerMapper {

    public static final String SANDBOX = "sandbox";
    public static final String DEVELOP = "dev";
    public static final String TEST = "test";
    public static final String PROD = "prod";
    public static final String NVE = "NVE";
    public static final String NVE_OWNER_VALUE = "nve@5948.0.0.0";
    public static final String NVE_CRISTIN_IDENTIFIER = "5948.0.0.0";
    public static final String KRUS = "KRUS";
    public static final String KRUS_OWNER_VALUE = "krus@1661.0.0.0";
    public static final String KRUS_CRISTIN_IDENTIFIER = "1661.0.0.0";
    public static final String KRISTIANIA = "KRISTIANIA";
    public static final String KRISTIANIA_OWNER_VALUE = "kristiania@1615.0.0.0";
    public static final String KRISTIANIA_CRISTIN_IDENTIFIER = "1615.0.0.0";
    public static final String FHS = "FHS";
    public static final String FHS_OWNER_VALUE = "fhs@1627.0.0.0";
    public static final String FHS_CRISTIN_IDENTIFIER = "1627.0.0.0";
    public static final String HIOF = "HIOF";
    public static final String HIOF_OWNER_VALUE = "hiof@224.0.0.0";
    public static final String HIOF_CRISTIN_IDENTIFIER = "224.0.0.0";
    public static final String NGI = "NGI";
    public static final String NGI_OWNER_VALUE = "ngi@7452.0.0.0";
    public static final String NGI_CRISTIN_IDENTIFIER = "7452.0.0.0";
    public static final String NIBIO = "NIBIO";
    public static final String NIBIO_OWNER_VALUE = "nibio@7677.0.0.0";
    public static final String NIBIO_CRISTIN_IDENTIFIER = "7677.0.0.0";
    public static final String NIH = "NIH";
    public static final String NIH_OWNER_VALUE = "nih@150.0.0.0";
    public static final String NIH_CRISTIN_IDENTIFIER = "150.0.0.0";
    public static final String CRISTIN_IDENTIFIER = "CRISTIN_IDENTIFIER";
    public static final String OWNER_VALUE = "OWNER_VALUE";
    public static final String SANDBOX_URI = "https://api.sandbox.nva.aws.unit.no/cristin/organization/";
    public static final String DEVELOP_URI = "https://api.sandbox.nva.aws.unit.no/cristin/organization/";
    public static final String TEST_URI = "https://api.test.nva.aws.unit.no/cristin/organization/";
    public static final String PROD_URI = "https://api.nva.aws.unit.no/cristin/organization/";
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private static final Map<String, Map<String, String>> RESOURCE_OWNER_MAP = Map.ofEntries(
        entry(NVE, Map.ofEntries(entry(OWNER_VALUE, NVE_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, NVE_CRISTIN_IDENTIFIER))),
        entry(KRUS, Map.ofEntries(entry(OWNER_VALUE, KRUS_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, KRUS_CRISTIN_IDENTIFIER))),
        entry(KRISTIANIA, Map.ofEntries(entry(OWNER_VALUE, KRISTIANIA_OWNER_VALUE),
                                        entry(CRISTIN_IDENTIFIER, KRISTIANIA_CRISTIN_IDENTIFIER))),
        entry(FHS, Map.ofEntries(entry(OWNER_VALUE, FHS_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, FHS_CRISTIN_IDENTIFIER))),
        entry(HIOF, Map.ofEntries(entry(OWNER_VALUE, HIOF_OWNER_VALUE),
                                  entry(CRISTIN_IDENTIFIER, HIOF_CRISTIN_IDENTIFIER))),
        entry(NGI, Map.ofEntries(entry(OWNER_VALUE, NGI_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, NGI_CRISTIN_IDENTIFIER))),
        entry(NIBIO, Map.ofEntries(entry(OWNER_VALUE, NIBIO_OWNER_VALUE),
                                   entry(CRISTIN_IDENTIFIER, NIBIO_CRISTIN_IDENTIFIER))),
        entry(NIH, Map.ofEntries(entry(OWNER_VALUE, NIH_OWNER_VALUE),
                                 entry(CRISTIN_IDENTIFIER, NIH_CRISTIN_IDENTIFIER))));
    private static final Map<String, String> ENVIRONMENT_URI_MAP = Map.ofEntries(
        entry(SANDBOX, SANDBOX_URI),
        entry(DEVELOP, DEVELOP_URI),
        entry(TEST, TEST_URI),
        entry(PROD, PROD_URI));

    private ResourceOwnerMapper() {
    }

    public static ResourceOwner getResourceOwner(String customerShortName, String environment) {
        return Optional.ofNullable(RESOURCE_OWNER_MAP)
                   .map(ownerMap -> ownerMap.get(customerShortName))
                   .map(valueMap -> constructResourceOwner(valueMap, environment))
                   .orElse(null);
    }

    private static ResourceOwner constructResourceOwner(Map<String, String> valueMap, String environment) {
        return new ResourceOwner(valueMap.get(OWNER_VALUE), constructCristinOrganizationUri(valueMap, environment));
    }

    private static URI constructCristinOrganizationUri(Map<String, String> valueMap, String environment) {
        return URI.create(ENVIRONMENT_URI_MAP.get(environment) + valueMap.get(CRISTIN_IDENTIFIER));
    }
}

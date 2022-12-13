package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.net.URI;
import java.util.Map;

public final class CustomerMapper {

    public static final String NVE = "NVE";
    public static final URI NVE_CUSTOMER_URI_DEVELOPMENT = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/b4497570-2903-49a2-9c2a-d6ab8b0eacc2");
    public static final String KRUS = "KRUS";
    public static final URI KRUS_CUSTOMER_URI_DEVELOPMENT = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/a768727e-4ecb-41c1-a616-1fec000cac1c");
    public static final String KRISTIANIA = "KRISTIANIA";
    public static final URI KRISTIANIA_CUSTOMER_URI_DEVELOPMENT = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/ca57b418-f837-40fd-af5c-5a6ba14abd7e");
    public static final String FHS = "FHS";
    public static final URI FHS_CUSTOMER_URI_DEVELOPMENT = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/290bb113-cf83-4917-8a07-463b4eca057b");
    public static final String HIOF = "HIOF";
    public static final URI HIOF_CUSTOMER_URI_DEVELOPMENT = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/5c243c2d-a8fb-48fb-a2c6-c6fa70310194");
    public static final String NGI = "NGI";
    public static final URI NGI_CUSTOMER_URI_DEVELOPMENT = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/f415cb81-ac56-4244-b31b-25e43dc3027e");
    public static final String NIBIO = "NIBIO";
    public static final URI NIBIO_CUSTOMER_URI_DEVELOPMENT = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/82c8b036-39cb-4ac2-8a2d-152cda4bcc27");
    public static final String NIH = "NIH";
    public static final URI NIH_CUSTOMER_URI_DEVELOPMENT = URI.create(
        "https://api.dev.nva.aws.unit.no/customer/b5ff15a1-0e58-44bf-b137-d5c5389ef63f");
    private static final Map<String, URI> CUSTOMER_MAP = Map.ofEntries(
        entry(NVE, NVE_CUSTOMER_URI_DEVELOPMENT),
        entry(KRUS, KRUS_CUSTOMER_URI_DEVELOPMENT),
        entry(KRISTIANIA, KRISTIANIA_CUSTOMER_URI_DEVELOPMENT),
        entry(FHS, FHS_CUSTOMER_URI_DEVELOPMENT),
        entry(HIOF, HIOF_CUSTOMER_URI_DEVELOPMENT),
        entry(NGI, NGI_CUSTOMER_URI_DEVELOPMENT),
        entry(NIBIO, NIBIO_CUSTOMER_URI_DEVELOPMENT),
        entry(NIH, NIH_CUSTOMER_URI_DEVELOPMENT)
    );

    private CustomerMapper() {

    }

    public static URI getCustomerUri(String customerShortName) {
        return CUSTOMER_MAP.getOrDefault(customerShortName, null);
    }
}

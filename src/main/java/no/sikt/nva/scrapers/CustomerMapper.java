package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.net.URI;
import java.util.Map;

public final class CustomerMapper {

    //TODO: Find customer identifier for NVA and put it in the map;
    private static final Map<String, URI> CUSTOMER_MAP = Map.ofEntries(
        entry("TEST", URI.create("https://api.nva.unit.no/customer/test")),
        entry("NVE", URI.create("https://api.sandbox.nva.aws.unit.no/customer/5eb7ca32-0e6b-4819-b794-c31a4b16ea6b")),
        entry("KRUS", URI.create("https://api.sandbox.nva.aws.unit.no/customer/6b5e1238-7a05-11ed-a1eb-0242ac120002"))
    );

    private CustomerMapper() {

    }

    public static URI getCustomerUri(String customerShortName) {
        return CUSTOMER_MAP.getOrDefault(customerShortName, null);
    }
}

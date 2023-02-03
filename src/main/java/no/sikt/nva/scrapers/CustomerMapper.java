package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CustomerMapper {

    public static final String NVE = "nve";
    public static final String KRUS = "krus";
    public static final String KRISTIANIA = "kristiania";
    public static final String FHS = "fhs";
    public static final String HIOF = "hiof";
    public static final String NGI = "ngi";
    public static final String NIBIO = "nibio";

    public static final String NIH = "nih";
    public static final String SANDBOX = "sandbox";
    public static final String DEVELOP = "dev";
    public static final String TEST = "test";
    public static final String PROD = "prod";

    private static final Map<String, Map<String, String>> CUSTOMER_MAP = Map.ofEntries(
        entry(NVE, Map.ofEntries(entry(SANDBOX, "5eb7ca32-0e6b-4819-b794-c31a4b16ea6b"),
                                 entry(DEVELOP, "b4497570-2903-49a2-9c2a-d6ab8b0eacc2"),
                                 entry(TEST, ""),
                                 entry(PROD, "4ba5f697-2056-4292-b0a3-f81ccf21ea22"))),

        entry(KRUS, Map.ofEntries(entry(SANDBOX, "6b5e1238-7a05-11ed-a1eb-0242ac120002"),
                                  entry(DEVELOP, "a768727e-4ecb-41c1-a616-1fec000cac1c"),
                                  entry(TEST, ""),
                                  entry(PROD, ""))),

        entry(KRISTIANIA, Map.ofEntries(entry(SANDBOX, "8fb3c2f4-da97-4eb1-be65-307c86b993ee"),
                                        entry(DEVELOP, "ca57b418-f837-40fd-af5c-5a6ba14abd7e"),
                                        entry(TEST, ""),
                                        entry(PROD, "05329411-0aa7-4c68-8cc6-5875f0d58f8c"))),

        entry(FHS, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "290bb113-cf83-4917-8a07-463b4eca057b"),
                                 entry(TEST, ""),
                                 entry(PROD, ""))),

        entry(HIOF, Map.ofEntries(entry(SANDBOX, ""),
                                  entry(DEVELOP, "5c243c2d-a8fb-48fb-a2c6-c6fa70310194"),
                                  entry(TEST, ""),
                                  entry(PROD, ""))),

        entry(NGI, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "f415cb81-ac56-4244-b31b-25e43dc3027e"),
                                 entry(TEST, ""),
                                 entry(PROD, ""))),

        entry(NIBIO, Map.ofEntries(entry(SANDBOX, ""),
                                   entry(DEVELOP, "82c8b036-39cb-4ac2-8a2d-152cda4bcc27"),
                                   entry(TEST, ""),
                                   entry(PROD, ""))),

        entry(NIH, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "b5ff15a1-0e58-44bf-b137-d5c5389ef63f"),
                                 entry(TEST, ""),
                                 entry(PROD, ""))));

    private static final String SANDBOX_URI = "https://api.sandbox.nva.aws.unit.no/customer/";
    private static final String DEVELOP_URI = "https://api.dev.nva.aws.unit.no/customer/";
    private static final String TEST_URI = "https://api.test.nva.aws.unit.no/customer/";
    private static final String PROD_URI = "https://api.nva.aws.unit.no/customer/";
    private static final Map<String, String> ENVIRONMENT_URI_MAP = Map.ofEntries(
        entry(SANDBOX, SANDBOX_URI),
        entry(DEVELOP, DEVELOP_URI),
        entry(TEST, TEST_URI),
        entry(PROD, PROD_URI));

    public CustomerMapper() {

    }

    public URI getCustomerUri(String customerShortName, String environment) {
        return Optional.ofNullable(CUSTOMER_MAP)
                   .map(customerMap -> customerMap.getOrDefault(customerShortName.toLowerCase(Locale.ROOT), null))
                   .map(environmentMap -> environmentMap.get(environment))
                   .map(customerIdentifier -> constructCustomerUri(environment, customerIdentifier))
                   .orElse(null);
    }

    private URI constructCustomerUri(String environment, String customerIdentifier) {
        return URI.create(ENVIRONMENT_URI_MAP.get(environment) + customerIdentifier);
    }
}

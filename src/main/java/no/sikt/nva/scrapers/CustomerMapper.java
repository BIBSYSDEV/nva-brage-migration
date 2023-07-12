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
    public static final String NTNU = "ntnu";
    public static final String NR = "nr";
    public static final String SAMFORSK = "samforsk";
    public static final String CMI = "cmi";
    public static final String DMMH = "dmmh";
    public static final String FAFO = "fafo";
    public static final String UIA = "uia";
    public static final String FDIR = "fdir";
    public static final String NORD = "nord";
    public static final String ODA = "oda";
    public static final String HIMOLDE = "himolde";
    public static final String HIVOLDA = "hivolda";
    public static final String HVLOPEN = "hvlopen";
    public static final String NHH = "nhh";
    public static final String BORA = "bora";
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
                                  entry(TEST, "719fdb5a-f90a-4e20-b81d-1dde3ca6e647"),
                                  entry(PROD, ""))),

        entry(NGI, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "f415cb81-ac56-4244-b31b-25e43dc3027e"),
                                 entry(TEST, ""),
                                 entry(PROD, ""))),

        entry(NIBIO, Map.ofEntries(entry(SANDBOX, ""),
                                   entry(DEVELOP, "82c8b036-39cb-4ac2-8a2d-152cda4bcc27"),
                                   entry(TEST, "cd925eea-7f4c-4167-9b7c-a7bf7f8eca59"),
                                   entry(PROD, "77766640-6066-48ef-a32c-0ba7f32abee9"))),

        entry(NIH, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "b5ff15a1-0e58-44bf-b137-d5c5389ef63f"),
                                 entry(TEST, ""),
                                 entry(PROD, ""))),
        entry(NTNU, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "8ddecceb-7d64-4df3-a842-489fe4d98f3a"),
                                 entry(TEST, "33c17ef6-864b-4267-bc9d-0cee636e247e"),
                                 entry(PROD, ""))),
        entry(NR, Map.ofEntries(entry(SANDBOX, ""),
                                  entry(DEVELOP, "54bed9d9-2bc4-4fa4-a6c4-9e957f93870d"),
                                  entry(TEST, ""),
                                  entry(PROD, ""))),
        entry(SAMFORSK, Map.ofEntries(entry(SANDBOX, ""),
                                entry(DEVELOP, "6da57387-b48a-46dc-a395-d07c12b1e54b"),
                                entry(TEST, ""),
                                entry(PROD, ""))),
        entry(CMI, Map.ofEntries(entry(SANDBOX, ""),
                                      entry(DEVELOP, "aebc37b9-593c-42b6-88bf-0d8a54492a32"),
                                      entry(TEST, ""),
                                      entry(PROD, ""))),
        entry(DMMH, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "0edc1e9a-a74b-4169-86ba-f98b92d0115e"),
                                 entry(TEST, "2e4f348c-47a3-4500-94dd-9b4ee4ba63d9"),
                                 entry(PROD, ""))),
        entry(FAFO, Map.ofEntries(entry(SANDBOX, ""),
                                  entry(DEVELOP, "eab76eb2-3f15-40af-9056-53946ec8fcde"),
                                  entry(TEST, "648a3ed0-f34e-48ff-8db9-092b2f5d5fa2"),
                                  entry(PROD, ""))),
        entry(FDIR, Map.ofEntries(entry(SANDBOX, ""),
                                  entry(DEVELOP, "4403a49b-a175-4985-a695-f16c5b87c9ac"),
                                  entry(TEST, ""),
                                  entry(PROD, ""))),
        entry(UIA, Map.ofEntries(entry(SANDBOX, ""),
                                  entry(DEVELOP, "8b77046d-2001-47cb-b061-0346d5b4b95c"),
                                  entry(TEST, "2732ed9a-971a-4e34-ac84-d5f6d067ed66"),
                                  entry(PROD, "20c7aad5-af76-4909-afe2-cdc9c7ec4f79"))),
        entry(NORD, Map.ofEntries(entry(SANDBOX, ""),
                                  entry(DEVELOP, "f94d1d3e-e05a-41f9-8eb8-f891ccd25666"),
                                  entry(TEST, "27c650e1-9af2-40fd-91c0-3abdf38c86f0"),
                                  entry(PROD, "0e878881-87b9-4392-b4e4-b410bbecbd1d"))),
        entry(ODA, Map.ofEntries(entry(SANDBOX, ""),
                                  entry(DEVELOP, "8db4a8bd-3db5-420b-a2e4-fc17a489aa6d"),
                                  entry(TEST, ""),
                                  entry(PROD, ""))),
        entry(HIMOLDE, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "2927ca20-cbd0-4b0a-abd3-3de23e8f2065"),
                                 entry(TEST, "e8833011-2e16-4765-b1dd-494b64c8d646"),
                                 entry(PROD, "9e6b93b6-1ef2-4b91-9599-ebc9ac8c6606"))),
        entry(HIVOLDA, Map.ofEntries(entry(SANDBOX, ""),
                                     entry(DEVELOP, "9c81f056-5839-4843-9b93-20962281df38"),
                                     entry(TEST, "235dc022-6beb-4f8f-8165-9fbe680e1f5b"),
                                     entry(PROD, "ca410109-2720-475b-a280-59783e2962c7"))),
        entry(HVLOPEN, Map.ofEntries(entry(SANDBOX, ""),
                                     entry(DEVELOP, "fe945ea8-22d1-481c-b5be-4e0447d5656c"),
                                     entry(TEST, "d60e86ef-2bcd-4e0b-8872-43f80858caaa"),
                                     entry(PROD, "2e3ae52c-ada5-4862-b72a-98e74690465c"))),
        entry(NHH, Map.ofEntries(entry(SANDBOX, ""),
                                     entry(DEVELOP, "418e4bf8-6f7e-419c-b7b7-45bcbab38429"),
                                     entry(TEST, "7bf52e6f-82dc-4203-a25e-93630819b741"),
                                     entry(PROD, "1432ce2a-5f2e-4560-8b6c-9af87c082d53"))),
        entry(BORA, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "a228aba6-932b-4f53-b2de-31ad8daf9f8d"),
                                 entry(TEST, "7c41e7cd-f494-4266-9979-48285cbb2434"),
                                 entry(PROD, "2f42a7e8-219b-4289-9b41-99b32a6b4866")))

    );

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
        return Optional.ofNullable(customerShortName)
                   .map(customer -> CUSTOMER_MAP.get(customer.toLowerCase(Locale.ROOT)))
                   .map(environmentMap -> environmentMap.get(environment))
                   .map(customerIdentifier -> constructCustomerUri(environment, customerIdentifier))
                   .orElse(null);
    }

    private URI constructCustomerUri(String environment, String customerIdentifier) {
        return URI.create(ENVIRONMENT_URI_MAP.get(environment) + customerIdentifier);
    }
}

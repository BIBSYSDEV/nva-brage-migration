package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import nva.commons.core.StringUtils;

@SuppressWarnings("PMD.DoubleBraceInitialization")
public class CustomerMapper {

    public static final String AHO = "aho";
    public static final String BANENOR = "banenor";
    public static final String BI = "bi";
    public static final String BORA = "bora";
    public static final String DMMH = "dmmh";
    public static final String CMI = "cmi";
    public static final String FAFO = "fafo";
    public static final String FDIR = "fdir";
    public static final String FHS = "fhs";
    public static final String HIOF = "hiof";
    public static final String HIVOLDA = "hivolda";
    public static final String HIMOLDE = "himolde";
    public static final String HVLOPEN = "hvlopen";
    public static final String IFE = "ife";
    public static final String INN = "inn";
    public static final String KHIO = "khio";
    public static final String KRISTIANIA = "kristiania";
    public static final String KRUS = "krus";
    public static final String LDH = "ldh";
    public static final String MF = "mf";
    public static final String NASJONALMUSEET = "nasjonalmuseet";
    public static final String NIBIO = "nibio";
    public static final String NGI = "ngi";
    public static final String NHH = "nhh";
    public static final String NIFU = "nifu";
    public static final String NIH = "nih";
    public static final String NIVA = "niva";
    public static final String NORD = "nord";
    public static final String NORGES_BANK = "norges-bank";
    public static final String NR = "nr";
    public static final String NTNU = "ntnu";
    public static final String NUPI = "nupi";
    public static final String NVE = "nve";
    public static final String ODA = "oda";
    public static final String OMSORGSFORSKNING = "omsorgsforskning";
    public static final String PHS = "phs";
    public static final String RA = "ra";
    public static final String SAMFORSK = "samforsk";
    public static final String STAMI = "stami";
    public static final String STATPED = "statped";
    public static final String STEINERHOYSKOLEN = "steinerhoyskolen";
    public static final String UIA = "uia";
    public static final String UNIT = "unit";
    public static final String VETINST = "vetinst";
    public static final String VID = "vid";
    public static final String VEGVESEN = "vegvesen";
    public static final String FFI = "ffi";
    public static final String SANDBOX = "sandbox";
    public static final String DEVELOP = "dev";
    public static final String TEST = "test";
    public static final String PROD = "prod";
    public static final String NB = "nb";
    public static final String NFORSK = "nforsk";
    public static final String CICERO = "cicero";
    public static final String FNI = "fni";
    public static final String NLA = "nla";
    public static final String NINA = "nina";
    public static final String NMBU = "nmbu";
    public static final String NMH = "nmh";
    public static final String NOFIMA = "nofima";
    public static final String NORCERESEARCH = "norceresearch";
    public static final String NORSKFOLKEMUSEUM = "norskfolkemuseum";
    public static final String NPOLAR = "npolar";
    public static final String RURALIS = "ruralis";
    public static final String SAMAS = "samas";
    public static final String SAMFUNNSFORSKNING = "samfunnsforskning";
    public static final String SIHF = "sihf";
    public static final String SINTEF = "sintef";
    public static final String SSB = "ssb";
    public static final String UIS = "uis";
    public static final String USN = "usn";
    public static final String NIKU = "niku";
    public static final String NILU = "nilu";
    public static final String NGU = "ngu";
    public static final String UIT = "uit";
    public static final String UIO = "uio";
    public static final String FHI = "fhi";
    public static final String R_BUP = "r-bup";
    public static final String IMR = "imr";
    public static final String TOI = "toi";
    private static final Map<String, Map<String, String>> CUSTOMER_MAP = new ConcurrentHashMap<>()
    {{

            put(NUPI, Map.ofEntries(
                entry(SANDBOX, ""),
                entry(DEVELOP, "8d11f0f0-d414-4564-9250-298c06ef5c87"),
                entry(TEST, "122d33a7-1844-466a-926b-333eeff42a93"),
                entry(PROD, "64413cd2-13c8-4f9b-aa1b-6750079f61a8")));
                put(OMSORGSFORSKNING, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "8ddecceb-7d64-4df3-a842-489fe4d98f3a"), // customer address to NTNU
                    entry(TEST, "33c17ef6-864b-4267-bc9d-0cee636e247e"), // customer address to NTNU
                    entry(PROD, "7509c2b8-dcaf-463e-a041-88dd89435f59")));
                put(INN, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "023f1608-1baa-4d5a-8acc-80d329bddd74"),
                    entry(TEST, "df2a796f-3eef-45dc-a39d-51b21989285a"),
                    entry(PROD, "54c3e27c-b414-4f42-8aa4-55bccf09cb49")));
                put(BI, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "a49a2deb-f782-4ab1-b2ab-97745bc14b3d"),
                    entry(TEST, "e9fb0c5d-1589-4d6f-932f-b312e50daa8f"),
                    entry(PROD, "cabc0883-6ba6-4860-ba6c-90b854b18c60")));
                put(VID, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "58d2d14c-ff23-4aa0-94f1-b53190fd020a"),
                    entry(TEST, "9b17e692-6690-41a2-923b-b7ac0d7a1522"),
                    entry(PROD, "8df3cee4-1d52-4c6a-910d-17d0386323f6")));
                put(BANENOR, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "4a8749c6-567b-42bd-b8c2-384aa41f43d3"),
                    entry(TEST, "ccfff2f4-e50a-4ee9-af52-469821476cc3"),
                    entry(PROD, "646b979a-1833-4048-9cd3-ae4043a4cba4")));
                put(RA, Map.ofEntries(
                    entry(SANDBOX, "b3435185-929a-4842-96ee-4243f4c9078c"),
                    entry(DEVELOP, "e974d7d1-50b5-47c0-ad7e-7135a2d47555"),
                    entry(TEST, "434ed8ed-f302-409f-9943-8886320550a3"),
                    entry(PROD, "9e389a98-5115-42cf-aac8-55d46f4272a0")));
                put(NORGES_BANK, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "9d02e866-aeba-466d-993a-b519c664d38c"),
                    entry(TEST, "2d37e9a9-dc70-4c30-bdb6-c7724eb7f0c3"),
                    entry(PROD, "de3c6727-8a17-4efa-b2d4-9bf248e0bf5a")));
                put(NVE, Map.ofEntries(
                    entry(SANDBOX, "5eb7ca32-0e6b-4819-b794-c31a4b16ea6b"),
                    entry(DEVELOP, "b4497570-2903-49a2-9c2a-d6ab8b0eacc2"),
                    entry(TEST, ""),
                    entry(PROD, "4ba5f697-2056-4292-b0a3-f81ccf21ea22")));
                put(KRUS, Map.ofEntries(
                    entry(SANDBOX, "6b5e1238-7a05-11ed-a1eb-0242ac120002"),
                    entry(DEVELOP, "a768727e-4ecb-41c1-a616-1fec000cac1c"),
                    entry(TEST, "2e0dc263-5af7-4b0e-8bdc-c6f2c660dfd3"),
                    entry(PROD, "a7b6b6cb-a69d-48a6-af42-a9a4401efd26")));
                put(KRISTIANIA, Map.ofEntries(
                    entry(SANDBOX, "8fb3c2f4-da97-4eb1-be65-307c86b993ee"),
                    entry(DEVELOP, "ca57b418-f837-40fd-af5c-5a6ba14abd7e"),
                    entry(TEST, "c1d7b72b-3319-4a1f-a365-c04ddb729b6f"),
                    entry(PROD, "05329411-0aa7-4c68-8cc6-5875f0d58f8c")));
                put(FHS, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "290bb113-cf83-4917-8a07-463b4eca057b"),
                    entry(TEST, "ba0b80e8-ca87-4b8f-bf2a-6d00188ad707"),
                    entry(PROD, "0dbaedb3-4849-4eb6-b272-d6afa689bd0f")));
                put(HIOF, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "5c243c2d-a8fb-48fb-a2c6-c6fa70310194"),
                    entry(TEST, "719fdb5a-f90a-4e20-b81d-1dde3ca6e647"),
                    entry(PROD, "fc77ac8f-6adc-474c-9cf9-42a52623dd6b")));
                put(NGI, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "f415cb81-ac56-4244-b31b-25e43dc3027e"),
                    entry(TEST, "1f899570-ed06-4c08-bc9f-ed92402e5128"),
                    entry(PROD, "0557ff2c-9658-47e6-8289-c98e4555bb64")));
                put(NIBIO, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "82c8b036-39cb-4ac2-8a2d-152cda4bcc27"),
                    entry(TEST, "cd925eea-7f4c-4167-9b7c-a7bf7f8eca59"),
                    entry(PROD, "77766640-6066-48ef-a32c-0ba7f32abee9")));
                put(NIH, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "b5ff15a1-0e58-44bf-b137-d5c5389ef63f"),
                    entry(TEST, "6c7ac184-1b21-425c-a422-2855804be2aa"),
                    entry(PROD, "e69348db-19da-4e5a-bbc8-43ec57c43f10")));
                put(NTNU, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "8ddecceb-7d64-4df3-a842-489fe4d98f3a"),
                    entry(TEST, "33c17ef6-864b-4267-bc9d-0cee636e247e"),
                    entry(PROD, "7509c2b8-dcaf-463e-a041-88dd89435f59")));
                put(NR, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "54bed9d9-2bc4-4fa4-a6c4-9e957f93870d"),
                    entry(TEST, "505328d2-1992-406c-8792-b064748d2f38"),
                    entry(PROD, "382ef5b6-19d1-4f64-9b27-bff934ceb9a8")));
                put(SAMFORSK, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "6da57387-b48a-46dc-a395-d07c12b1e54b"),
                    entry(TEST, "999f15f5-b0d4-4273-8dc4-97a971454b74"),
                    entry(PROD, "7d2c7158-8944-463b-b7ed-7477878e679e")));
                put(CMI, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "aebc37b9-593c-42b6-88bf-0d8a54492a32"),
                    entry(TEST, "f9a5a7fb-5add-4968-ac10-68e36953cbb9"),
                    entry(PROD, "07f5674d-8f59-4b81-8b24-fa4f3ab35c70")));
                put(DMMH, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "0edc1e9a-a74b-4169-86ba-f98b92d0115e"),
                    entry(TEST, "2e4f348c-47a3-4500-94dd-9b4ee4ba63d9"),
                    entry(PROD, "9ebd6346-2a9e-41ca-bdd5-80b95d2f5109")));
                put(FAFO, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "eab76eb2-3f15-40af-9056-53946ec8fcde"),
                    entry(TEST, "648a3ed0-f34e-48ff-8db9-092b2f5d5fa2"),
                    entry(PROD, "d7aea10a-5643-4f8a-8e74-5b1a11ff669c")));
                put(FDIR, Map.ofEntries(
                    entry(SANDBOX, "2e8f2de9-b6e7-411e-93f4-615b6da4225a"),
                    entry(DEVELOP, "4403a49b-a175-4985-a695-f16c5b87c9ac"),
                    entry(TEST, "c92c0f86-6a65-484a-ae0e-a045029168b5"),
                    entry(PROD, "")));
                put(UIA, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "8b77046d-2001-47cb-b061-0346d5b4b95c"),
                    entry(TEST, "2732ed9a-971a-4e34-ac84-d5f6d067ed66"),
                    entry(PROD, "20c7aad5-af76-4909-afe2-cdc9c7ec4f79")));
                put(NORD, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "f94d1d3e-e05a-41f9-8eb8-f891ccd25666"),
                    entry(TEST, "27c650e1-9af2-40fd-91c0-3abdf38c86f0"),
                    entry(PROD, "0e878881-87b9-4392-b4e4-b410bbecbd1d")));
                put(ODA, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "8db4a8bd-3db5-420b-a2e4-fc17a489aa6d"),
                                       entry(TEST, "b7c2979e-c56b-4493-84ce-3cfa7bb3ba27"),
                                       entry(PROD, "60f650d1-43ae-453c-ae1a-d131f0500487")));
                put(HIMOLDE, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "2927ca20-cbd0-4b0a-abd3-3de23e8f2065"),
                                           entry(TEST, "e8833011-2e16-4765-b1dd-494b64c8d646"),
                                           entry(PROD, "9e6b93b6-1ef2-4b91-9599-ebc9ac8c6606")));
                put(HIVOLDA, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "9c81f056-5839-4843-9b93-20962281df38"),
                                           entry(TEST, "235dc022-6beb-4f8f-8165-9fbe680e1f5b"),
                                           entry(PROD, "ca410109-2720-475b-a280-59783e2962c7")));
                put(HVLOPEN, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "fe945ea8-22d1-481c-b5be-4e0447d5656c"),
                                           entry(TEST, "d60e86ef-2bcd-4e0b-8872-43f80858caaa"),
                                           entry(PROD, "2e3ae52c-ada5-4862-b72a-98e74690465c")));
                put(NHH, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "418e4bf8-6f7e-419c-b7b7-45bcbab38429"),
                                       entry(TEST, "7bf52e6f-82dc-4203-a25e-93630819b741"),
                                       entry(PROD, "1432ce2a-5f2e-4560-8b6c-9af87c082d53")));
                put(BORA, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "a228aba6-932b-4f53-b2de-31ad8daf9f8d"),
                                        entry(TEST, "7c41e7cd-f494-4266-9979-48285cbb2434"),
                                        entry(PROD, "2f42a7e8-219b-4289-9b41-99b32a6b4866")));
                put(AHO, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "d6e0ba1c-44fd-49bc-8d79-a45bf1a7b66c"),
                                       entry(TEST, "ba0b80e8-ca87-4b8f-bf2a-6d00188ad707"),
                                       entry(PROD, "c95f4e24-2d79-4135-b9b5-d7ef2bbfb56f")));
                put(IFE, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "d13b4e4e-5dfe-4c92-b6d9-dab1b910cb02"),
                                       entry(TEST, "6c55e437-b373-4875-8505-d0af113aec59"),
                                       entry(PROD, "4c941422-7865-4c8e-a996-b09266854d6c")));
                put(KHIO, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "67e867aa-e925-4697-bad0-c2125c1d6a7c"),
                                        entry(TEST, "130e5373-9673-4d66-a0d3-d22a2874b32f"),
                                        entry(PROD, "5056e51f-0b49-41c6-9dc5-1fa0a5c48e7d")));
                put(LDH, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "367b47d3-8532-47ca-aebf-e3553bea76a8"),
                                       entry(TEST, "2197e9e0-93b0-4051-8044-16ffd0087982"),
                                       entry(PROD, "5fcf63d0-fedd-49c0-8470-6c3037f4a3d9")));
                put(MF, Map.ofEntries(entry(SANDBOX, ""),
                                      entry(DEVELOP, "2287bd5f-7ac9-4ad0-b70f-0ce6575adea3"),
                                      entry(TEST, "577c0a2f-2697-4401-ad94-0a9bf20bdb40"),
                                      entry(PROD, "31665824-ee77-48e8-b508-a0cdbaeca0f4")));
                put(NIFU, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "043ad701-d2c5-46be-b991-c58aef97626d"),
                                        entry(TEST, "f09c78fe-0d6a-4e45-be1c-6fede449cda8"),
                                        entry(PROD, "b9bbbdc4-380b-4c12-9b7d-5d82deeb2e6a")));
                put(NIVA, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "6c94a30a-d4cf-47d7-8de1-7ed3d145f183"),
                                        entry(TEST, "a497cf56-1a26-4de3-b2cc-ec20aa54af10"),
                                        entry(PROD, "260129ec-4f4d-42a2-bcc9-d324e029f0dc")));
                put(PHS, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "a0df7c41-35f8-4080-a86f-4da018572f54"),
                                       entry(TEST, "f20f314d-fa91-4c66-9131-98b6c4fed42e"),
                                       entry(PROD, "1b36e4bf-223c-4da7-9bae-35846106c788")));
                put(STAMI, Map.ofEntries(entry(SANDBOX, ""),
                                         entry(DEVELOP, "838c45e1-8470-490b-b71d-a947b444e220"),
                                         entry(TEST, "959587b0-d49b-49c8-adb4-43910500c2c3"),
                                         entry(PROD, "de807018-361b-4472-b753-cd268a1f35c8")));
                put(STEINERHOYSKOLEN, Map.ofEntries(entry(SANDBOX, ""),
                                                    entry(DEVELOP, "a739d345-a564-4f0e-adec-c8c9ecb09bbb"),
                                                    entry(TEST, "57bb7742-271a-4a17-ac4c-d142cb598edb"),
                                                    entry(PROD, "f25f082e-7522-49bf-a615-c0ea02788ceb")));
                put(VETINST, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "cd13fb16-8f88-4216-bc92-d2e63c9fd393"),
                                           entry(TEST, "2ccab0d9-c238-4fb6-b417-58586cf0ed22"),
                                           entry(PROD, "1987c64c-3830-46c0-90e0-2603162e0d88")));
                put(NASJONALMUSEET, Map.ofEntries(entry(SANDBOX, ""),
                                                  entry(DEVELOP, "bbd9048d-e51b-49f6-b699-fd86ec86acd6"),
                                                  entry(TEST, "63efab04-c807-4efa-8ee9-a2c3ae6f77af"),
                                                  entry(PROD, "7f8dab31-0600-4454-80c0-3b519290f60d")));
                put(UNIT, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "bbd9048d-e51b-49f6-b699-fd86ec86acd6"),
                                        entry(TEST, "ed671c06-964c-46ce-85d5-777c164ac81e"),
                                        entry(PROD, "256b9785-808b-4c2a-a2d9-fd7a2de8da0c")));
                put(STATPED, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "e6890d28-a370-49a3-9100-f78b2e8fb324"),
                                           entry(TEST, "82fa69db-a834-47e2-8d03-048b3f60cbcf"),
                                           entry(PROD, "a63f80aa-d479-4271-8a17-d82ab7d0efd2")));
                put(VEGVESEN, Map.ofEntries(entry(SANDBOX, ""),
                                            entry(DEVELOP, "2c1deeca-c0fc-487e-93c8-9f0cf4fae490"),
                                            entry(TEST, "95100df5-b46b-41c9-a87a-9a2efc58716e"),
                                            entry(PROD, "d4604fb0-ec96-406c-bbf7-7ecf80fbfa69")));
                put(FFI, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "04939e4d-105e-4d3c-b945-7d482d4f18d5"),
                                       entry(TEST, "b11f0a40-ac27-42b9-ba94-f4952e3d5a5a"),
                                       entry(PROD, "eb732391-19ec-4d9f-9cb3-a77c8876a18f")));
                put(NB, Map.ofEntries(entry(SANDBOX, ""),
                                      entry(DEVELOP, "9fde89c7-baea-4ac1-ad52-97d4dee0e6da"),
                                      entry(TEST, "155b291c-9da7-482c-833d-e1b1488f9ee3"),
                                      entry(PROD, "20db2e6c-228c-4071-971c-9eb2f9eb133b")));
                put(NFORSK, Map.ofEntries(entry(SANDBOX, ""),
                                          entry(DEVELOP, "5196161e-c952-4b36-9125-3e9746ee338d"),
                                          entry(TEST, "71fb8ede-c372-4f51-a9ec-de1874badaf7"),
                                          entry(PROD, "c3359446-f54d-4755-ad8b-e2403283da84")));
                put(CICERO, Map.ofEntries(entry(SANDBOX, ""),

                                          entry(DEVELOP, "d22e1273-5dd0-410f-a0d9-356353683a98"),

                                          entry(TEST, "47db88b9-3013-4e53-873c-bb0003074ca3"),

                                          entry(PROD, "efb11f03-33fc-4740-9324-91a5628aaf35")));
                put(FNI, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "de17a451-0aec-4beb-a17f-ca6779e4e295"),
                                       entry(TEST, "ad4555b9-8615-4802-93b0-fe84fc4d2971"),
                                       entry(PROD, "d4f65b16-dca9-409b-a6a1-2ff1f1cb6e45")));
                put(NLA, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "53fdf89c-7408-4371-bab9-89812ce34e91"),
                                       entry(TEST, "d1472762-8e09-4897-bcfe-93418fa90448"),
                                       entry(PROD, "b04c8716-865c-4f81-8555-d3e33c2b7ec3")));
                put(NINA, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "afa4fb86-76f6-4339-9133-924d999a792e"),
                                        entry(TEST, "899823c8-7315-4bb7-b734-97e65b43af15"),
                                        entry(PROD, "adb5c9d0-0973-47c9-92db-7323c2b59cb7")));
                put(NMBU, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "8b0609fc-8787-4c71-ba6d-fa3718bc73e1"),
                                        entry(TEST, "b8977f51-28bd-41b5-bb19-71c577be7c28"),
                                        entry(PROD, "83abb85b-768b-4b3d-a096-3fd9442e3d6d")));
                put(NMH, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "2f148e5f-a017-4e7f-8f78-6a52eb6a9938"),
                                       entry(TEST, "9efe66cc-ead0-49e0-b787-b73a188d4770"),
                                       entry(PROD, "eda1b8c8-5e8a-4138-b095-1816a5b38c2e")));
                put(NOFIMA, Map.ofEntries(entry(SANDBOX, ""),
                                          entry(DEVELOP, "15af7bc4-16cd-4134-91ba-68d900a5685c"),
                                          entry(TEST, "dc9f40f0-7397-4237-a54d-cf33b5d236f1"),
                                          entry(PROD, "b5748bde-b32f-4a7e-81cc-901eae44291b")));
                put(NORCERESEARCH, Map.ofEntries(entry(SANDBOX, ""),
                                                 entry(DEVELOP, "d039e7ea-1380-46f1-bb7d-b6456ddc0fc2"),
                                                 entry(TEST, "e58d2162-c4de-4627-8b3f-ac2d9fb49446"),
                                                 entry(PROD, "858d32f1-fb0b-4b5c-a02f-118d8294ffc0")));
                put(NORSKFOLKEMUSEUM, Map.ofEntries(entry(SANDBOX, ""),
                                                    entry(DEVELOP, "890ae660-440f-41cb-9d57-1afa3f5d0444"),
                                                    entry(TEST, "2fcfc7e5-b8d7-4c08-98ef-3927ad934e6f"),
                                                    entry(PROD, "646b979a-1833-4048-9cd3-ae4043a4cba4")));
                put(NPOLAR, Map.ofEntries(entry(SANDBOX, ""),
                                          entry(DEVELOP, "009974e9-217c-46b8-8f61-f7c1501ee04a"),
                                          entry(TEST, "7de3c640-f71e-4837-b46b-32f250acf179"),
                                          entry(PROD, "4bffea43-c3c5-4416-93ed-16b2ccd0dd7e")));
                put(RURALIS, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "f8f52dc0-688c-4d56-b0f6-26e2698e16a0"),
                                           entry(TEST, "37b410b5-d85f-4bb2-8421-c8afc2e5de60"),
                                           entry(PROD, "8ce49301-0615-4d32-a7c9-bc395cefda13")));
                put(SAMAS, Map.ofEntries(entry(SANDBOX, ""),
                                         entry(DEVELOP, "69f64ed3-e2c6-4c0c-a370-0de946ca6568"),
                                         entry(TEST, "b611090b-a768-4d89-a62d-ddd90f19ae22"),
                                         entry(PROD, "ebcf1530-b634-4f7d-b02c-988b029e68d1")));
                put(SAMFUNNSFORSKNING, Map.ofEntries(entry(SANDBOX, ""),
                                                     entry(DEVELOP, "818c957b-65ce-447d-b78c-cd472ea0e65f"),
                                                     entry(TEST, "999f15f5-b0d4-4273-8dc4-97a971454b74"),
                                                     entry(PROD, "b21d784f-dbd7-4d09-bc60-3584bea585e0")));
                put(SIHF, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "73662b0f-4eba-4783-97da-de9e835affb6"),
                                        entry(TEST, "ff990b53-59da-44b0-ba95-ac942a220d12"),
                                        entry(PROD, "9d0ab613-7ddc-4a2d-8d9a-638cfb65898d")));
                put(SINTEF, Map.ofEntries(entry(SANDBOX, ""),
                                          entry(DEVELOP, "11b70ff2-1ba4-4cf1-95f3-06fea953cf2e"),
                                          entry(TEST, "dd70eb42-264a-4538-b77a-e31bb4cf3a99"),
                                          entry(PROD, "668f1338-4026-4668-8ab8-426fd9176abb")));
            put(SSB, Map.ofEntries(entry(SANDBOX, ""),
                                   entry(DEVELOP, "3169a5cb-db8b-44ac-8bab-69d3f4ff9f54"),
                                   entry(TEST, "3f2485a8-ba56-48e2-827b-0a307a44eafb"),
                                   entry(PROD, "8b699c48-85e0-40b3-b686-d91bed21b7ad")));
                put(UIS, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "3e1da1fd-559e-4334-acb9-797684e5bae2"),
                                       entry(TEST, "2d353a69-afa0-44a2-9028-211771d748ae"),
                                       entry(PROD, "48ab46e0-9a2e-4908-b144-25bfcdeb42e9")));
                put(USN, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "702c4fbe-d51a-4d20-aec8-50beb813ff36"),
                                       entry(TEST, "b3d72c51-c4f2-4aa1-a15a-e9cc9b1f5e1e"),
                                       entry(PROD, "72a4dc34-ff98-4d66-a663-7f2eb005555b")));
                put(NIKU, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "11c1a22c-6b1b-4463-b74e-e20cd13d7243"),
                                        entry(TEST, "61f179e2-4c8a-4f92-a285-6244556011f6"),
                                        entry(PROD, "4223e8a3-8ed5-4f02-b47d-b557b7fa482d")));
                put(NILU, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "68e24208-5840-45a1-903c-44ebb3afccc2"),
                                        entry(TEST, "31ac9694-3e2e-4424-bd4c-b03e950335e2"),
                                        entry(PROD, "da2c0478-9ec4-4e1e-8b7d-0a27898fb6de")));
                put(NGU, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "4ff971c0-071b-421e-8566-1c0899f3b26c"),
                                       entry(TEST, "897e5123-d4b5-425a-9872-bfd1c554e30b"),
                                       entry(PROD, "0557ff2c-9658-47e6-8289-c98e4555bb64")));
                put(UIT, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "2d35d439-055d-40e5-94de-a9ec28493835"),
                                       entry(TEST, "acf2bd2e-a721-4f0f-91ca-3c7f46c0e375"),
                                       entry(PROD, "7c277049-48de-4901-bc7e-21768835d461")));
                put(UIO, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "8db4a8bd-3db5-420b-a2e4-fc17a489aa6d"),
                                       entry(TEST, "e4fd5fe5-b073-47a7-bb3e-2516d40335e6"),
                                       entry(PROD, "a4aaa430-c26a-43ba-bbdf-8d102f814559")));
                put(FHI, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "95bfedd6-0a44-4013-8e96-256b0efb7d28"),
                                       entry(TEST, "94fb0975-499d-42be-b729-1d8327a33a76"),
                                       entry(PROD, "edbebb28-cdbf-4070-ab8c-e839317b46f7")));
        put(R_BUP, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "3259519a-1ef6-444d-87d2-534d7c6df021"),
                                 entry(TEST, "458137ea-64bf-438d-a69c-6808d1b06e79"),
                                 entry(PROD, "8b04e93b-5d73-4202-b260-969bdd779ee4")));
        put(IMR, Map.ofEntries(entry(SANDBOX, ""),
                               entry(DEVELOP, "3e78024a-657d-482c-9496-0f34a376ca78"),
                               entry(TEST, "f76cf282-43c0-495a-8f4d-532838994bd1"),
                               entry(PROD, "492c03a9-6c9c-48a9-9259-78b945aefef5")));
            put(TOI, Map.ofEntries(entry(SANDBOX, ""),
                                   entry(DEVELOP, "ff6fd53f-30ff-4c47-955e-f201fe5d9e0c"),
                                   entry(TEST, "d102baef-5861-43e2-bb21-42a1f6479aef"),
                                   entry(PROD, "1c313c5f-3544-48ba-8c09-5e8cb0bea3be")));
        }};

    public static boolean customerExistsInEnvironment(String customerShortName, String environment) {
        return Optional.ofNullable(customerShortName)
                   .map(customer -> CUSTOMER_MAP.get(customer.toLowerCase(Locale.ROOT)))
                   .map(environmentMap -> environmentMap.get(environment))
                   .filter(StringUtils::isNotBlank)
                   .isPresent();
    }
}

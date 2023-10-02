package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final Map<String, Map<String, String>> CUSTOMER_MAP = new ConcurrentHashMap<>()
    {{

            put(NUPI, Map.ofEntries(
                entry(SANDBOX, ""),
                entry(DEVELOP, "8d11f0f0-d414-4564-9250-298c06ef5c87"),
                entry(TEST, "122d33a7-1844-466a-926b-333eeff42a93"),
                entry(PROD, "")));
                put(OMSORGSFORSKNING, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, ""),
                    entry(TEST, ""),
                    entry(PROD, "")));
                put(INN, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "023f1608-1baa-4d5a-8acc-80d329bddd74"),
                    entry(TEST, "df2a796f-3eef-45dc-a39d-51b21989285a"),
                    entry(PROD, "")));
                put(BI, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "a49a2deb-f782-4ab1-b2ab-97745bc14b3d"),
                    entry(TEST, "e9fb0c5d-1589-4d6f-932f-b312e50daa8f"),
                    entry(PROD, "")));
                put(VID, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "58d2d14c-ff23-4aa0-94f1-b53190fd020a"),
                    entry(TEST, "9b17e692-6690-41a2-923b-b7ac0d7a1522"),
                    entry(PROD, "")));
                put(BANENOR, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, ""),
                    entry(TEST, ""),
                    entry(PROD, "")));
                put(RA, Map.ofEntries(
                    entry(SANDBOX, "b3435185-929a-4842-96ee-4243f4c9078c"),
                    entry(DEVELOP, "e974d7d1-50b5-47c0-ad7e-7135a2d47555"),
                    entry(TEST, "434ed8ed-f302-409f-9943-8886320550a3"),
                    entry(PROD, "")));
                put(NORGES_BANK, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "9d02e866-aeba-466d-993a-b519c664d38c"),
                    entry(TEST, "2d37e9a9-dc70-4c30-bdb6-c7724eb7f0c3"),
                    entry(PROD, "")));
                put(NVE, Map.ofEntries(
                    entry(SANDBOX, "5eb7ca32-0e6b-4819-b794-c31a4b16ea6b"),
                    entry(DEVELOP, "b4497570-2903-49a2-9c2a-d6ab8b0eacc2"),
                    entry(TEST, ""),
                    entry(PROD, "4ba5f697-2056-4292-b0a3-f81ccf21ea22")));
                put(KRUS, Map.ofEntries(
                    entry(SANDBOX, "6b5e1238-7a05-11ed-a1eb-0242ac120002"),
                    entry(DEVELOP, "a768727e-4ecb-41c1-a616-1fec000cac1c"),
                    entry(TEST, ""),
                    entry(PROD, "")));
                put(KRISTIANIA, Map.ofEntries(
                    entry(SANDBOX, "8fb3c2f4-da97-4eb1-be65-307c86b993ee"),
                    entry(DEVELOP, "ca57b418-f837-40fd-af5c-5a6ba14abd7e"),
                    entry(TEST, "c1d7b72b-3319-4a1f-a365-c04ddb729b6f"),
                    entry(PROD, "05329411-0aa7-4c68-8cc6-5875f0d58f8c")));
                put(FHS, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "290bb113-cf83-4917-8a07-463b4eca057b"),
                    entry(TEST, "ba0b80e8-ca87-4b8f-bf2a-6d00188ad707"),
                    entry(PROD, "")));
                put(HIOF, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "5c243c2d-a8fb-48fb-a2c6-c6fa70310194"),
                    entry(TEST, "719fdb5a-f90a-4e20-b81d-1dde3ca6e647"),
                    entry(PROD, "")));
                put(NGI, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "f415cb81-ac56-4244-b31b-25e43dc3027e"),
                    entry(TEST, "1f899570-ed06-4c08-bc9f-ed92402e5128"),
                    entry(PROD, "")));
                put(NIBIO, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "82c8b036-39cb-4ac2-8a2d-152cda4bcc27"),
                    entry(TEST, "cd925eea-7f4c-4167-9b7c-a7bf7f8eca59"),
                    entry(PROD, "77766640-6066-48ef-a32c-0ba7f32abee9")));
                put(NIH, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "b5ff15a1-0e58-44bf-b137-d5c5389ef63f"),
                    entry(TEST, "6c7ac184-1b21-425c-a422-2855804be2aa"),
                    entry(PROD, "")));
                put(NTNU, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "8ddecceb-7d64-4df3-a842-489fe4d98f3a"),
                    entry(TEST, "33c17ef6-864b-4267-bc9d-0cee636e247e"),
                    entry(PROD, "")));
                put(NR, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "54bed9d9-2bc4-4fa4-a6c4-9e957f93870d"),
                    entry(TEST, "505328d2-1992-406c-8792-b064748d2f38"),
                    entry(PROD, "")));
                put(SAMFORSK, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "6da57387-b48a-46dc-a395-d07c12b1e54b"),
                    entry(TEST, ""),
                    entry(PROD, "")));
                put(CMI, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "aebc37b9-593c-42b6-88bf-0d8a54492a32"),
                    entry(TEST, ""),
                    entry(PROD, "")));
                put(DMMH, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "0edc1e9a-a74b-4169-86ba-f98b92d0115e"),
                    entry(TEST, "2e4f348c-47a3-4500-94dd-9b4ee4ba63d9"),
                    entry(PROD, "")));
                put(FAFO, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "eab76eb2-3f15-40af-9056-53946ec8fcde"),
                    entry(TEST, "648a3ed0-f34e-48ff-8db9-092b2f5d5fa2"),
                    entry(PROD, "")));
                put(FDIR, Map.ofEntries(
                    entry(SANDBOX, ""),
                    entry(DEVELOP, "4403a49b-a175-4985-a695-f16c5b87c9ac"),
                    entry(TEST, ""),
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
                                       entry(TEST, ""),
                                       entry(PROD, "")));
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
                                       entry(PROD, "")));
                put(IFE, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "d13b4e4e-5dfe-4c92-b6d9-dab1b910cb02"),
                                       entry(TEST, "6c55e437-b373-4875-8505-d0af113aec59"),
                                       entry(PROD, "")));
                put(KHIO, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "67e867aa-e925-4697-bad0-c2125c1d6a7c"),
                                        entry(TEST, "130e5373-9673-4d66-a0d3-d22a2874b32f"),
                                        entry(PROD, "")));
                put(LDH, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "367b47d3-8532-47ca-aebf-e3553bea76a8"),
                                       entry(TEST, "2197e9e0-93b0-4051-8044-16ffd0087982"),
                                       entry(PROD, "")));
                put(MF, Map.ofEntries(entry(SANDBOX, ""),
                                      entry(DEVELOP, "2287bd5f-7ac9-4ad0-b70f-0ce6575adea3"),
                                      entry(TEST, "577c0a2f-2697-4401-ad94-0a9bf20bdb40"),
                                      entry(PROD, "")));
                put(NIFU, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "043ad701-d2c5-46be-b991-c58aef97626d"),
                                        entry(TEST, "f09c78fe-0d6a-4e45-be1c-6fede449cda8"),
                                        entry(PROD, "")));
                put(NIVA, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "6c94a30a-d4cf-47d7-8de1-7ed3d145f183"),
                                        entry(TEST, "a497cf56-1a26-4de3-b2cc-ec20aa54af10"),
                                        entry(PROD, "")));
                put(PHS, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "a0df7c41-35f8-4080-a86f-4da018572f54"),
                                       entry(TEST, "f20f314d-fa91-4c66-9131-98b6c4fed42e"),
                                       entry(PROD, "1b36e4bf-223c-4da7-9bae-35846106c788")));
                put(STAMI, Map.ofEntries(entry(SANDBOX, ""),
                                         entry(DEVELOP, "838c45e1-8470-490b-b71d-a947b444e220"),
                                         entry(TEST, "959587b0-d49b-49c8-adb4-43910500c2c3"),
                                         entry(PROD, "")));
                put(STEINERHOYSKOLEN, Map.ofEntries(entry(SANDBOX, ""),
                                                    entry(DEVELOP, "a739d345-a564-4f0e-adec-c8c9ecb09bbb"),
                                                    entry(TEST, "57bb7742-271a-4a17-ac4c-d142cb598edb"),
                                                    entry(PROD, "")));
                put(VETINST, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "cd13fb16-8f88-4216-bc92-d2e63c9fd393"),
                                           entry(TEST, "2ccab0d9-c238-4fb6-b417-58586cf0ed22"),
                                           entry(PROD, "1987c64c-3830-46c0-90e0-2603162e0d88")));
                put(NASJONALMUSEET, Map.ofEntries(entry(SANDBOX, ""),
                                                  entry(DEVELOP, "bbd9048d-e51b-49f6-b699-fd86ec86acd6"),
                                                  entry(TEST, "63efab04-c807-4efa-8ee9-a2c3ae6f77af"),
                                                  entry(PROD, "")));
                put(UNIT, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "bbd9048d-e51b-49f6-b699-fd86ec86acd6"),
                                        entry(TEST, "ed671c06-964c-46ce-85d5-777c164ac81e"),
                                        entry(PROD, "")));
                put(STATPED, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "e6890d28-a370-49a3-9100-f78b2e8fb324"),
                                           entry(TEST, "82fa69db-a834-47e2-8d03-048b3f60cbcf"),
                                           entry(PROD, "a63f80aa-d479-4271-8a17-d82ab7d0efd2")));
                put(VEGVESEN, Map.ofEntries(entry(SANDBOX, ""),
                                            entry(DEVELOP, "2c1deeca-c0fc-487e-93c8-9f0cf4fae490"),
                                            entry(TEST, "95100df5-b46b-41c9-a87a-9a2efc58716e"),
                                            entry(PROD, "")));
                put(FFI, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "04939e4d-105e-4d3c-b945-7d482d4f18d5"),
                                       entry(TEST, ""),
                                       entry(PROD, "")));
                put(NB, Map.ofEntries(entry(SANDBOX, ""),
                                      entry(DEVELOP, "9fde89c7-baea-4ac1-ad52-97d4dee0e6da"),
                                      entry(TEST, ""),
                                      entry(PROD, "")));
                put(NFORSK, Map.ofEntries(entry(SANDBOX, ""),
                                          entry(DEVELOP, "5196161e-c952-4b36-9125-3e9746ee338d"),
                                          entry(TEST, ""),
                                          entry(PROD, "")));
                put(CICERO, Map.ofEntries(entry(SANDBOX, ""),

                                          entry(DEVELOP, "d22e1273-5dd0-410f-a0d9-356353683a98"),

                                          entry(TEST, "47db88b9-3013-4e53-873c-bb0003074ca3"),

                                          entry(PROD, "")));
                put(FNI, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "de17a451-0aec-4beb-a17f-ca6779e4e295"),
                                       entry(TEST, ""),
                                       entry(PROD, "")));
                put(NLA, Map.ofEntries(entry(SANDBOX, ""),

                                       entry(DEVELOP, "53fdf89c-7408-4371-bab9-89812ce34e91"),

                                       entry(TEST, ""),

                                       entry(PROD, "")));
                put(NINA, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "afa4fb86-76f6-4339-9133-924d999a792e"),
                                        entry(TEST, ""),
                                        entry(PROD, "")));
                put(NMBU, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "8b0609fc-8787-4c71-ba6d-fa3718bc73e1"),
                                        entry(TEST, ""),
                                        entry(PROD, "")));
                put(NMH, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "2f148e5f-a017-4e7f-8f78-6a52eb6a9938"),
                                       entry(TEST, ""),
                                       entry(PROD, "")));
                put(NOFIMA, Map.ofEntries(entry(SANDBOX, ""),
                                          entry(DEVELOP, "15af7bc4-16cd-4134-91ba-68d900a5685c"),
                                          entry(TEST, ""),
                                          entry(PROD, "")));
                put(NORCERESEARCH, Map.ofEntries(entry(SANDBOX, ""),
                                                 entry(DEVELOP, "d039e7ea-1380-46f1-bb7d-b6456ddc0fc2"),
                                                 entry(TEST, ""),
                                                 entry(PROD, "")));
                put(NORSKFOLKEMUSEUM, Map.ofEntries(entry(SANDBOX, ""),
                                                    entry(DEVELOP, "890ae660-440f-41cb-9d57-1afa3f5d0444"),
                                                    entry(TEST, ""),
                                                    entry(PROD, "")));
                put(NPOLAR, Map.ofEntries(entry(SANDBOX, ""),
                                          entry(DEVELOP, "009974e9-217c-46b8-8f61-f7c1501ee04a"),
                                          entry(TEST, ""),
                                          entry(PROD, "")));
                put(RURALIS, Map.ofEntries(entry(SANDBOX, ""),
                                           entry(DEVELOP, "f8f52dc0-688c-4d56-b0f6-26e2698e16a0"),
                                           entry(TEST, ""),
                                           entry(PROD, "")));
                put(SAMAS, Map.ofEntries(entry(SANDBOX, ""),
                                         entry(DEVELOP, "69f64ed3-e2c6-4c0c-a370-0de946ca6568"),
                                         entry(TEST, "b611090b-a768-4d89-a62d-ddd90f19ae22"),
                                         entry(PROD, "")));
                put(SAMFUNNSFORSKNING, Map.ofEntries(entry(SANDBOX, ""),
                                                     entry(DEVELOP, "818c957b-65ce-447d-b78c-cd472ea0e65f"),
                                                     entry(TEST, ""),
                                                     entry(PROD, "")));
                put(SIHF, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "73662b0f-4eba-4783-97da-de9e835affb6"),
                                        entry(TEST, "ff990b53-59da-44b0-ba95-ac942a220d12"),
                                        entry(PROD, "")));
                put(SINTEF, Map.ofEntries(entry(SANDBOX, ""),
                                          entry(DEVELOP, "11b70ff2-1ba4-4cf1-95f3-06fea953cf2e"),
                                          entry(TEST, "dd70eb42-264a-4538-b77a-e31bb4cf3a99"),
                                          entry(PROD, "")));
            put(SSB, Map.ofEntries(entry(SANDBOX, ""),
                                   entry(DEVELOP, "3169a5cb-db8b-44ac-8bab-69d3f4ff9f54"),
                                   entry(TEST, "3f2485a8-ba56-48e2-827b-0a307a44eafb"),
                                   entry(PROD, "")));
                put(UIS, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "3e1da1fd-559e-4334-acb9-797684e5bae2"),
                                       entry(TEST, "2d353a69-afa0-44a2-9028-211771d748ae"),
                                       entry(PROD, "")));
                put(USN, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "702c4fbe-d51a-4d20-aec8-50beb813ff36"),
                                       entry(TEST, ""),
                                       entry(PROD, "")));
                put(NIKU, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "11c1a22c-6b1b-4463-b74e-e20cd13d7243"),
                                        entry(TEST, ""),
                                        entry(PROD, "")));
                put(NILU, Map.ofEntries(entry(SANDBOX, ""),
                                        entry(DEVELOP, "68e24208-5840-45a1-903c-44ebb3afccc2"),
                                        entry(TEST, "31ac9694-3e2e-4424-bd4c-b03e950335e2"),
                                        entry(PROD, "")));
                put(NGU, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "4ff971c0-071b-421e-8566-1c0899f3b26c"),
                                       entry(TEST, ""),
                                       entry(PROD, "")));
                put(UIT, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "2d35d439-055d-40e5-94de-a9ec28493835"),
                                       entry(TEST, "acf2bd2e-a721-4f0f-91ca-3c7f46c0e375"),
                                       entry(PROD, "")));
                put(UIO, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "8db4a8bd-3db5-420b-a2e4-fc17a489aa6d"),
                                       entry(TEST, ""),
                                       entry(PROD, "")));
                put(FHI, Map.ofEntries(entry(SANDBOX, ""),
                                       entry(DEVELOP, "95bfedd6-0a44-4013-8e96-256b0efb7d28"),
                                       entry(TEST, ""),
                                       entry(PROD, "")));
        put(R_BUP, Map.ofEntries(entry(SANDBOX, ""),
                                 entry(DEVELOP, "3259519a-1ef6-444d-87d2-534d7c6df021"),
                                 entry(TEST, ""),
                                 entry(PROD, "")));
        }};

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

package no.sikt.nva.scrapers;

import static java.util.Map.entry;
import static java.util.Objects.nonNull;
import java.util.List;
import java.util.Map;

public final class PublisherMapper {

    public static final String NVE_BRAGE_PUBLISHER = "NVE";
    public static final String NVE_CHANNEL_REGISTER_PUBLISHER = "Norges vassdrags- og energidirektorat";
    public static final String KRUS_BRAGE_PUBLISHER_V1 = "Kriminalomsorgens utdanningssenter";
    public static final String KRUS_BRAGE_PUBLISHER_V2 = "KRUS";
    public static final String KRUS_BRAGE_PUBLISHER_V3 = "Kriminalomsorgens høgskole og utdanningssenter KRUS";
    public static final String KRUS_BRAGE_PUBLISHER_V4 = "Kriminalomsorgens utdanningssenter KRUS";
    public static final String KRUS_BRAGE_PUBLISHER_V5 = "Kriminalomsorgen";
    public static final String KRUS_BRAGE_PUBLISHER_V6 = "Direktoratet for Kriminalforsorgen";
    public static final String KRUS_CHANNEL_REGISTER_PUBLISHER = "Kriminalomsorgens høgskole og utdanningssenter KRUS";
    public static final String FHS_BRAGE_PUBLISHER_V1 = "Forsvarsstaben";
    public static final String FHS_BRAGE_PUBLISHER_V2 = "Royal Norwegian Naval Academy";
    public static final String FHS_BRAGE_PUBLISHER_V3 = "Forsvarets stabsskole";
    public static final String FHS_BRAGE_PUBLISHER_V4 = "Forsvarets høgskole";
    public static final String FHS_BRAGE_PUBLISHER_V5 = "Luftkrigsskolen";
    public static final String FHS_BRAGE_PUBLISHER_V6 = "Forsvarets overkommando; utarbeidet ved Forsvarets stabsskole";
    public static final String FHS_BRAGE_PUBLISHER_V7 = "MCDC. Multinational Capability Development Campaign";
    public static final String FHS_BRAGE_PUBLISHER_V8 = "Forsvarssjefen. Utarbeidet av Forsvarets høgskole/Forsvarets"
                                                        + " stabsskole";
    public static final String FHS_BRAGE_PUBLISHER_V9 = "Orkana Forlag";
    public static final String FHS_BRAGE_PUBLISHER_V10 = "Forsvarsstaben. Utarbeidet av Forsvarets stabsskole. Denne "
                                                         + "versjonen er erstattet av en ny utgave, utgitt 1. oktober "
                                                         + "2014.";
    public static final String FHS_BRAGE_PUBLISHER_V11 = "Institutt for forsvarsstudier";
    public static final String FHS_BRAGE_PUBLISHER_V12 = "Sjøkrigsskolen";
    public static final String FHS_BRAGE_PUBLISHER_V13 = "Norwegian Institute for Defence Studies";
    public static final String FHS_BRAGE_PUBLISHER_V14 = "Luftled - luftmilitært tidsskrift";
    public static final String FHS_BRAGE_PUBLISHER_V15 = "Forsvarets høgskole - Luftkrigsskolen";
    public static final String FHS_BRAGE_PUBLISHER_V16 = "Forsvarets høgskole Luftkrigsskolen";
    public static final String FHS_BRAGE_PUBLISHER_V17 = "Norsk utenrikspolitisk institutt";
    public static final String FHS_BRAGE_PUBLISHER_V18 = "Den norske atlanterhavskomitee";
    public static final String FHS_BRAGE_PUBLISHER_V19 = "Forsvarets stabsskole/Forsvarets høgskole";
    public static final String FHS_BRAGE_PUBLISHER_V20 = "Forsvarets Høgskole";
    public static final String FHS_CHANNEL_REGISTER_PUBLISHER = "Försvarshögskolan (FHS)";

    private static final Map<List<String>, String> PUBLISHER_MAP = Map.ofEntries(
        entry(List.of(NVE_BRAGE_PUBLISHER), NVE_CHANNEL_REGISTER_PUBLISHER),
        entry(List.of(KRUS_BRAGE_PUBLISHER_V1, KRUS_BRAGE_PUBLISHER_V2, KRUS_BRAGE_PUBLISHER_V3,
                      KRUS_BRAGE_PUBLISHER_V4, KRUS_BRAGE_PUBLISHER_V5, KRUS_BRAGE_PUBLISHER_V6,
                      KRUS_CHANNEL_REGISTER_PUBLISHER), KRUS_CHANNEL_REGISTER_PUBLISHER),
        entry(List.of(FHS_BRAGE_PUBLISHER_V1, FHS_BRAGE_PUBLISHER_V2, FHS_BRAGE_PUBLISHER_V3, FHS_BRAGE_PUBLISHER_V4,
                      FHS_BRAGE_PUBLISHER_V5, FHS_BRAGE_PUBLISHER_V6, FHS_BRAGE_PUBLISHER_V7, FHS_BRAGE_PUBLISHER_V8,
                      FHS_BRAGE_PUBLISHER_V9, FHS_BRAGE_PUBLISHER_V10, FHS_BRAGE_PUBLISHER_V11,
                      FHS_BRAGE_PUBLISHER_V12, FHS_BRAGE_PUBLISHER_V13, FHS_BRAGE_PUBLISHER_V14,
                      FHS_BRAGE_PUBLISHER_V15, FHS_BRAGE_PUBLISHER_V16, FHS_BRAGE_PUBLISHER_V17,
                      FHS_BRAGE_PUBLISHER_V18, FHS_CHANNEL_REGISTER_PUBLISHER, FHS_BRAGE_PUBLISHER_V19,
                      FHS_BRAGE_PUBLISHER_V20), FHS_CHANNEL_REGISTER_PUBLISHER));

    public static String getMappablePublisher(String publisher) {
        if (nonNull(publisher) && !publisher.isEmpty()) {
            for (List<String> keys : PUBLISHER_MAP.keySet()) {
                if (keys.contains(publisher)) {
                    return PUBLISHER_MAP.get(keys);
                }
            }
        }
        return null;
    }
}

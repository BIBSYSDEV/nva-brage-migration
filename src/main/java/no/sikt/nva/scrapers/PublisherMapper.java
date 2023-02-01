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
    public static final String KRUS_CHANNEL_REGISTER_PUBLISHER = "Kriminalomsorgens høgskole og utdanningssenter KRUS";
    public static final String FDIR_CHANNEL_REGISTER_PUBLISHER = "Fiskeridirektoratet";
    public static final String FDIR_BRAGE_PUBLISHER = "Directorate of fisheries";
    private static final Map<List<String>, String> PUBLISHER_MAP = Map.ofEntries(
        entry(List.of(NVE_BRAGE_PUBLISHER), NVE_CHANNEL_REGISTER_PUBLISHER),
        entry(List.of(KRUS_BRAGE_PUBLISHER_V1, KRUS_BRAGE_PUBLISHER_V2, KRUS_BRAGE_PUBLISHER_V3,
                      KRUS_BRAGE_PUBLISHER_V4, KRUS_BRAGE_PUBLISHER_V5,
                      KRUS_CHANNEL_REGISTER_PUBLISHER), KRUS_CHANNEL_REGISTER_PUBLISHER),
        entry(List.of(FDIR_BRAGE_PUBLISHER), FDIR_CHANNEL_REGISTER_PUBLISHER)
    );

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

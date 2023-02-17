package no.sikt.nva.scrapers;

import java.util.Map;

public class Mapper {

    public final static Map<String, String> HARDCODED_VALUES_MAPPER = Map.of(
        "1502-8190", "1502-8143");


    public static String mapToHardcodedValue(String value) {
        return HARDCODED_VALUES_MAPPER.getOrDefault(value, value);
    }
}

package no.sikt.nva.scrapers;

import java.util.Map;
import java.util.Optional;

public class AccessCodeMapper {

    public static final Map<String, String> ACCESS_CODES = Map.of(
    "Tilgangskode/Access code A", "Dette dokumentet er ikke elektronisk tilgjengelig etter ønske fra forfatter",
    "Tilgangskode/Access code B", "Kun forskere og studenter kan få innsyn i dokumentet",
    "Tilgangskode/Access code C", "Dokumentet er klausulert grunnet lovpålagt taushetsplikt",
    "Tilgangskode/Access code D", "Klausulert: Kan bare siteres etter nærmere avtale med forfatter",
    "Tilgangskode/Access code E", "Klausulert: Kan bare tillates lest etter nærmere avtale med forfatter"
    );

    public static String toAccessCode(String value) {
        var accessCodeMatch = findMatchingAccessCode(value);
        if (accessCodeMatch.isPresent()) {
            return ACCESS_CODES.get(accessCodeMatch.get());
        } else {
            return value;
        }
    }

    private static Optional<String> findMatchingAccessCode(String value) {
        return ACCESS_CODES.keySet().stream().filter(value::contains).findFirst();
    }
}

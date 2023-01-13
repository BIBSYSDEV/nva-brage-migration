package no.sikt.nva.scrapers;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Affiliation;

public final class AffiliationsScraper {

    public static List<Affiliation> getAffiliations(File file) {
        try {
            var string = Files.readString(file.toPath());
            return convertStringToAffiliations(string);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static List<Affiliation> convertStringToAffiliations(String string) {
        List<String> list = Arrays.asList(string.split("\n"));
        return list.stream()
                   .map(AffiliationsScraper::toAffiliation)
                   .collect(Collectors.toList());
    }

    private static Affiliation toAffiliation(String string) {
        var affiliationValues = string.split(";");
        return new Affiliation(getIdentifier(affiliationValues),
                               getName(affiliationValues),
                               getHandle(affiliationValues));
    }

    private static String getIdentifier(String... affiliationValues) {
        return affiliationValues[3].trim();
    }

    private static String getName(String... affiliationValues) {
        return affiliationValues[1];
    }

    private static String getHandle(String... affiliationValues) {
        return affiliationValues[2].trim();
    }
}

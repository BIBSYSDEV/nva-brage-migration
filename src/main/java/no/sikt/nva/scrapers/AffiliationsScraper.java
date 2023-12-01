package no.sikt.nva.scrapers;

import static no.sikt.nva.BrageMigrationCommand.logger;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Affiliation;

public final class AffiliationsScraper {

    public static final String AFFILIATIONS_NUMBER_MESSAGE =
        "Provided affiliations file contains following number of affiliations {}";

    public static Map<String, Affiliation> getAffiliations(File file) {
        try {
            var string = Files.readString(file.toPath());
            var affiliations = convertStringToAffiliations(string);
            logger.info(AFFILIATIONS_NUMBER_MESSAGE, affiliations.size());
            return affiliations;
        } catch (Exception e) {
            logger.info("Affiliations failed: {}", e.getMessage());
            return Map.of();
        }
    }

    private static Map<String, Affiliation> convertStringToAffiliations(String string) {
        List<String> list = Arrays.asList(string.split("\n"));
        return list.stream()
                   .map(AffiliationsScraper::toAffiliation)
                   .collect(Collectors.toMap(Affiliation::getHandle,
                                             affiliation -> affiliation));
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

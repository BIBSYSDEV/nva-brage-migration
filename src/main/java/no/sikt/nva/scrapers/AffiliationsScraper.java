package no.sikt.nva.scrapers;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.aws.ColoredLogger;
import no.sikt.nva.brage.migration.common.model.record.Affiliation;

public final class AffiliationsScraper {

    public static final String NUMBER_OF_AFFILIATIONS_MESSAGE =
        "Provided affiliations file contains following number of affiliations: ";
    public static final String TYPE_TO_APPLY_ON_MESSAGE = "Affiliations will be applied on following types: ";
    private static final ColoredLogger logger = ColoredLogger.create(AffiliationsScraper.class);
    public static final String LINE_BREAK = "\n";
    public static final String REGEX_COLON = ";";
    public static final int ROW_WITH_TYPES = 0;
    public static final String COULD_NOT_READ_AFFILIATIONS_MESSAGE = "Could not read affiliations file, affiliations will not be applied!";
    public static final String COULD_NOT_READ_AFFILIATIONS_FILE_MESSAGE = "Could not read affiliations file!";

    public static AffiliationType getAffiliations(File file) {
        try {
            if (file.exists()) {
                var string = Files.readString(file.toPath());
                var affiliations = convertStringToAffiliations(string);
                var types = Arrays.asList(string.split(LINE_BREAK)[0].split(REGEX_COLON));
                logger.info(NUMBER_OF_AFFILIATIONS_MESSAGE + affiliations.size());
                logger.info(TYPE_TO_APPLY_ON_MESSAGE + Arrays.toString(types.toArray()));
                return new AffiliationType(affiliations, types);
            } else {
                return new AffiliationType(Map.of(), List.of());
            }
        } catch (Exception e) {
            logger.error(COULD_NOT_READ_AFFILIATIONS_MESSAGE);
            logger.error("Exception" + e);
            throw new RuntimeException(COULD_NOT_READ_AFFILIATIONS_FILE_MESSAGE);
        }
    }

    private static Map<String, Affiliation> convertStringToAffiliations(String string) {
        List<String> list = new LinkedList<>(Arrays.asList(string.split(LINE_BREAK)));
        removeTypes(list);
        return list.stream()
                   .map(AffiliationsScraper::toAffiliation)
                   .collect(Collectors.toMap(Affiliation::getHandle,
                                             affiliation -> affiliation));
    }

    private static String removeTypes(List<String> list) {
        return list.remove(ROW_WITH_TYPES);
    }

    private static Affiliation toAffiliation(String string) {
        var affiliationValues = string.split(REGEX_COLON);
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

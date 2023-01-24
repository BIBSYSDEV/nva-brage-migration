package no.sikt.nva.scrapers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Affiliation;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Identity;
import no.sikt.nva.exceptions.ContributorExtractorException;
import nva.commons.core.StringUtils;

public class ContributorScraper {

    public static final String COULD_NOT_EXTRACT_CONTRIBUTORS = "Could not extract contributors";
    public static final String CONTRIBUTOR_FILE_ATTRIBUTOR_DELIMITER = ";";

    //contributor file comes in the format name. surname;cristin_identifier;affiliation

    public static Map<String, Contributor> getContributors(File file) {
        try {
            var string = Files.readString(file.toPath());
            return convertStringToContributors(string);
        } catch (ContributorExtractorException | IOException e) {
            throw new ContributorExtractorException(COULD_NOT_EXTRACT_CONTRIBUTORS, e);
        }
    }

    private static Map<String, Contributor> convertStringToContributors(String string) {
        List<String> list = Arrays.asList(string.split("\n"));
        return list.stream()
                   .map(ContributorScraper::toContributor)
                   .collect(Collectors.toMap(contributor -> contributor.getIdentity().getName(),
                                             contributor -> contributor));
    }

    private static Contributor toContributor(String line) {
        var contributorValues = line.split(CONTRIBUTOR_FILE_ATTRIBUTOR_DELIMITER);
        var name = getContributorNameFromLine(contributorValues);
        var contributorIdentifier = getContributorCristinIdentifierFromLine(contributorValues);
        var affiliationIdentifier = getAffiliationCristinIdentifierFromLine(contributorValues);
        return new Contributor(getIdentity(name, contributorIdentifier), null, null,
                               getAffiliationsIfAffiliationHascristinIdentifier(affiliationIdentifier));
    }

    private static List<Affiliation> getAffiliationsIfAffiliationHascristinIdentifier(String affiliationIdentifier) {
        return hasCristinAffiliation(affiliationIdentifier)
                   ? List.of(
            new Affiliation(extractAffiliationIdentifier(affiliationIdentifier).orElse(null), null, null))
                   : List.of();
    }

    private static boolean hasCristinAffiliation(String affiliationIdentifier) {
        return extractAffiliationIdentifier(affiliationIdentifier).isPresent();
    }

    private static Optional<String> extractAffiliationIdentifier(String affiliationIdentifier) {
        var identifier = affiliationIdentifier.replaceAll(StringUtils.SPACE, StringUtils.EMPTY_STRING)
                             .replaceAll("\r", StringUtils.EMPTY_STRING).trim();
        if (StringUtils.isEmpty(identifier)) {
            return Optional.empty();
        } else {
            return Optional.of(identifier);
        }
    }

    private static String getContributorNameFromLine(String... contributorValues) {
        return contributorValues[0];
    }

    private static String getContributorCristinIdentifierFromLine(String... contributorValues) {
        return contributorValues[1];
    }

    private static String getAffiliationCristinIdentifierFromLine(String... contributorValues) {
        return contributorValues[2];
    }

    private static Identity getIdentity(String name, String cristinIdentifier) {
        return new Identity(name, getContributorCristinIdentifier(cristinIdentifier).orElse(null));
    }

    private static Optional<String> getContributorCristinIdentifier(String cristinIdentifier) {
        if (StringUtils.isEmpty(cristinIdentifier)) {
            return Optional.empty();
        } else {
            return Optional.of(cristinIdentifier);
        }
    }
}

package no.sikt.nva.scrapers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Affiliation;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.Identity;
import no.sikt.nva.exceptions.ContributorExtractorException;
import nva.commons.core.StringUtils;

public class ContributorScraper {

    public static final String COULD_NOT_EXTRACT_CONTRIBUTORS = "Could not extract contributors";

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

    private static Contributor toContributor(String string) {
        var contributorValues = string.split(";");
        return new Contributor(getIdentity(contributorValues), null, null, getAffiliations(contributorValues));
    }

    private static List<Affiliation> getAffiliations(String... contributorValues) {
        return List.of(new Affiliation(extractAffiliationIdentifier(contributorValues), null, null));
    }

    private static String extractAffiliationIdentifier(String... contributorValues) {
        return contributorValues[2].replaceAll(StringUtils.SPACE, StringUtils.EMPTY_STRING)
                   .replaceAll("\r", StringUtils.EMPTY_STRING);
    }

    private static Identity getIdentity(String... contributorValues) {
        return new Identity(contributorValues[0], contributorValues[1]);
    }
}
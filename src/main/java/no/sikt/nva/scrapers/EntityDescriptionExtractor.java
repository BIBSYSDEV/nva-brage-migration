package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.EntityDescription;
import no.sikt.nva.brage.migration.common.model.record.Identity;
import no.sikt.nva.brage.migration.common.model.record.PublicationDate;
import no.sikt.nva.brage.migration.common.model.record.PublicationDateNva;
import no.sikt.nva.brage.migration.common.model.record.PublicationDateNva.Builder;
import no.sikt.nva.brage.migration.common.model.record.PublicationInstance;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.validators.DublinCoreValidator;

public final class EntityDescriptionExtractor {

    public static final String FIRST_DAY_OF_A_MONTH = "01";
    public static final String ADVISOR = "Advisor";
    public static final String AUTHOR = "Creator";
    public static final String EDITOR = "Editor";
    public static final String ILLUSTRATOR = "Illustrator";
    public static final String OTHER_CONTRIBUTOR = "Other";
    public static final String DATE_DELIMITER = "[-.]";
    public static final int LOCAL_DATE_MAX_LENGTH = 12;

    private EntityDescriptionExtractor() {

    }

    public static String extractMainTitle(DublinCore dublinCore) {
        var titles = dublinCore.getDcValues()
                         .stream()
                         .filter(DcValue::isMainTitle)
                         .map(DcValue::scrapeValueAndSetToScraped)
                         .collect(Collectors.toList());
        return titles.isEmpty() ? null : titles.get(0);
    }

    public static EntityDescription extractEntityDescription(DublinCore dublinCore) {
        var entityDescription = new EntityDescription();
        entityDescription.setAbstracts(extractAbstracts(dublinCore));
        entityDescription.setDescriptions(extractDescriptions(dublinCore));
        entityDescription.setMainTitle(extractMainTitle(dublinCore));
        entityDescription.setAlternativeTitles(extractAlternativeTitles(dublinCore));
        entityDescription.setContributors(extractContributors(dublinCore));
        entityDescription.setTags(SubjectScraper.extractTags(dublinCore));
        entityDescription.setPublicationInstance(extractPublicationInstance(dublinCore));
        entityDescription.setPublicationDate(extractPublicationDate(dublinCore));
        entityDescription.setLanguage(BrageNvaLanguageMapper.extractLanguage(dublinCore));
        return entityDescription;
    }

    public static List<String> extractAlternativeTitles(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isAlternativeTitle)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    public static List<Contributor> extractContributors(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isContributor)
                   .map(EntityDescriptionExtractor::createContributorFromDcValue)
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    public static String extractIssue(DublinCore dublinCore) {
        var issues = dublinCore.getDcValues().stream()
                         .filter(DcValue::isIssue)
                         .map(DcValue::scrapeValueAndSetToScraped)
                         .collect(Collectors.toList());
        return !issues.isEmpty() ? issues.get(0) : null;
    }

    private static String modifyIfDateIsOfLocalDateTimeFormat(String date) {
        try {
            if (date.length() > LOCAL_DATE_MAX_LENGTH) {
                var instant = Instant.parse(date);
                return LocalDate.ofInstant(instant, ZoneOffset.UTC).toString();
            } else {
                return date;
            }
        } catch (Exception e) {
            return date;
        }
    }

    private static PublicationDate extractPublicationDate(DublinCore dublinCore) {
        var date = dublinCore.getDcValues().stream()
                       .filter(DcValue::isPublicationDate)
                       .findAny()
                       .map(DcValue::scrapeValueAndSetToScraped)
                       .map(EntityDescriptionExtractor::modifyIfDateIsOfLocalDateTimeFormat)
                       .orElse(null);

        if (isNull(date) || isEmptyString(date)) {
            return new PublicationDate(null, new PublicationDateNva.Builder().build());
        }
        if (DublinCoreValidator.containsYearOnly(date)) {
            var publicationDateNva = new Builder().withYear(date).build();
            return new PublicationDate(date, publicationDateNva);
        }
        if (DublinCoreValidator.containsYearAndMonth(date)) {
            var publicationDateNva = new Builder()
                                         .withYear(date.split(DATE_DELIMITER)[0])
                                         .withMonth(date.split(DATE_DELIMITER)[1])
                                         .withDay(FIRST_DAY_OF_A_MONTH).build();
            return new PublicationDate(date, publicationDateNva);
        }
        var publicationDateNva = new Builder()
                                     .withYear(date.split(DATE_DELIMITER)[0])
                                     .withMonth(date.split(DATE_DELIMITER)[1])
                                     .withDay(date.split(DATE_DELIMITER)[2]).build();
        return new PublicationDate(date, publicationDateNva);
    }

    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    private static boolean isEmptyString(String date) {
        return date.trim().isEmpty();
    }

    private static List<String> extractAbstracts(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isAbstract)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static String extractArticleNumber(DublinCore dublinCore) {
        var articleNumbers = dublinCore.getDcValues()
                                 .stream()
                                 .filter(DcValue::isArticleNumber)
                                 .map(DcValue::scrapeValueAndSetToScraped)
                                 .collect(Collectors.toList());
        if (articleNumbers.isEmpty()) {
            return null;
        } else {
            return articleNumbers.get(0);
        }
    }

    private static List<String> extractDescriptions(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isDescription)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static PublicationInstance extractPublicationInstance(DublinCore dublinCore) {
        var publicationInstance = new PublicationInstance();
        publicationInstance.setIssue(extractIssue(dublinCore));
        publicationInstance.setPageNumber(PageConverter.extractPages(dublinCore));
        publicationInstance.setVolume(extractVolume(dublinCore));
        publicationInstance.setArticleNumber(extractArticleNumber(dublinCore));
        return publicationInstance;
    }

    private static String extractVolume(DublinCore dublinCore) {
        var volumes = dublinCore.getDcValues().stream()
                          .filter(DcValue::isVolume)
                          .map(DcValue::scrapeValueAndSetToScraped)
                          .collect(Collectors.toList());
        return volumes.isEmpty() ? null : volumes.get(0);
    }

    private static Optional<Contributor> createContributorFromDcValue(DcValue dcValue) {
        Identity identity = new Identity(dcValue.scrapeValueAndSetToScraped());
        if (isNull(identity.getName()) || identity.getName().isEmpty()) {
            return Optional.empty();
        }
        String brageRole = dcValue.getQualifier().getValue();
        if (dcValue.isAuthor()) {
            return Optional.of(new Contributor(identity, AUTHOR, brageRole, List.of()));
        }
        if (dcValue.isAdvisor()) {
            return Optional.of(new Contributor(identity, ADVISOR, brageRole, List.of()));
        }
        if (dcValue.isEditor()) {
            return Optional.of(new Contributor(identity, EDITOR, brageRole, List.of()));
        }
        if (dcValue.isIllustrator()) {
            return Optional.of(new Contributor(identity, ILLUSTRATOR, brageRole, List.of()));
        }
        if (dcValue.isOtherContributor()) {
            return Optional.of(new Contributor(identity, OTHER_CONTRIBUTOR, brageRole, List.of()));
        }
        return Optional.empty();
    }
}

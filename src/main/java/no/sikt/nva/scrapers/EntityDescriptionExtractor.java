package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.record.PublisherVersion.isSupportedPublisherVersion;
import static no.sikt.nva.model.dublincore.Qualifier.NONE;
import static no.sikt.nva.model.dublincore.Qualifier.ORCID;
import static no.sikt.nva.scrapers.DublinCoreScraper.isSingleton;
import static no.sikt.nva.validators.DublinCoreValidator.DASH;
import static no.sikt.nva.validators.DublinCoreValidator.HYPHEN;
import static no.sikt.nva.validators.DublinCoreValidator.containsYearOnly;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Contributor;
import no.sikt.nva.brage.migration.common.model.record.EntityDescription;
import no.sikt.nva.brage.migration.common.model.record.Identity;
import no.sikt.nva.brage.migration.common.model.record.PublicationDate;
import no.sikt.nva.brage.migration.common.model.record.PublicationDateNva;
import no.sikt.nva.brage.migration.common.model.record.PublicationDateNva.Builder;
import no.sikt.nva.brage.migration.common.model.record.PublicationInstance;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Qualifier;
import no.sikt.nva.validators.DublinCoreValidator;
import nva.commons.core.StringUtils;

@SuppressWarnings("PMD.GodClass")
public final class EntityDescriptionExtractor {

    public static final String FIRST_DAY_OF_A_MONTH = "01";
    public static final String SUPERVISOR = "Supervisor";
    public static final String AUTHOR = "Creator";
    public static final String EDITOR = "Editor";
    public static final String DATA_COLLECTOR = "DataCollector";
    public static final String ILLUSTRATOR = "Illustrator";
    public static final String OTHER_CONTRIBUTOR = "Other";
    public static final String DATE_DELIMITER = "[-.]";
    public static final int LOCAL_DATE_MAX_LENGTH = 12;

    private EntityDescriptionExtractor() {
    }

    public static String extractMainTitle(DublinCore dublinCore) {
        var titles = extreactMainTitles(dublinCore);
        return titles.isEmpty() ? null : titles.get(0);
    }

    public static List<String> extractAlternativeTitles(DublinCore dublinCore) {
        var alternativeTitles = dublinCore.getDcValues()
                                    .stream()
                                    .filter(DcValue::isAlternativeTitle)
                                    .map(DcValue::scrapeValueAndSetToScraped)
                                    .distinct()
                                    .collect(Collectors.toList());

        var mainTitles = extreactMainTitles(dublinCore);
        if (hasMoreThanTwoValues(mainTitles)) {
            alternativeTitles.add(mainTitles.get(1));
            return alternativeTitles;
        }
        return alternativeTitles;
    }

    public static EntityDescription extractEntityDescription(DublinCore dublinCore,
                                                             Map<String, Contributor> contributors, String customer) {
        var entityDescription = new EntityDescription();
        entityDescription.setAbstracts(extractAbstracts(dublinCore));
        entityDescription.setDescriptions(extractDescriptions(dublinCore));
        entityDescription.setMainTitle(extractMainTitle(dublinCore));
        entityDescription.setAlternativeTitles(extractAlternativeTitles(dublinCore));
        entityDescription.setContributors(extractContributors(dublinCore, contributors, customer));
        entityDescription.setTags(SubjectScraper.extractTags(dublinCore));
        entityDescription.setPublicationInstance(extractPublicationInstance(dublinCore));
        entityDescription.setPublicationDate(extractPublicationDate(dublinCore));
        entityDescription.setLanguage(BrageNvaLanguageMapper.extractLanguage(dublinCore));
        return entityDescription;
    }

    public static List<Contributor> extractContributors(DublinCore dublinCore, Map<String, Contributor> contributors,
                                                       String customer) {
        var contributorList = constructContributors(dublinCore, contributors, customer);
        var contributorsWithSequence = injectSequenceNumberToContributors(contributorList);
        var orcIdList = DublinCoreScraper.extractOrcIds(dublinCore);
        return containsSingleContributorAndOrcId(contributorsWithSequence, orcIdList)
                   ? createSingleContributorWithOrcId(contributorsWithSequence, orcIdList)
                   : contributorsWithSequence;
    }

    private static List<Contributor> createSingleContributorWithOrcId(List<Contributor> contributorsWithSequence,
                                                              List<URI> orcIdList) {
        contributorsWithSequence.get(0).getIdentity().setOrcId(orcIdList.get(0));
        return contributorsWithSequence;
    }

    private static boolean containsSingleContributorAndOrcId(List<Contributor> contributorsWithSequence,
                                                             List<URI> orcIdList) {
        return isSingleton(contributorsWithSequence) && isSingleton(orcIdList);
    }

    private static List<Contributor> constructContributors(DublinCore dublinCore, Map<String, Contributor> contributors,
                                                                    String customer) {
        return dublinCore.getDcValues().stream()
                   .filter(EntityDescriptionExtractor::isContributor)
                   .map(EntityDescriptionExtractor::createContributorFromDcValue)
                   .flatMap(Optional::stream)
                   .map(contributor -> updateRoleBasedOnType(contributor, dublinCore, customer))
                   .map(contributor -> updateContributor(contributor, contributors))
                   .map(EntityDescriptionExtractor::updateNameOrder)
                   .collect(Collectors.toList());
    }

    private static List<Contributor> injectSequenceNumberToContributors(List<Contributor> contributorList) {
        return IntStream.range(0, contributorList.size()).mapToObj(i -> {
            var contributor = contributorList.get(i);
            contributor.setSequence(i + 1);
            return contributor;
        }).collect(Collectors.toList());
    }

    public static String extractIssue(DublinCore dublinCore) {
        var issues = dublinCore.getDcValues().stream()
                         .filter(DcValue::isIssue)
                         .map(DcValue::scrapeValueAndSetToScraped)
                         .collect(Collectors.toList());
        return !issues.isEmpty() ? issues.get(0) : null;
    }

    public static List<String> extractDescriptions(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(EntityDescriptionExtractor::shouldBeMappedToDescription)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(EntityDescriptionExtractor::trim)
                   .distinct()
                   .collect(Collectors.toList());
    }

    private static boolean shouldBeMappedToDescription(DcValue value) {
        return value.isDescription() || value.isOneOfTwoPossibleVersions() && !isSupportedPublisherVersion(value.getValue());
    }

    public static String extractVolume(DublinCore dublinCore) {
        var volumes = dublinCore.getDcValues().stream()
                          .filter(DcValue::isVolume)
                          .map(DcValue::scrapeValueAndSetToScraped)
                          .collect(Collectors.toList());
        return volumes.isEmpty() ? null : volumes.get(0);
    }

    public static String trim(String value) {
        return nonNull(value)
                   ? value.replaceAll("\\r\\n|\\n\\r", StringUtils.SPACE)
                   .replaceAll("\\s{2,}", StringUtils.SPACE)
                   : null;
    }

    private static List<String> extreactMainTitles(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isMainTitle)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .filter(StringUtils::isNotBlank)
                   .collect(Collectors.toList());
    }

    private static boolean hasMoreThanTwoValues(List<String> values) {
        return values.size() >= 2;
    }

    private static boolean isContributor(DcValue dcValue) {
        return dcValue.isContributor() || dcValue.isCreator();
    }

    private static Contributor updateRoleBasedOnType(Contributor contributor, DublinCore dublinCore, String customer) {
        var type = TypeMapper.convertBrageTypeToNvaType(DublinCoreScraper.extractType(dublinCore, customer));
        if (NvaType.DATASET.getValue().equals(type)) {
            contributor.setRole(DATA_COLLECTOR);
        }
        return contributor;
    }

    private static Contributor updateNameOrder(Contributor contributor) {
        return fullNameIsSeparatedByComa(contributor)
                   ? switchNames(contributor)
                   : removeUnusedNameElementsIfNeeded(contributor);
    }

    private static Contributor removeUnusedNameElementsIfNeeded(Contributor contributor) {
        var name = contributor.getIdentity().getName();
        if (canNotBeSplitted(name)) {
            var updatedName = name.replaceAll(",", StringUtils.EMPTY_STRING);
            contributor.getIdentity().setName(updatedName);
            return contributor;
        }
        return contributor;
    }

    private static boolean canNotBeSplitted(String name) {
        return name.split(",").length == 1;
    }

    private static Contributor switchNames(Contributor contributor) {
        var fullNameValues = Arrays.asList(contributor.getIdentity().getName().split(","));
        contributor.getIdentity().setName(getFirstName(fullNameValues)
                                          + StringUtils.SPACE
                                          + getLastName(fullNameValues));
        return contributor;
    }

    /**
     * Should switch names when they are separated by coma, but do not contain "og" in between. "Fullname" can be a
     * name, but also a role to person. Role containing coma and "og" should not be switched. Example: Lennon, John =>
     * John Lennon Fagansvarlig i Oslo, Trondheim og Bergen => no changes
     */

    private static boolean fullNameIsSeparatedByComa(Contributor contributor) {
        String fullname = contributor.getIdentity().getName();
        return fullname.contains(",")
               && !fullname.contains(" og ")
               && fullname.split(",").length > 1;
    }

    private static String getLastName(List<String> fullNameValues) {
        return fullNameValues.get(0).trim();
    }

    private static String getFirstName(List<String> fullNameValues) {
        return fullNameValues.get(1).trim();
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
        var date = extractDate(dublinCore);
        try {
            if (isNull(date) || isEmptyString(date)) {
                return new PublicationDate(null, new PublicationDateNva.Builder().build());
            }
            if (containsYearOnly(date)) {
                return new PublicationDate(date, constructDateWithYearOnly(date));
            }
            if (DublinCoreValidator.containsTwoDigitYearOnly(date)) {
                return new PublicationDate(date, constructPublicationDateForTwoDigitYear(date));
            }
            if (DublinCoreValidator.containsYearAndMonth(date)) {
                return new PublicationDate(date, constructDateWithYearAndMonth(date));
            }
            if (DublinCoreValidator.isPeriodDate(date)) {
                return new PublicationDate(date, constructDateWithYearOnly(date));
            }
            return new PublicationDate(date, constructFullDate(date));
        } catch (Exception e) {
            return new PublicationDate(date, new PublicationDateNva.Builder().build());
        }
    }

    private static PublicationDateNva constructDateWithYearOnly(String date) {
        if (containsYearOnly(date.split(HYPHEN)[0])) {
            return new Builder().withYear(date.split(HYPHEN)[0]).build();
        }
        if (containsYearOnly(date.split(DASH)[0])) {
            return new Builder().withYear(date.split(DASH)[0]).build();
        } else {
            return new Builder().withYear(date).build();
        }
    }

    private static String extractDate(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isPublicationDate)
                   .findAny()
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(EntityDescriptionExtractor::modifyIfDateIsOfLocalDateTimeFormat)
                   .orElse(null);
    }

    private static PublicationDateNva constructDateWithYearAndMonth(String date) {
        return new Builder()
                   .withYear(date.split(DATE_DELIMITER)[0])
                   .withMonth(date.split(DATE_DELIMITER)[1])
                   .withDay(FIRST_DAY_OF_A_MONTH).build();
    }

    private static PublicationDateNva constructFullDate(String date) {
        return new Builder()
                   .withYear(date.split(DATE_DELIMITER)[0])
                   .withMonth(date.split(DATE_DELIMITER)[1])
                   .withDay(date.split(DATE_DELIMITER)[2]).build();
    }

    private static PublicationDateNva constructPublicationDateForTwoDigitYear(String date) {
        if (isYearFromLastCentury(date)) {
            return constructDateWithYearOnly("19" + date);
        }
        return constructDateWithYearOnly("20" + date);
    }

    private static boolean isYearFromLastCentury(String date) {
        return Integer.parseInt(date) > 24;
    }

    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    private static boolean isEmptyString(String date) {
        return date.trim().isEmpty();
    }

    private static Set<String> extractAbstracts(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isAbstract)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .map(EntityDescriptionExtractor::trim)
                   .collect(Collectors.toSet());
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

    private static PublicationInstance extractPublicationInstance(DublinCore dublinCore) {
        var publicationInstance = new PublicationInstance();
        publicationInstance.setIssue(extractIssue(dublinCore));
        publicationInstance.setPageNumber(PageConverter.extractPages(dublinCore));
        publicationInstance.setVolume(extractVolume(dublinCore));
        publicationInstance.setArticleNumber(extractArticleNumber(dublinCore));
        return publicationInstance;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private static Optional<Contributor> createContributorFromDcValue(DcValue dcValue) {
        var name = Optional.ofNullable(dcValue.scrapeValueAndSetToScraped())
                       .map(value -> value.replace("Author::", StringUtils.EMPTY_STRING))
                       .orElse(null);
        var identity = new Identity(name, null);
        if (isNull(identity.getName()) || identity.getName().isEmpty()) {
            return Optional.empty();
        }
        var brageRole = Optional.ofNullable(dcValue.getQualifier()).map(Qualifier::getValue).orElse(null);
        if (dcValue.isAuthor()) {
            return Optional.of(new Contributor(identity, AUTHOR, brageRole, Set.of()));
        }
        if (dcValue.isAdvisor()) {
            return Optional.of(new Contributor(identity, SUPERVISOR, brageRole, Set.of()));
        }
        if (dcValue.isEditor()) {
            return Optional.of(new Contributor(identity, EDITOR, brageRole, Set.of()));
        }
        if (dcValue.isIllustrator()) {
            return Optional.of(new Contributor(identity, ILLUSTRATOR, brageRole, Set.of()));
        }
        if (dcValue.getQualifier() == ORCID || dcValue.isCreator() && dcValue.getQualifier() == NONE) {
            return Optional.empty();
        }
        return Optional.of(new Contributor(identity, OTHER_CONTRIBUTOR, brageRole, Set.of()));
    }

    private static Contributor updateContributor(Contributor contributor, Map<String, Contributor> contributors) {
        var contributorWithCristinIdentifier = contributors.get(contributor.getIdentity().getName());
        if (nonNull(contributorWithCristinIdentifier)) {
            contributor.setIdentity(new Identity(contributorWithCristinIdentifier.getIdentity().getName(),
                                                 contributorWithCristinIdentifier.getIdentity().getIdentifier()));
            contributor.setAffiliations(contributorWithCristinIdentifier.getAffiliations());
            return contributor;
        }
        return contributor;
    }
}

package no.sikt.nva.scrapers;

import static java.util.Objects.isNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.record.Contributor;
import no.sikt.nva.model.record.EntityDescription;
import no.sikt.nva.model.record.Identity;
import no.sikt.nva.model.record.PublicationDate;
import no.sikt.nva.model.record.PublicationInstance;
import no.sikt.nva.validators.DublinCoreValidator;

public final class EntityDescriptionExtractor {

    public static final String FIRST_DAY_OF_A_MONTH = "-01";

    public static final String CONTRIBUTOR = "Contributor";
    public static final String ADVISOR = "Advisor";
    public static final String AUTHOR = "Author";
    public static final String EDITOR = "Editor";
    public static final String ILLUSTRATOR = "Illustrator";
    public static final String OTHER_CONTRIBUTOR = "Other";

    private EntityDescriptionExtractor() {

    }

    public static String extractMainTitle(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isMainTitle)
                   .findAny()
                   .orElse(new DcValue())
                   .scrapeValueAndSetToScraped();
    }

    public static List<String> extractAlternativeTitles(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isAlternativeTitle)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
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
        return entityDescription;
    }

    private static PublicationDate extractPublicationDate(DublinCore dublinCore) {
        var date = dublinCore.getDcValues().stream()
                       .filter(DcValue::isPublicationDate)
                       .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();

        if (isNull(date)) {
            return null;
        }
        if (DublinCoreValidator.containsYearOnly(date)) {
            return new PublicationDate(date, date);
        }
        if (DublinCoreValidator.containsYearAndMonth(date)) {
            return new PublicationDate(date, date + FIRST_DAY_OF_A_MONTH);
        }
        return new PublicationDate(date, date);
    }

    private static List<String> extractAbstracts(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isAbstract)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static List<String> extractDescriptions(DublinCore dublinCore) {
        return dublinCore.getDcValues()
                   .stream()
                   .filter(DcValue::isDescription)
                   .map(DcValue::scrapeValueAndSetToScraped)
                   .collect(Collectors.toList());
    }

    private static List<Contributor> extractContributors(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isContributor)
                   .map(EntityDescriptionExtractor::createContributorFromDcValue)
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    private static PublicationInstance extractPublicationInstance(DublinCore dublinCore) {
        var publicationInstance = new PublicationInstance();
        publicationInstance.setIssue(extractIssue(dublinCore));
        publicationInstance.setPageNumber(extractPageNumber(dublinCore));
        publicationInstance.setVolume(extractVolume(dublinCore));
        return publicationInstance;
    }

    private static String extractIssue(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isIssue)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static String extractVolume(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isVolume)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static String extractPageNumber(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(DcValue::isPageNumber)
                   .findAny().orElse(new DcValue()).scrapeValueAndSetToScraped();
    }

    private static Optional<Contributor> createContributorFromDcValue(DcValue dcValue) {
        Identity identity = new Identity(dcValue.scrapeValueAndSetToScraped());
        String brageRole = dcValue.getQualifier().getValue();
        if (dcValue.isAuthor()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, AUTHOR, brageRole));
        }
        if (dcValue.isAdvisor()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, ADVISOR, brageRole));
        }
        if (dcValue.isEditor()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, EDITOR, brageRole));
        }
        if (dcValue.isIllustrator()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, ILLUSTRATOR, brageRole));
        }
        if (dcValue.isOtherContributor()) {
            return Optional.of(new Contributor(CONTRIBUTOR, identity, OTHER_CONTRIBUTOR, brageRole));
        }
        return Optional.empty();
    }
}
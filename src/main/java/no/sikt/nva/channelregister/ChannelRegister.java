package no.sikt.nva.channelregister;

import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DC_JOURNAL_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DC_PUBLISHER_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_DC_ISSN_AND_DC_JOURNAL;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_DC_PUBLISHER;
import static no.sikt.nva.scrapers.CustomerMapper.BORA;
import static no.sikt.nva.scrapers.CustomerMapper.IMR;
import static no.sikt.nva.scrapers.CustomerMapper.NMBU;
import static no.sikt.nva.scrapers.CustomerMapper.OMSORGSFORSKNING;
import static no.sikt.nva.scrapers.DublinCoreScraper.isInCristin;
import static no.sikt.nva.validators.DublinCoreValidator.filterOutNullValues;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import no.sikt.nva.BrageProcessor;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.NvaType;
import no.sikt.nva.brage.migration.common.model.record.Publication;
import no.sikt.nva.brage.migration.common.model.record.PublicationContext;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.dublincore.Element;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.TypeMapper;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.SingletonCollector;
import nva.commons.core.StringUtils;
import nva.commons.core.ioutils.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public final class ChannelRegister {

    public static final Set<String> DEGREES = Set.of(NvaType.BACHELOR_THESIS.getValue(),
                                                      NvaType.DOCTORAL_THESIS.getValue(),
                                                      NvaType.MASTER_THESIS.getValue());
    public static final String NO_ISSN_OR_JOURNAL_FOUND = "No issn or journal found";
    public static final String NO_PUBLISHER_FOUND = "No publisher found";
    public static final String KANALREGISTER_READING_ERROR_MESSAGE = "Fatal error, could not read kanalregister";
    public static final String NOT_FOUND_IN_CHANNEL_REGISTER = "NOT_FOUND_IN_CHANNEL_REGISTER: ";
    public final static List<NvaType> SEARCHABLE_TYPES_IN_PUBLISHERS = List.of(
        NvaType.BOOK, NvaType.DATASET, NvaType.REPORT, NvaType.BACHELOR_THESIS, NvaType.MASTER_THESIS,
        NvaType.DOCTORAL_THESIS, NvaType.WORKING_PAPER, NvaType.STUDENT_PAPER, NvaType.STUDENT_PAPER_OTHERS,
        NvaType.RESEARCH_REPORT, NvaType.DESIGN_PRODUCT, NvaType.MEDIA_FEATURE_ARTICLE, NvaType.SOFTWARE,
        NvaType.LECTURE,
        NvaType.RECORDING_MUSICAL, NvaType.PLAN_OR_BLUEPRINT, NvaType.MAP, NvaType.CONFERENCE_POSTER,
        NvaType.SCIENTIFIC_MONOGRAPH, NvaType.SCIENTIFIC_CHAPTER);
    public static final String PUBLISHER_CHANNEL_REGISTRY_ALIASES_CSV_PATH = "publisher_channel_registry_aliases.csv";
    public static final String NTNU_PUBLISHER_CHANNEL_REGISTRY_FACULTIES_CSV_PATH =
        "ntnu_publisher_channel_registry_faculties.csv";
    public static final String NMBU_PUBLISHER_CHANNEL_REGISTRY_FACULTIES_CSV_PATH =
        "nmbu_publisher_channel_registry_faculties.csv";
    public static final String UIB_PUBLISHER_CHANNEL_REGISTRY_FACULTIES_CSV_PATH =
        "uib_specific_publisher_channel_registry.csv";
    public static final String CUSTOMERS_ISSUING_DEGREES = "customer_pids_for_customers_issuing_degrees.json";
    public static final String PUBLISHER_WILDCARDS_CSV_NAME = "publisher_wildcards.csv";
    public static final String NTNU = "ntnu";
    private static final String JOURNAL_PATH = "journals_channel_registry_v2.csv";
    private static final String PUBLISHERS_PATH = "publisher_channel_registry_v2.csv";
    private static final String JOURNAL_ALIAS_PATH = "journals_channel_registry_aliases.csv";
    private static final char SEPARATOR = ';';
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    /*volatile*/ private static ChannelRegister register;
    private final List<ChannelRegisterJournal> channelRegisterJournals;
    private final List<ChannelRegisterPublisher> channelRegisterPublishers;
    private final List<ChannelRegisterAlias> channelRegisterAliasesJournals;
    private final List<ChannelRegisterAlias> channelRegisterAliasesPublishers;
    private final List<ChannelRegisterWildcard> publisherWildCards;
    private final List<ChannelRegisterAlias> channelRegisterAliasesForNtnu;
    private final List<ChannelRegisterAlias> channelRegisterAliasesForNmbu;
    private final List<ChannelRegisterAlias> channelRegisterAliasesForBora;
    private static final Set<CustomerIssuingDegrees> customerIssuingDegrees = getCustomersIssuingDegrees();

    private ChannelRegister() {
        this.channelRegisterJournals = getJournalsFromCsv();
        this.channelRegisterPublishers = getPublishersFromCsv();
        this.channelRegisterAliasesJournals = getChannelRegisterAliases(JOURNAL_ALIAS_PATH);
        this.channelRegisterAliasesPublishers = getChannelRegisterAliases(PUBLISHER_CHANNEL_REGISTRY_ALIASES_CSV_PATH);
        this.channelRegisterAliasesForNtnu = getChannelRegisterAliases(
            NTNU_PUBLISHER_CHANNEL_REGISTRY_FACULTIES_CSV_PATH);
        this.channelRegisterAliasesForBora =
            getChannelRegisterAliases(UIB_PUBLISHER_CHANNEL_REGISTRY_FACULTIES_CSV_PATH);
        this.channelRegisterAliasesForNmbu = getChannelRegisterAliases(
            NMBU_PUBLISHER_CHANNEL_REGISTRY_FACULTIES_CSV_PATH);
        this.publisherWildCards = getPublisherWildcards(PUBLISHER_WILDCARDS_CSV_NAME);
    }

    private static Set<CustomerIssuingDegrees> getCustomersIssuingDegrees() {
        var json = IoUtils.stringFromResources(Path.of(CUSTOMERS_ISSUING_DEGREES));
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(json,
                                                                      new TypeReference<Set<CustomerIssuingDegrees>>() {})).orElseThrow();
    }

    private List<ChannelRegisterWildcard> getPublisherWildcards(String filename) {
        try (var inputStream = Thread.currentThread().getContextClassLoader()
                                   .getResourceAsStream(filename);
            var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            var microJournal = new CsvToBeanBuilder<ChannelRegisterWildcard>(bufferedReader)
                                   .withSeparator(SEPARATOR)
                                   .withType(ChannelRegisterWildcard.class)
                                   .build();
            return microJournal.parse();
        } catch (IOException e) {
            logger.error(KANALREGISTER_READING_ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    public static ChannelRegister getRegister() {
        ChannelRegister result = register;
        if (result == null) {
            synchronized (ChannelRegister.class) {
                if (register == null) {
                    register = new ChannelRegister();
                }
            }
        }
        return register;
    }

    public Optional<ErrorDetails> getChannelRegisterErrors(DublinCore dublinCore,
                                                           BrageLocation brageLocation,
                                                           String customer) {
        if (typeIsPresentInDublinCore(dublinCore) && !isInCristin(dublinCore)) {
            if (isJournalArticle(dublinCore, customer)) {
                return getErrorDetailsForJournalArticle(dublinCore, brageLocation, customer);
            }
            if (hasPublisher(dublinCore) && isSearchableInPublishers(dublinCore, customer) && doesNotHaveSpecialCaseMappingOverRidingRegularChannelRegistry(dublinCore, customer)) {
                return getErrorDetailsForPublisher(dublinCore, brageLocation, customer);
            }
        }
        return Optional.empty();
    }

    private static boolean doesNotHaveSpecialCaseMappingOverRidingRegularChannelRegistry(DublinCore dublinCore,
                                                                               String customer ) {
        return !isDegreeFromInstitutionIssuingDegrees(dublinCore, customer);
    }

    public static boolean isDegreeFromInstitutionIssuingDegrees(DublinCore dublinCore, String customer) {
        return isDegree(dublinCore) && customerIssuesDegrees(customer);
    }

    private static boolean isDegree(DublinCore dublinCore) {
        var dublinCoreTypes = extractTypes(dublinCore);
        var nvaTypes = TypeMapper.mapToNvaTypeIfMappable(dublinCoreTypes);
        if (StringUtils.isBlank(nvaTypes)) {
            return false;
        }
        return DEGREES.contains(nvaTypes);
    }

    private static Set<String> extractTypes(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .filter(dcValue -> Element.TYPE.equals(dcValue.getElement()))
                   .map(DcValue::getValue)
                   .collect(Collectors.toSet());
    }

    public String lookUpInChannelRegisterForPublisher(Record record, String customer) {
        return shouldLookUpInDegreePids(record, customer)
            ? lookUpInDegreePids(customer)
            :         Optional.ofNullable(record.getPublication())
                   .map(Publication::getPublicationContext)
                   .map(PublicationContext::getBragePublisher)
                   .map(ChannelRegister::formatValue)
                   .map(value -> lookUpInPublisher(value, customer))
                   .orElse(null);
    }

    private static String lookUpInDegreePids(String customer) {
        var customerIssuingDegree =
            customerIssuingDegrees.stream().filter(c -> c.getBrage().equals(customer)).findFirst().get();
        return  customerIssuingDegree.getChannelRegistryPidProd();
    }



    private boolean shouldLookUpInDegreePids(Record record, String customer) {
        return record.isDegree() && customerIssuesDegrees(customer);
    }

    private static boolean customerIssuesDegrees(String customer) {
        return customerIssuingDegrees.stream().anyMatch(customerIssuingDegree ->  customerIssuingDegree.getBrage().equals(customer));
    }

    public String lookUpInJournal(Publication publication, BrageLocation brageLocation, String customer) {
        var issnList = publication.getIssnSet();
        var title = formatValue(getTitleToSearchInChannelRegister(publication, customer));
        var identifiersForIssnList = issnList.stream()
                                         .map(issn -> lookUpInJournalByIssn(issn, brageLocation))
                                         .collect(Collectors.toList());
        var identifierByIssn = !identifiersForIssnList.isEmpty() ? identifiersForIssnList.get(0) : null;
        return nonNull(identifierByIssn)
                   ? identifierByIssn
                   : lookUpInJournalByTitle(title, brageLocation);
    }

    private static String getTitleToSearchInChannelRegister(Publication publication, String customer) {
        return IMR.equalsIgnoreCase(customer) && getBragePublisher(publication).map("FiskeribladetFiskaren"::equals).orElse(false)
                   ? getBragePublisher(publication).get()
                   : publication.getJournal();
    }

    private static Optional<String> getBragePublisher(Publication publication) {
        return Optional.ofNullable(publication)
                   .map(Publication::getPublicationContext)
                   .map(PublicationContext::getBragePublisher);
    }

    private static String formatValue(String value) {
        return nonNull(value) ? value.replaceAll("(\n)|(\b)|(\u200b)|(\t)", StringUtils.EMPTY_STRING) : null;
    }

    public String lookUpInJournalByIssn(String issn, BrageLocation brageLocation) {
        try {
            if (StringUtils.isBlank(issn)) {
                return issn;
            } else {
                return channelRegisterJournals.stream()
                           .filter(item -> item.hasIssn(issn))
                           .map(ChannelRegisterJournal::getPid)
                           .distinct()
                           .collect(SingletonCollector.collectOrElse(null));
            }
        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER, filterOutNullValues(Set.of(issn)))
                         + StringUtils.SPACE
                         + brageLocation.getOriginInformation());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String lookUpInJournalByTitle(String title, BrageLocation brageLocation) {
        try {
            if (StringUtils.isBlank(title)) {
                return title;
            } else {
                return Optional.ofNullable(lookUpInJournalsByTitle(title))
                           .orElseGet(() -> lookupInJournalAliases(title));
            }
        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER,
                                          filterOutNullValues(Set.of(title)))
                         + StringUtils.SPACE
                         + brageLocation.getOriginInformation());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String lookUpInJournalsByTitle(String title) {
        return channelRegisterJournals.stream()
                   .filter(item -> item.hasTitle(title))
                   .map(ChannelRegisterJournal::getPid)
                   .distinct()
                   .collect(SingletonCollector.collectOrElse(null));
    }

    public String lookUpInPublisher(String publisher, String customer) {
        try {
            return lookupInPublishers(publisher)
                           .or(() -> lookupInPublisherWildcards(publisher))
                           .orElseGet(() -> lookupInPublisherAliases(publisher, customer));

        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER,
                                          filterOutNullValues(Set.of(publisher))).toString());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Optional<String> lookupInPublisherWildcards(String publisher) {
        return getPublisherName(publisher)
                   .map(value -> lookInPublishers(channelRegisterPublishers, value));
    }

    private Optional<String> getPublisherName(String publisher) {
        return publisherWildCards.stream()
                   .filter(wildcard -> publisher.contains(wildcard.getWildcard()))
                   .map(ChannelRegisterWildcard::getName)
                   .findFirst();
    }

    private static List<ChannelRegisterAlias> getChannelRegisterAliases(String filePath) {
        try (var inputStream = Thread.currentThread().getContextClassLoader()
                                   .getResourceAsStream(filePath);
            var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            var microJournal = new CsvToBeanBuilder<ChannelRegisterAlias>(bufferedReader)
                                   .withSeparator(SEPARATOR)
                                   .withType(ChannelRegisterAlias.class)
                                   .build();
            return microJournal.parse();
        } catch (IOException e) {
            logger.error(KANALREGISTER_READING_ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    private static boolean isSearchableInPublishers(DublinCore dublinCore, String customer) {
        return SEARCHABLE_TYPES_IN_PUBLISHERS.stream()
                   .map(NvaType::getValue)
                   .collect(Collectors.toList())
                   .contains(TypeMapper.convertBrageTypeToNvaType(DublinCoreScraper.extractType(dublinCore, customer)));
    }

    private static List<ChannelRegisterPublisher> getPublishersFromCsv() {
        try (var inputStream = Thread.currentThread().getContextClassLoader()
                                   .getResourceAsStream(PUBLISHERS_PATH);
            var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            var microJournal = new CsvToBeanBuilder<ChannelRegisterPublisher>(bufferedReader)
                                   .withSeparator(SEPARATOR)
                                   .withType(ChannelRegisterPublisher.class)
                                   .build();
            return microJournal.parse();
        } catch (IOException e) {
            logger.error(KANALREGISTER_READING_ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    private static List<ChannelRegisterJournal> getJournalsFromCsv() {

        try (var inputStream = Thread.currentThread().getContextClassLoader()
                                   .getResourceAsStream(JOURNAL_PATH);
            var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            var microJournal = new CsvToBeanBuilder<ChannelRegisterJournal>(bufferedReader)
                                   .withSeparator(SEPARATOR)
                                   .withType(ChannelRegisterJournal.class)
                                   .build();
            return microJournal.parse();
        } catch (IOException e) {
            logger.error(KANALREGISTER_READING_ERROR_MESSAGE);
            throw new RuntimeException(e);
        }
    }

    private static boolean hasPublisher(DublinCore dublinCore) {
        var publisher = DublinCoreScraper.extractPublisher(dublinCore);
        return nonNull(publisher);
    }

    private static Optional<ErrorDetails> getChannelRegisterErrorDetailsWhenSearchingForPublisher(String publisher) {
        if (!filterOutNullValues(Collections.singleton(publisher)).isEmpty()) {
            return Optional.of(
                new ErrorDetails(DC_PUBLISHER_NOT_IN_CHANNEL_REGISTER,
                                 filterOutNullValues(Collections.singleton(publisher))));
        } else {
            return Optional.of(new ErrorDetails(MISSING_DC_PUBLISHER,
                                                Collections.singleton(NO_PUBLISHER_FOUND)));
        }
    }

    private static Optional<ErrorDetails> getChannelRegisterErrorDetailsWhenSearchingForJournals(Set<String> issnList,
                                                                                                 String title) {
        var valuesToLog = new HashSet<String>();
        valuesToLog.add(title);
        valuesToLog.addAll(issnList);
        if (!filterOutNullValues(valuesToLog).isEmpty()) {
            return Optional.of(new ErrorDetails(DC_JOURNAL_NOT_IN_CHANNEL_REGISTER, filterOutNullValues(valuesToLog)));
        } else {
            return Optional.of(
                new ErrorDetails(MISSING_DC_ISSN_AND_DC_JOURNAL, Collections.singleton(NO_ISSN_OR_JOURNAL_FOUND)));
        }
    }

    private static boolean typeIsPresentInDublinCore(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isType);
    }

    private static boolean isJournalArticle(DublinCore dublinCore, String customer) {
        return DublinCoreScraper.extractType(dublinCore, customer).contains(BrageType.JOURNAL_ARTICLE.getValue())
               || DublinCoreScraper.extractType(dublinCore, customer).contains(BrageType.JOURNAL_ISSUE.getValue())
               || DublinCoreScraper.extractType(dublinCore, customer).contains(BrageType.ARTICLE.getValue());
    }

    private String lookupInPublisherAliases(String publisher,
                                            String customer) {
        var pid = lookupInPublisherAliases(channelRegisterPublishers, channelRegisterAliasesPublishers, publisher);
        if (StringUtils.isEmpty(pid)) {
            pid = lookupInCustomerSpecificCsv(publisher, customer).orElse(null);
        }
        return pid;
    }

    private Optional<String> lookupInCustomerSpecificCsv(String publisher, String customer) {
        if (NTNU.equalsIgnoreCase(customer) || OMSORGSFORSKNING.equalsIgnoreCase(customer)) {
            return Optional.ofNullable(lookupInPublisherAliases(channelRegisterPublishers, channelRegisterAliasesForNtnu, publisher));
        }
        if (BORA.equalsIgnoreCase(customer)) {
            return Optional.ofNullable(lookupInPublisherAliases(channelRegisterPublishers, channelRegisterAliasesForBora, publisher));
        }
        if (NMBU.equalsIgnoreCase(customer)) {
            return Optional.ofNullable(lookupInPublisherAliases(channelRegisterPublishers, channelRegisterAliasesForNmbu, publisher));
        }
        return Optional.empty();
    }

    private String lookupInJournalAliases(String publisher) {
        var originalTitle = channelRegisterAliasesJournals.stream()
                   .filter(item -> item.hasAlias(publisher))
                   .map(ChannelRegisterAlias::getOriginalTitle)
                   .distinct()
                   .collect(SingletonCollector.collectOrElse(null));
        return lookUpInJournalsByTitle(originalTitle);
    }

    private String lookupInPublisherAliases(List<ChannelRegisterPublisher> publishers, List<ChannelRegisterAlias> aliases, String publisher) {
        var originalPublisher = getOriginalPublisher(aliases, publisher);
        return nonNull(originalPublisher)
                   ? lookInPublishers(publishers, originalPublisher)
                   : null;
    }

    private static String lookInPublishers(List<ChannelRegisterPublisher> publishers, String originalPublisher) {
        return publishers.stream()
                   .filter(item -> item.hasPublisher(originalPublisher))
                   .map(ChannelRegisterPublisher::getPid)
                   .distinct()
                   .collect(SingletonCollector.collectOrElse(null));
    }

    private static String getOriginalPublisher(List<ChannelRegisterAlias> aliases, String publisher) {
        return aliases.stream()
                   .filter(item -> item.hasAlias(publisher))
                   .map(ChannelRegisterAlias::getOriginalTitle)
                   .distinct()
                   .collect(SingletonCollector.collectOrElse(null));
    }

    private Optional<ErrorDetails> getErrorDetailsForJournalArticle(DublinCore dublinCore,
                                                                    BrageLocation brageLocation, String customer) {
        var publication = DublinCoreScraper.extractPublication(dublinCore, customer);
        var possibleIdentifier = lookUpInJournal(publication, brageLocation, customer);
        if (nonNull(possibleIdentifier)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForJournals(publication.getIssnSet(),
                                                                          getTitleToSearchInChannelRegister(
                                                                              publication, customer));
        }
    }

    private Optional<ErrorDetails> getErrorDetailsForPublisher(DublinCore dublinCore,
                                                               BrageLocation brageLocation,
                                                               String customer) {
        var publisher = formatValue(DublinCoreScraper.extractPublisher(dublinCore));
        var publication = DublinCoreScraper.extractPublication(dublinCore, customer);
        var journalIdentifier = lookUpInJournal(publication, brageLocation, customer);
        var publisherIdentifier = lookUpInPublisher(publisher, customer);
        if (nonNull(journalIdentifier)
            || nonNull(publisherIdentifier)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForPublisher(publisher);
        }
    }

    private Optional<String> lookupInPublishers(String publisherFromMapper) {
        return Optional.ofNullable(lookInPublishers(channelRegisterPublishers, publisherFromMapper));
    }
}

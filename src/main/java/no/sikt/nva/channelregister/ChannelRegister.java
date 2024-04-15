package no.sikt.nva.channelregister;

import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DC_JOURNAL_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DC_PUBLISHER_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_DC_ISSN_AND_DC_JOURNAL;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_DC_PUBLISHER;
import static no.sikt.nva.scrapers.CustomerMapper.BORA;
import static no.sikt.nva.scrapers.CustomerMapper.NMBU;
import static no.sikt.nva.scrapers.DublinCoreScraper.isInCristin;
import static no.sikt.nva.validators.DublinCoreValidator.filterOutNullValues;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.TypeMapper;
import nva.commons.core.SingletonCollector;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GodClass")
public final class ChannelRegister {

    public static final String NO_ISSN_OR_JOURNAL_FOUND = "No issn or journal found";
    public static final String NO_PUBLISHER_FOUND = "No publisher found";
    public static final String KANALREGISTER_READING_ERROR_MESSAGE = "Fatal error, could not read kanalregister";
    public static final String NOT_FOUND_IN_CHANNEL_REGISTER = "NOT_FOUND_IN_CHANNEL_REGISTER: ";
    public static final List<NvaType> SEARCHABLE_TYPES_IN_JOURNALS = List.of(NvaType.JOURNAL_ARTICLE,
                                                                             NvaType.SCIENTIFIC_ARTICLE,
                                                                             NvaType.REPORT);
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
    private final List<ChannelRegisterAlias> channelRegisterAliasesForNtnu;
    private final List<ChannelRegisterAlias> channelRegisterAliasesForNmbu;
    private final List<ChannelRegisterAlias> channelRegisterAliasesForBora;

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
            if (hasPublisher(dublinCore) && isSearchableInPublishers(dublinCore, customer)) {
                return getErrorDetailsForPublisher(dublinCore, brageLocation, customer);
            }
        }
        return Optional.empty();
    }

    public String lookUpInChannelRegisterForPublisher(Record record, String customer) {
        var publisher = formatValue(record.getPublication().getPublicationContext().getBragePublisher());
        if (extractedIdentifierFromPublishersIsPresent(publisher, customer)) {
            return lookUpInPublisher(formatValue(publisher), customer);
        }
        return null;
    }

    public String lookUpInJournal(Publication publication, BrageLocation brageLocation) {
        var issnList = publication.getIssnSet();
        var title = formatValue(publication.getJournal());
        var identifiersForIssnList = issnList.stream()
                                         .map(issn -> lookUpInJournalByIssn(issn, brageLocation))
                                         .collect(Collectors.toList());
        var identifierByIssn = !identifiersForIssnList.isEmpty() ? identifiersForIssnList.get(0) : null;
        return nonNull(identifierByIssn)
                   ? identifierByIssn
                   : lookUpInJournalByTitle(title, brageLocation);
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
                var channel = channelRegisterJournals.stream()
                                  .filter(item -> item.hasTitle(title))
                                  .map(ChannelRegisterJournal::getPid)
                                  .distinct()
                                  .collect(SingletonCollector.collectOrElse(null));
                return Optional.ofNullable(channel).orElseGet(() -> lookupInJournalAliases(title));
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

    public String lookUpInPublisher(String publisher, String customer) {
        try {
            if (StringUtils.isBlank(publisher)) {
                return publisher;
            } else {
                return nonNull(getPublisherIdentifier(publisher))
                           ? getPublisherIdentifier(publisher)
                           : lookupInPublisherAliases(publisher, customer);
            }
        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER,
                                          filterOutNullValues(Set.of(publisher))).toString());
            return null;
        } catch (Exception e) {
            return null;
        }
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
               || DublinCoreScraper.extractType(dublinCore, customer).contains(BrageType.JOURNAL_ISSUE.getValue());
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
        if (NTNU.equalsIgnoreCase(customer)) {
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
        return channelRegisterJournals.stream()
                   .filter(item -> item.hasTitle(originalTitle))
                   .map(ChannelRegisterJournal::getPid)
                   .distinct()
                   .collect(SingletonCollector.collectOrElse(null));
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
        var possibleIdentifier = lookUpInJournal(publication, brageLocation);
        if (nonNull(possibleIdentifier)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForJournals(publication.getIssnSet(),
                                                                          publication.getJournal());
        }
    }

    private Optional<ErrorDetails> getErrorDetailsForPublisher(DublinCore dublinCore,
                                                               BrageLocation brageLocation,
                                                               String customer) {
        var publisher = formatValue(DublinCoreScraper.extractPublisher(dublinCore));
        var publication = DublinCoreScraper.extractPublication(dublinCore, customer);
        var journalIdentifier = lookUpInJournal(publication, brageLocation);
        var publisherIdentifier = lookUpInPublisher(publisher, customer);
        if (nonNull(journalIdentifier)
            || nonNull(publisherIdentifier)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForPublisher(publisher);
        }
    }

    private String getPublisherIdentifier(String publisherFromMapper) {
        return lookInPublishers(channelRegisterPublishers, publisherFromMapper);
    }

    private boolean extractedIdentifierFromPublishersIsPresent(String publisher, String customer) {
        if (StringUtils.isNotBlank(publisher)) {
            var identifierFromPublisher = lookUpInPublisher(publisher, customer);
            return StringUtils.isNotBlank(identifierFromPublisher);
        }
        return false;
    }
}

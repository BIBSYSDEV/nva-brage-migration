package no.sikt.nva.channelregister;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DC_JOURNAL_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_DC_ISSN_AND_DC_JOURNAL;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_DC_PUBLISHER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DC_PUBLISHER_NOT_IN_CHANNEL_REGISTER;
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
    private static final String JOURNAL_PATH = "journals_channel_registry_v2.csv";
    private static final String PUBLISHERS_PATH = "publisher_channel_registry_v2.csv";
    private static final String JOURNAL_ALIAS_PATH = "journals_channel_registry_aliases.csv";
    private static final char SEPARATOR = ';';
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    public static final String NTNU = "ntnu";
    /*volatile*/ private static ChannelRegister register;
    private final List<ChannelRegisterJournal> channelRegisterJournals;
    private final List<ChannelRegisterPublisher> channelRegisterPublishers;

    private final List<ChannelRegisterAlias> channelRegisterAliasesJournals;
    private final List<ChannelRegisterAlias> channelRegisterAliasesPublishers;


    private final List<ChannelRegisterAlias> channelRegisterAliasesForNtnu;

    private ChannelRegister() {
        this.channelRegisterJournals = getJournalsFromCsv();
        this.channelRegisterPublishers = getPublishersFromCsv();
        this.channelRegisterAliasesJournals = getChannelRegisterAliases(JOURNAL_ALIAS_PATH);
        this.channelRegisterAliasesPublishers = getChannelRegisterAliases(PUBLISHER_CHANNEL_REGISTRY_ALIASES_CSV_PATH);
        this.channelRegisterAliasesForNtnu = getChannelRegisterAliases("ntnu_publisher_channel_registry_faculties.csv");
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
        if (typeIsPresentInDublinCore(dublinCore)) {
            if (isJournalArticle(dublinCore)) {
                return getErrorDetailsForJournalArticle(dublinCore, brageLocation);
            }
            if (hasPublisher(dublinCore) && isSearchableInPublishers(dublinCore)) {
                return getErrorDetailsForPublisher(dublinCore, brageLocation, customer);
            }
        }
        return Optional.empty();
    }

    public String lookUpInChannelRegisterForPublisher(Record record, String customer) {
        var publicationContext = record.getPublication().getPublicationContext();
        if (extractedIdentifierFromPublishersIsPresent(publicationContext.getBragePublisher(), customer)) {
            return lookUpInPublisher(publicationContext.getBragePublisher(), customer);
        }
        return null;
    }

    public String lookUpInJournal(Publication publication, BrageLocation brageLocation) {
        var issnList = publication.getIssnSet();
        var title = publication.getJournal();
        var identifiersForIssnList = issnList.stream()
                                         .map(issn -> lookUpInJournalByIssn(issn, brageLocation))
                                         .collect(Collectors.toList());
        var identifierByIssn = !identifiersForIssnList.isEmpty() ? identifiersForIssnList.get(0) : null;
        return nonNull(identifierByIssn)
                   ? identifierByIssn
                   : lookUpInJournalByTitle(title, brageLocation);
    }

    public String lookUpInJournalByIssn(String issn, BrageLocation brageLocation) {
        try {
            if (isNullOrEmpty(issn)) {
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
            if (isNullOrEmpty(title)) {
                return title;
            } else {
                var channel = channelRegisterJournals.stream()
                                  .filter(item -> item.hasTitle(title))
                                  .map(ChannelRegisterJournal::getPid)
                                  .distinct()
                                  .collect(SingletonCollector.collectOrElse(null));

                return Optional.ofNullable(channel).orElse(lookupInAliases(channelRegisterAliasesJournals, title));
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
            if (isNullOrEmpty(publisher)) {
                return publisher;
            } else {
                return nonNull(getPublisherIdentifer(publisher))
                           ? getPublisherIdentifer(publisher)
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

    private String lookupInPublisherAliases(String publisher,
                                            String customer) {
        String pid = lookupInAliases(channelRegisterAliasesPublishers, publisher);
        if (StringUtils.isEmpty(pid) && NTNU.equalsIgnoreCase(customer)) {
            pid = lookupInAliases(channelRegisterAliasesForNtnu, publisher);
        }
        return pid;
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

    private static boolean isSearchableInPublishers(DublinCore dublinCore) {
        return SEARCHABLE_TYPES_IN_PUBLISHERS.stream()
                   .map(NvaType::getValue)
                   .collect(Collectors.toList())
                   .contains(TypeMapper.convertBrageTypeToNvaType(DublinCoreScraper.extractType(dublinCore)));
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

    private static boolean isJournalArticle(DublinCore dublinCore) {
        return DublinCoreScraper.extractType(dublinCore).contains(BrageType.JOURNAL_ARTICLE.getValue())
               || DublinCoreScraper.extractType(dublinCore).contains(BrageType.JOURNAL_ISSUE.getValue());
    }

    private String lookupInAliases(List<ChannelRegisterAlias>  aliases , String publisher) {
        return aliases.stream()
                   .filter(item -> item.hasAlias(publisher))
                   .map(ChannelRegisterAlias::getPid)
                   .distinct()
                   .collect(SingletonCollector.collectOrElse(null));
    }

    private Optional<ErrorDetails> getErrorDetailsForJournalArticle(DublinCore dublinCore,
                                                                    BrageLocation brageLocation) {
        var publication = DublinCoreScraper.extractPublication(dublinCore);
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
        var publisher = DublinCoreScraper.extractPublisher(dublinCore);
        var publication = DublinCoreScraper.extractPublication(dublinCore);
        var journalIdentifier = lookUpInJournal(publication, brageLocation);
        var publisherIdentifier = lookUpInPublisher(publisher, customer);
        if (nonNull(journalIdentifier)
            || nonNull(publisherIdentifier)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForPublisher(publisher);
        }
    }

    private String getPublisherIdentifer(String publisherFromMapper) {
        return channelRegisterPublishers.stream()
                   .filter(item -> item.hasPublisher(publisherFromMapper))
                   .map(ChannelRegisterPublisher::getPid)
                   .distinct()
                   .collect(SingletonCollector.collectOrElse(null));
    }

    private boolean extractedIdentifierFromPublishersIsPresent(String publisher, String customer) {
        if (isNotNullOrEmpty(publisher)) {
            var identifierFromPublisher = lookUpInPublisher(publisher, customer);
            return isNotNullOrEmpty(identifierFromPublisher);
        }
        return false;
    }

    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    private boolean isNotNullOrEmpty(String candidate) {
        return nonNull(candidate) && !candidate.trim().isEmpty();
    }

    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    private boolean isNullOrEmpty(String candidate) {
        return isNull(candidate) || candidate.trim().isEmpty();
    }
}

package no.sikt.nva.channelregister;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.JOURNAL_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_ISSN_AND_JOURNAL;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MISSING_PUBLISHER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.PUBLISHER_NOT_IN_CHANNEL_REGISTER;
import static no.sikt.nva.scrapers.DublinCoreScraper.channelRegister;
import static no.sikt.nva.validators.DublinCoreValidator.filterOutNullValues;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.BrageProcessor;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.BrageType;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.record.Publication;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.model.dublincore.DcValue;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.PublisherMapper;
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
    private static final String JOURNAL_PATH = "journals.csv";
    private static final String PUBLISHERS_PATH = "publishers.csv";
    private static final char SEPARATOR = ';';
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    /*volatile*/ private static ChannelRegister register;
    private final List<ChannelRegisterJournal> channelRegisterJournals;
    private final List<ChannelRegisterPublisher> channelRegisterPublishers;

    private ChannelRegister() {
        this.channelRegisterJournals = getJournalsFromCsv();
        this.channelRegisterPublishers = getPublishersFromCsv();
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

    public static Optional<ErrorDetails> getChannelRegisterErrors(DublinCore dublinCore, BrageLocation brageLocation) {
        if (typeIsPresentInDublinCore(dublinCore)) {
            if (isJournalArticle(dublinCore)) {
                return getErrorDetailsForJournalArticle(dublinCore, brageLocation);
            }
            if (hasPublisher(dublinCore) && isReport(dublinCore) || isBook(dublinCore)) {
                return getErrorDetailsForReport(dublinCore, brageLocation);
            }
        }
        return Optional.empty();
    }

    public String lookUpInChannelRegisterForPublisher(Record record) {
        var publicationContext = record.getPublication().getPublicationContext();
        if (extractedIdentifierFromPublishersIsPresent(publicationContext.getBragePublisher())) {
            return lookUpInPublisher(publicationContext.getBragePublisher());
        }
        return null;
    }

    public String lookUpInJournal(Publication publication, BrageLocation brageLocation) {
        var issnList = publication.getIssnList();
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
                           .map(ChannelRegisterJournal::getIdentifier)
                           .collect(SingletonCollector.collectOrElse(null));
            }
        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER,
                                          filterOutNullValues(List.of(issn)))
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
                return channelRegisterJournals.stream()
                           .filter(item -> item.hasTitle(title))
                           .map(ChannelRegisterJournal::getIdentifier)
                           .collect(SingletonCollector.collectOrElse(null));
            }
        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER,
                                          filterOutNullValues(List.of(title)))
                         + StringUtils.SPACE
                         + brageLocation.getOriginInformation());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String lookUpInPublisher(String publisher) {
        try {
            if (isNullOrEmpty(publisher)) {
                return publisher;
            } else {
                return nonNull(getPublisherIdentifer(publisher))
                           ? getPublisherIdentifer(publisher)
                           : getPublisherIdentifer(PublisherMapper.getMappablePublisher(publisher));
            }
        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER,
                                          filterOutNullValues(List.of(publisher))).toString());
            return null;
        } catch (Exception e) {
            return null;
        }
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

    private static boolean isBook(DublinCore dublinCore) {
        return DublinCoreScraper.extractType(dublinCore).contains(BrageType.BOOK.getValue());
    }

    private static boolean isReport(DublinCore dublinCore) {
        return DublinCoreScraper.extractType(dublinCore).contains(BrageType.REPORT.getValue());
    }

    private static boolean hasPublisher(DublinCore dublinCore) {
        var publisher = DublinCoreScraper.extractPublisher(dublinCore);
        return nonNull(publisher);
    }

    private static Optional<ErrorDetails> getErrorDetailsForJournalArticle(DublinCore dublinCore,
                                                                           BrageLocation brageLocation) {
        var publication = DublinCoreScraper.extractPublication(dublinCore);
        var possibleIdentifier = channelRegister.lookUpInJournal(publication, brageLocation);
        if (nonNull(possibleIdentifier)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForJournals(publication.getIssnList(),
                                                                          publication.getJournal());
        }
    }

    private static Optional<ErrorDetails> getErrorDetailsForReport(DublinCore dublinCore, BrageLocation brageLocation) {
        var publisher = DublinCoreScraper.extractPublisher(dublinCore);
        var publication = DublinCoreScraper.extractPublication(dublinCore);
        var journalIdentifier = channelRegister.lookUpInJournal(publication, brageLocation);
        var publisherIdentifier = channelRegister.lookUpInPublisher(publisher);
        if (nonNull(journalIdentifier)
            || nonNull(publisherIdentifier)) {
            return Optional.empty();
        } else {
            return getChannelRegisterErrorDetailsWhenSearchingForPublisher(publisher);
        }
    }

    private static Optional<ErrorDetails> getChannelRegisterErrorDetailsWhenSearchingForPublisher(String publisher) {
        if (!filterOutNullValues(Collections.singletonList(publisher)).isEmpty()) {
            return Optional.of(
                new ErrorDetails(PUBLISHER_NOT_IN_CHANNEL_REGISTER,
                                 filterOutNullValues(Collections.singletonList(publisher))));
        } else {
            return Optional.of(new ErrorDetails(MISSING_PUBLISHER,
                                                Collections.singletonList(NO_PUBLISHER_FOUND)));
        }
    }

    private static Optional<ErrorDetails> getChannelRegisterErrorDetailsWhenSearchingForJournals(List<String> issnList,
                                                                                                 String title) {
        List<String> valuesToLog = new ArrayList<>();
        valuesToLog.add(title);
        valuesToLog.addAll(issnList);
        if (!filterOutNullValues(valuesToLog).isEmpty()) {
            return Optional.of(new ErrorDetails(JOURNAL_NOT_IN_CHANNEL_REGISTER, filterOutNullValues(valuesToLog)));
        } else {
            return Optional.of(
                new ErrorDetails(MISSING_ISSN_AND_JOURNAL, Collections.singletonList(NO_ISSN_OR_JOURNAL_FOUND)));
        }
    }

    private static boolean typeIsPresentInDublinCore(DublinCore dublinCore) {
        return dublinCore.getDcValues().stream()
                   .anyMatch(DcValue::isType);
    }

    private static boolean isJournalArticle(DublinCore dublinCore) {
        return DublinCoreScraper.extractType(dublinCore).contains(BrageType.JOURNAL_ARTICLE.getValue());
    }

    private String getPublisherIdentifer(String publisherFromMapper) {
        return channelRegisterPublishers.stream()
                   .filter(item -> item.hasPublisher(publisherFromMapper))
                   .map(ChannelRegisterPublisher::getIdentifier)
                   .collect(SingletonCollector.collectOrElse(null));
    }

    private boolean extractedIdentifierFromPublishersIsPresent(String publisher) {
        if (isNotNullOrEmpty(publisher)) {
            var identifierFromPublisher = lookUpInPublisher(publisher);
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

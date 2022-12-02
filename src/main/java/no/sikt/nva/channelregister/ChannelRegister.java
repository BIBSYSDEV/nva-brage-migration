package no.sikt.nva.channelregister;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_JOURNAL_IN_CHANNEL_REGISTER;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER;
import static no.sikt.nva.validators.DublinCoreValidator.filterOutNullValues;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import no.sikt.nva.BrageProcessor;
import no.sikt.nva.brage.migration.common.model.BrageLocation;
import no.sikt.nva.brage.migration.common.model.ErrorDetails;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.PublisherMapper;
import nva.commons.core.SingletonCollector;
import nva.commons.core.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChannelRegister {

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

    public String lookUpInChannelRegisterForPublisher(Record record) {
        var publicationContext = record.getPublication().getPublicationContext();
        if (extractedIdentifierFromPublishersIsPresent(publicationContext.getBragePublisher())) {
            return lookUpInPublisher(publicationContext.getBragePublisher());
        }
        return null;
    }

    public String lookUpInJournal(String issn, String title, BrageLocation brageLocation) {
        return ObjectUtils.firstNonNull(lookUpInJournalByIssn(issn, brageLocation),
                                        lookUpInJournalByTitle(title, brageLocation));
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
                                          filterOutNullValues(issn))
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
                                          filterOutNullValues(title))
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
                var publisherFromMapper = PublisherMapper.getMappablePublisher(publisher);
                return channelRegisterPublishers.stream()
                           .filter(item -> item.hasPublisher(publisherFromMapper))
                           .map(ChannelRegisterPublisher::getIdentifier)
                           .collect(SingletonCollector.collectOrElse(null));
            }
        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(DUPLICATE_PUBLISHER_IN_CHANNEL_REGISTER,
                                          filterOutNullValues(publisher)).toString());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String extractIdentifierFromJournals(DublinCore dublinCore, BrageLocation brageLocation) {
        var issn = DublinCoreScraper.extractIssn(dublinCore, brageLocation);
        var title = DublinCoreScraper.extractJournal(dublinCore);
        try {
            return isNotNullOrEmpty(issn) || isNotNullOrEmpty(title)
                       ? channelRegisterJournals.stream()
                             .filter(item -> item.hasIssn(issn) || item.hasTitle(title))
                             .map(ChannelRegisterJournal::getIdentifier)
                             .collect(SingletonCollector.collectOrElse(null))
                       : null;
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

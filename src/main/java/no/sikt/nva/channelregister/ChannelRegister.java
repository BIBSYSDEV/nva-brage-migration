package no.sikt.nva.channelregister;

import static java.util.Objects.nonNull;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import no.sikt.nva.BrageProcessor;
import no.sikt.nva.model.BrageLocation;
import no.sikt.nva.model.dublincore.DublinCore;
import no.sikt.nva.model.record.Record;
import no.sikt.nva.scrapers.DublinCoreScraper;
import nva.commons.core.SingletonCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChannelRegister {

    public static final String KANALREGISTER_READING_ERROR_MESSAGE = "Fatal error, could not read kanalregister";
    private static final String JOURNAL_PATH = "journals.csv";
    private static final String PUBLISHERS_PATH = "publishers.csv";
    private static final char SEPARATOR = ';';
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    public static final String NOT_FOUND_IN_CHANNEL_REGISTER = "NOT_FOUND_IN_CHANNEL_REGISTER";
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

    public static List<ChannelRegisterPublisher> getPublishersFromCsv() {
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

    public String lookUpInChannelRegister(Record record) {
        var publisher = record.getPublication().getPublisher();
        var issn = record.getPublication().getIssn();
        if (isNotNullOrEmpty(publisher) && isNotNullOrEmpty(issn)) {
            var identifierFromJournal = lookUpInJournalByIssn(issn);
            if (isNotNullOrEmpty(identifierFromJournal)) {
                return identifierFromJournal;
            }
        }
        var isbn = record.getPublication().getIsbn();
        if (isNotNullOrEmpty(publisher) && isNotNullOrEmpty(isbn)) {
            var identifierFromPublisher = lookUpInPublisherByIsbn(isbn);
            if (isNotNullOrEmpty(identifierFromPublisher)) {
                return identifierFromPublisher;
            }
        }
        logger.warn(NOT_FOUND_IN_CHANNEL_REGISTER);
        return null;
    }

    public String lookUpInPublisherByIsbn(String isbn) {
        return isNotNullOrEmpty(isbn) ? channelRegisterPublishers.stream()
                                            .filter(item -> item.hasIsbn(isbn))
                                            .map(ChannelRegisterPublisher::getIdentifier)
                                            .collect(SingletonCollector.collectOrElse(null)) : null;
    }

    public String lookUpInJournalByIssn(String issn) {
        return isNotNullOrEmpty(issn) ? channelRegisterJournals.stream()
                                            .filter(item -> item.hasIssn(issn))
                                            .map(ChannelRegisterJournal::getIdentifier)
                                            .collect(SingletonCollector.collectOrElse(null)) : null;
    }

    public String lookUpInJournalByTitle(String title) {
        return isNotNullOrEmpty(title) ? channelRegisterJournals.stream()
                                             .filter(item -> item.hasTitle(title))
                                             .map(ChannelRegisterJournal::getIdentifier)
                                             .collect(SingletonCollector.collectOrElse(null)) : null;
    }

    public String extractIdentifier(DublinCore dublinCore, BrageLocation brageLocation) {
        var issn = DublinCoreScraper.extractIssn(dublinCore, brageLocation);
        var title = DublinCoreScraper.extractJournal(dublinCore);

        return isNotNullOrEmpty(issn) || isNotNullOrEmpty(title)
                   ? channelRegisterJournals.stream()
                         .filter(item -> item.hasIssn(issn) || item.hasTitle(title))
                         .map(ChannelRegisterJournal::getIdentifier)
                         .collect(SingletonCollector.collectOrElse(null))
                   : null;
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

    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    private boolean isNotNullOrEmpty(String candidate) {
        return nonNull(candidate) && !candidate.trim().isEmpty();
    }
}

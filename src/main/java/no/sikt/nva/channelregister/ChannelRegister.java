package no.sikt.nva.channelregister;

import static java.util.Objects.nonNull;
import static no.sikt.nva.brage.migration.common.model.ErrorDetails.Error.MULTIPLE_SEARCH_RESULTS_IN_CHANNEL_REGISTER_BY_VALUE;
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
import nva.commons.core.SingletonCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChannelRegister {

    public static final String KANALREGISTER_READING_ERROR_MESSAGE = "Fatal error, could not read kanalregister";
    public static final String NOT_FOUND_IN_CHANNEL_REGISTER = "NOT_FOUND_IN_CHANNEL_REGISTER: ";
    private static final String JOURNAL_PATH = "journals.csv";
    private static final char SEPARATOR = ';';
    private static final Logger logger = LoggerFactory.getLogger(BrageProcessor.class);
    /*volatile*/ private static ChannelRegister register;
    private final List<ChannelRegisterJournal> channelRegisterJournals;

    private ChannelRegister() {
        this.channelRegisterJournals = getJournalsFromCsv();
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

    public String lookUpInChannelRegister(Record record) {
        var publisher = record.getPublication().getPublisher();
        var issn = record.getPublication().getIssn();
        if (extractedIdentifierFromJournalsIsPresent(publisher, issn)) {
            return lookUpInJournalByIssn(issn);
        }
        return null;
    }

    public String lookUpInJournalByIssn(String issn) {
        try {
            if (!isNotNullOrEmpty(issn)) {
                return issn;
            } else {
                return channelRegisterJournals.stream()
                           .filter(item -> item.hasIssn(issn))
                           .map(ChannelRegisterJournal::getIdentifier)
                           .collect(SingletonCollector.collectOrElse(null));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public String lookUpInJournalByTitle(String title) {
        try {
            if (!isNotNullOrEmpty(title)) {
                return title;
            } else {
                return channelRegisterJournals.stream()
                           .filter(item -> item.hasTitle(title))
                           .map(ChannelRegisterJournal::getIdentifier)
                           .collect(SingletonCollector.collectOrElse(null));
            }
        } catch (IllegalStateException e) {
            logger.error(new ErrorDetails(MULTIPLE_SEARCH_RESULTS_IN_CHANNEL_REGISTER_BY_VALUE,
                                          filterOutNullValues(title)).toString());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String extractIdentifier(DublinCore dublinCore, BrageLocation brageLocation) {
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

    private boolean extractedIdentifierFromJournalsIsPresent(String publisher, String issn) {
        if (isNotNullOrEmpty(publisher) && isNotNullOrEmpty(issn)) {
            var identifierFromJournal = lookUpInJournalByIssn(issn);
            return isNotNullOrEmpty(String.valueOf(identifierFromJournal));
        }
        return false;
    }

    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    private boolean isNotNullOrEmpty(String candidate) {
        return nonNull(candidate) && !candidate.trim().isEmpty();
    }
}

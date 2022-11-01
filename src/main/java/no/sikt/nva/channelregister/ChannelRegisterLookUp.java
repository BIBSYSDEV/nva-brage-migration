package no.sikt.nva.channelregister;

import static java.util.Objects.nonNull;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import nva.commons.core.SingletonCollector;

public final class ChannelRegisterLookUp {

    public static final String JOURNAL_PATH = "src/main/resources/journals.csv";
    public static final char SEPARATOR = ';';

    public static final List<ChannelRegisterJournal> JOURNALS = getFromCsv();

    public static String lookUpForJournalByIssn(String issn) {
        return isNotNullOrEmpty(issn) ? JOURNALS.stream()
                                            .filter(item -> item.hasIssn(issn))
                                            .map(ChannelRegisterJournal::getIdentifier)
                                            .collect(SingletonCollector.collectOrElse(null)) : null;
    }

    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    private static boolean isNotNullOrEmpty(String candidate) {
        return nonNull(candidate) && !candidate.trim().isEmpty();
    }

    private static List<ChannelRegisterJournal> getFromCsv() {
        CsvTransfer csvTransfer = new CsvTransfer();

        try (Reader reader = Files.newBufferedReader(Path.of(JOURNAL_PATH))) {
            var microJournal = new CsvToBeanBuilder<ChannelRegisterJournal>(reader)
                                   .withSeparator(SEPARATOR)
                                   .withType(ChannelRegisterJournal.class)
                                   .build();
            csvTransfer.setCsvList(microJournal.parse());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return csvTransfer.getCsvList();
    }
}

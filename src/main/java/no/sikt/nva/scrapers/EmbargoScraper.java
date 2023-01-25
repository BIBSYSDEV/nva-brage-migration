package no.sikt.nva.scrapers;

import static no.sikt.nva.scrapers.ContentScraper.EMPTY_LINE_REGEX;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.common.model.record.Record;
import no.sikt.nva.brage.migration.common.model.record.content.ContentFile;
import no.sikt.nva.model.Embargo;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmbargoScraper {

    public static final String ERROR_OCCURRED_EXTRACTING_EMBARGOES = "ERROR_OCCURRED_EXTRACTING_EMBARGO";
    public static final String PDF_TXT = "pdf.txt";
    public static final String PDF_JPG = "pdf.jpg";
    public static final String PDF = "pdf";
    private static final Logger logger = LoggerFactory.getLogger(EmbargoScraper.class);

    public EmbargoScraper() {
    }

    public static List<Embargo> getEmbargoList(File file) throws IOException {
        var contentFileAsString = Files.readString(file.toPath());
        try {
            var embargoes = convertStringToEmbargoObjects(contentFileAsString);
            logger.info("FOLLOWING COLLECTION CONTAINS " + embargoes.size() + " EMBARGOES.");
            return embargoes;
        } catch (Exception e) {
            logger.error(ERROR_OCCURRED_EXTRACTING_EMBARGOES);
            return Collections.emptyList();
        }
    }

    public static Record checkForEmbargoFromSuppliedEmbargoFile(Record record, List<Embargo> embargoes) {
        var handle = record.getId().toString();
        if (containsHandle(embargoes, handle)) {
            var potentialEmbargo = embargoes.stream()
                                       .filter(embargo -> embargo.getHandle().equals(handle))
                                       .findAny().orElse(null);
            if (recordContainsEmbargoFile(record, Objects.requireNonNull(potentialEmbargo))) {
                record.getContentBundle()
                    .getContentFileByFilename(potentialEmbargo.getFilename())
                    .setEmbargoDate(potentialEmbargo.getDate());
            }
        }
        return record;
    }

    private static boolean containsHandle(List<Embargo> embargoes, String handle) {
        return embargoes.stream().map(Embargo::getHandle).collect(Collectors.toList()).contains(handle);
    }

    private static List<Embargo> convertStringToEmbargoObjects(String contentFileAsString) {
        var listOfStringObjects = Arrays.asList(
            contentFileAsString.replaceAll(EMPTY_LINE_REGEX, StringUtils.EMPTY_STRING)
                .replace("|", ";")
                .split("\n"));
        var embargoes = listOfStringObjects.stream()
                            .map(EmbargoScraper::convertToEmbargo)
                            .collect(Collectors.toList());
        return filterOutSameEmbargoes(embargoes);
    }

    private static Embargo convertToEmbargo(String string) {
        var embargoAsList = Arrays.asList(string.split(";"));
        String handle = embargoAsList.get(0).trim();
        String filename = embargoAsList.get(1).trim();
        String date = embargoAsList.get(2).trim();
        return new Embargo(handle, filename, date);
    }

    private static List<Embargo> filterOutSameEmbargoes(List<Embargo> embargoes) {
        List<Embargo> uniqueEmbargoesList = new ArrayList<>();
        for (Embargo embargo : embargoes) {
            var handlesList = uniqueEmbargoesList.stream()
                                  .map(Embargo::getHandle)
                                  .collect(Collectors.toList());
            if (!handlesList.contains(embargo.getHandle()) && isNotTextFileOrThumbnail(embargo.getFilename())) {
                uniqueEmbargoesList.add(embargo);
            }
        }
        return uniqueEmbargoesList;
    }

    private static boolean isNotTextFileOrThumbnail(String filename) {
        return !filename.contains(PDF_TXT)
               && !filename.contains(PDF_JPG);
    }

    private static boolean recordContainsEmbargoFile(Record record, Embargo potentialEmbargo) {
        return record.getContentBundle().getContentFiles().stream()
                   .map(ContentFile::getFilename)
                   .collect(Collectors.toList())
                   .contains(potentialEmbargo.getFilename());
    }
}

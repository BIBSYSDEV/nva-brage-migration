package no.sikt.nva;

import static no.sikt.nva.scrapers.ContentScraper.EMPTY_LINE_REGEX;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.model.Embargo;
import nva.commons.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmbargoScraper {

    public static final String ERROR_OCCURRED_EXTRACTING_EMBARGOES = "ERROR_OCCURRED_EXTRACTING_EMBARGO";
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
            return null;
        }
    }

    public static boolean containsHandle(List<Embargo> embargoes, String handle) {
        return embargoes.stream().map(Embargo::getHandle).collect(Collectors.toList()).contains(handle);
    }

    private static List<Embargo> convertStringToEmbargoObjects(String contentFileAsString) {
        var listOfStringObjects = Arrays.asList(
            contentFileAsString.replaceAll(EMPTY_LINE_REGEX, StringUtils.EMPTY_STRING).split("\n"));
        var embargoes = listOfStringObjects.stream()
                            .map(EmbargoScraper::convertToEmbargo)
                            .collect(Collectors.toList());
        return filterOutSameEmbargoes(embargoes);
    }

    private static Embargo convertToEmbargo(String string) {
        var embargoAsList = Arrays.asList(string.split(";"));
        String handle = embargoAsList.get(0);
        String filename = embargoAsList.get(1);
        String date = embargoAsList.get(2);
        return new Embargo(handle, filename, date);
    }

    private static List<Embargo> filterOutSameEmbargoes(List<Embargo> embargoes) {
        List<Embargo> uniqueEmbargoesList = new ArrayList<>();
        for (Embargo embargo : embargoes) {
            var handlesList = uniqueEmbargoesList.stream()
                                  .map(Embargo::getHandle)
                                  .collect(Collectors.toList());
            if (!handlesList.contains(embargo.getHandle()) && isPdfFile(embargo.getFilename())) {
                uniqueEmbargoesList.add(embargo);
            }
        }
        return uniqueEmbargoesList;
    }

    private static boolean isPdfFile(String filename) {
        return !filename.contains("pdf.txt")
               && !filename.contains("pdf.jpg")
               && filename.contains("pdf")
               && filename.split("\\.").length == 2;
    }
}

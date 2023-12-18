package no.sikt.nva.scrapers;

import static no.sikt.nva.scrapers.ContentScraper.EMPTY_LINE_REGEX;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.sikt.nva.brage.migration.aws.ColoredLogger;
import no.sikt.nva.model.Embargo;
import nva.commons.core.StringUtils;

public final class EmbargoScraper {

    public static final String ERROR_OCCURRED_EXTRACTING_EMBARGOES =
        "ERROR_OCCURRED_EXTRACTING_EMBARGO, embargoes will not be attached to publications";
    public static final String EMPTY_EMBARGO_FILE_MESSAGE = "EMBARGO_FILE_IS_EMPTY, embargoes will not be attached to"
                                                            + " publications";

    private static final ColoredLogger logger = ColoredLogger.create(EmbargoScraper.class);

    public EmbargoScraper() {
    }

    public static Map<String, List<Embargo>> getEmbargoes(File file) {
        try {
            var contentFileAsString = Files.readString(file.toPath());
            var embargoes = convertStringToEmbargoObjects(contentFileAsString);
            logger.info("FOLLOWING COLLECTION CONTAINS " + embargoes.size() + " EMBARGOES.");
            return embargoes;
        } catch (Exception e) {
            if (file.exists()) {
                if (fileIsEmpty(file)) {
                    logger.error(EMPTY_EMBARGO_FILE_MESSAGE);
                }
                logger.error(ERROR_OCCURRED_EXTRACTING_EMBARGOES);
            }
            return new HashMap<>();
        }
    }

    private static boolean fileIsEmpty(File file) {
        return file.length() == 0;
    }

    private static Map<String, List<Embargo>> convertStringToEmbargoObjects(String contentFileAsString) {
        var listOfStringObjects = Arrays.asList(contentFileAsString.replaceAll(EMPTY_LINE_REGEX, StringUtils.EMPTY_STRING)
                                                                    .replace("|", ";")
                                                                    .split("\n"));
        var embargoes = removeIgnoredLines(listOfStringObjects).stream()
                            .map(EmbargoScraper::convertToEmbargo)
                            .collect(Collectors.toList());
        return createEmbargoHandleMap(embargoes);
    }

    private static List<String> removeIgnoredLines(List<String> listOfStringObjects) {
        var list = new ArrayList<>(listOfStringObjects);
        list.remove(0);
        list.remove(0);
        list.remove(list.size() - 1);
        return list;
    }

    private static Embargo convertToEmbargo(String string) {
        var embargoAsList = Arrays.asList(string.split(";"));
        String handle = embargoAsList.get(0).trim();
        String filename = embargoAsList.get(1).trim();
        String date = embargoAsList.get(2).trim();
        return new Embargo(handle, filename, date);
    }

    private static Map<String, List<Embargo>> createEmbargoHandleMap(List<Embargo> embargoes) {
        var embargoesByHandle = new HashMap<String, List<Embargo>>();
        embargoes.forEach(embargo -> addEmbargoToMap(embargoesByHandle, embargo));
        return embargoesByHandle;
    }

    private static void addEmbargoToMap(Map<String, List<Embargo>> embargoMap,
                                        Embargo embargo) {
        List<Embargo> embargoList;
        if (embargoMap.containsKey(embargo.getHandle())) {
            embargoList = embargoMap.get(embargo.getHandle());
            embargoList.add(embargo);
        } else {
            embargoList = new ArrayList<>();
            embargoList.add(embargo);
        }
        embargoMap.put(embargo.getHandle(), embargoList);
    }
}

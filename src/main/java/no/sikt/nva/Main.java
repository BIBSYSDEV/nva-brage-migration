package no.sikt.nva;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import no.sikt.nva.exceptions.DublinCoreException;
import no.sikt.nva.model.record.Record;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.ShortClassName")
@JacocoGenerated
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        UnZipper unZipper = new UnZipper();
        DublinCoreParser dublinCoreParser = new DublinCoreParser();
        var fileToUnzip = IoUtils.inputStreamFromResources("inputWithCristinId.zip");
        var destinationFile = new File("destinationDirectory");
        var unzippedFile = unZipper.unzip(fileToUnzip, destinationFile);
        var resourceDirectories = Arrays.stream(Objects.requireNonNull(unzippedFile.listFiles()))
                                      .collect(Collectors.toList());

        List<Record> records = new ArrayList<>();

        for (File entryDirectory : resourceDirectories) {
            if (entryDirectory.isDirectory()) {
                for (File file : Objects.requireNonNull(entryDirectory.listFiles())) {
                    try {
                        if ("dublin_core.xml".equals(file.getName())) {
                            var record = dublinCoreParser.parseDublinCoreToRecord(file);
                            records.add(record);
                        }
                    } catch (DublinCoreException e) {
                        logger.info(e.getMessage());
                    }
                }
            }
        }
        System.out.println(records);
    }
}
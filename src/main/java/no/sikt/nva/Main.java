package no.sikt.nva;

import jakarta.xml.bind.JAXBException;
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

@JacocoGenerated
public class Main {

    public static void main(String[] args) throws JAXBException, DublinCoreException {
        UnZipper unZipper = new UnZipper();
        DublinCoreParser dublinCoreParser = new DublinCoreParser();
        var fileToUnzip = IoUtils.inputStreamFromResources("testinput.zip");
        var destinationFile = new File("destinationDirectory");
        var unzippedFile = unZipper.unzip(fileToUnzip, destinationFile);
        var resourceDirectories = Arrays.stream(Objects.requireNonNull(unzippedFile.listFiles()))
                                      .collect(Collectors.toList());

        List<Record> records = new ArrayList<>();

        for (File entryDirectory : resourceDirectories) {
            if (entryDirectory.isDirectory()) {
                for (File file : Objects.requireNonNull(entryDirectory.listFiles())) {
                    if (file.getName().equals("dublin_core.xml")) {
                        var record = dublinCoreParser.parseDublinCoreToRecord(file);
                        records.add(record);
                    }
                }
            }
        }
        System.out.println(records);
    }
}
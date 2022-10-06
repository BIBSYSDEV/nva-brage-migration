package no.sikt.nva;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import nva.commons.core.JacocoGenerated;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@SuppressWarnings({"PMD.DoNotUseThreads"})
@JacocoGenerated
@Command(
    name = "Brage migration",
    description = "Tool for migrating Brage bundles"
)
public class BrageMigrationCommand implements Callable<Integer> {

    public static final String PATH_DELIMITER = "/";
    public static final String OUTPUT_JSON_FILENAME = "records.json";

    @Option(names = {"-c", "--customer"}, required = true, description = "customer id in NVA")
    private String customer;

    @Option(names = {"-z",
        "--zip-files"}, arity = "1..*", description = "input zipfiles containing brage bundles")
    private String[] zipFiles;

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        var brageProcessors = createBrageProcessorThread(zipFiles, customer);
        var brageProcessorThreads = brageProcessors.stream().map(Thread::new).collect(Collectors.toList());
        startProcessors(brageProcessorThreads);
        waitForAllProcesses(brageProcessorThreads);
        writeRecordsToFiles(brageProcessors);
        System.out.println("hello world " + customer + " " + String.join(" ", zipFiles));
        return 0;
    }

    private void writeRecordsToFiles(List<BrageProcessor> brageProcessors) {
        brageProcessors.forEach(brageProcessor -> writeRecordToFile(brageProcessor));
    }

    private void writeRecordToFile(BrageProcessor brageProcessor) {
        var outputFileName = brageProcessor.getDestinationDirectory() + PATH_DELIMITER + OUTPUT_JSON_FILENAME;
        RecordsWriter.writeRecordsToFile(outputFileName, brageProcessor.getRecords());
    }

    private void waitForAllProcesses(List<Thread> brageProcessors) {
        brageProcessors.forEach(brageProcessor -> {
            try {
                brageProcessor.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void startProcessors(List<Thread> brageProcessors) {
        brageProcessors.forEach(Thread::start);
    }

    private List<BrageProcessor> createBrageProcessorThread(String[] zipFiles, String customer) {
        return
            Arrays.stream(zipFiles)
                .map(zipfile -> BrageProcessorFactory.createBrageProcessor(zipfile, customer))
                .collect(Collectors.toList());
    }
}

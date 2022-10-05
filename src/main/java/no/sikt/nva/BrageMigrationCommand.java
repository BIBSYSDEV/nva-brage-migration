package no.sikt.nva;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(BrageMigrationCommand.class);

    @Option(names = {"-c", "--customer"}, required = true, description = "customer id in NVA")
    private String customer;

    @Option(names = {"-z", "--zip-files"}, required = true, description = "input zipfiles containing brage bundles")
    private String[] zipFiles;

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        logger.info("hello from CLI");
        var brageProcessors = createBrageProcessorThread(zipFiles, customer);
        startProcessors(brageProcessors);
        waitForAllprocesses(brageProcessors);
        System.out.println("hello world " + customer + " " + String.join(" ", zipFiles));
        return 0;
    }

    private void waitForAllprocesses(List<Thread> brageProcessors) {
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

    private List<Thread> createBrageProcessorThread(String[] zipFiles, String customer) {
        return
            Arrays.stream(zipFiles)
                .map(zipfile -> BrageProcessorFactory.createBrageProcessor(zipfile, customer))
                .map(Thread::new).collect(Collectors.toList());
    }
}

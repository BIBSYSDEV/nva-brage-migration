package no.sikt.nva;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "Brage migration",
    description = "Tool for migrating Brage bundles"
)
public class BrageMigrationCommand implements Callable<Integer> {

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
        System.out.println("hello world " + customer + " " + String.join(" ", zipFiles));
        return 0;
    }
}

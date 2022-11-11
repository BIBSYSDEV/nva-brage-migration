package no.sikt.nva;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import no.sikt.nva.scrapers.DublinCoreScraper;
import no.sikt.nva.scrapers.HandleTitleMapReader;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UriWrapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@SuppressWarnings({"PMD.DoNotUseThreads"})
@JacocoGenerated
@Command(
    name = "Brage migration",
    description = "Tool for migrating Brage bundles"
)
public class BrageMigrationCommand implements Callable<Integer> {

    public static final String PATH_DELIMITER = "/";
    public static final String OUTPUT_JSON_FILENAME = "records.json";
    public static final String FAILURE_IN_BRAGE_MIGRATION_COMMAND = "Failure in BrageMigration command";

    public static final String FOLLOWING_FIELDS_ARE_IGNORED = "The following fields are ignored: \n";
    public static final String INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY =
        "Both specified zipfiles and starting directory cannot be set at "
        + "the same time";
    private static final int NORMAL_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 2;
    private static final String NVE_DEV_CUSTOMER_ID =
        "https://api.dev.nva.aws.unit.no/customer/b4497570-2903-49a2-9c2a-d6ab8b0eacc2";
    private static final String COLLECTION_FILENAME = "samlingsfil.txt";
    private static final String ZIP_FILE_ENDING = ".zip";
    @Option(names = {"-c", "--customer"},
        defaultValue = NVE_DEV_CUSTOMER_ID,
        description = "customer id in NVA")
    private String customer;

    @Option(names = {"-ov", "--online-validator"}, description = "enable online validator, disabled if not present")
    private boolean enableOnlineValidation;

    @Parameters(description = "input zipfiles containing brage bundles, if none specified "
                              + "all zipfiles will be read based on samlingsfil.txt")
    private String[] zipFiles;

    @Option(names = {"-D", "--directory"}, description = "Directory to search for samlingsfil.txt. This option cannot"
                                                         + " be set at the same time as specified zipfiles")
    private String startingDirectory;

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    @Option(names = {"-O", "--output-directory"}, description = "result outputdirectory.")
    private String userSpecifiedOutputDirectory;

    @Option(names = {"-no-handle-erros"}, description = "turn off handle errors. Invalid and missing handles does not"
                                                        + " get checked")
    private boolean noHandleCheck;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BrageMigrationCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {

            checkForIllegalArguments();
            var inputDirectory = StringUtils.isNotEmpty(startingDirectory)
                                     ? startingDirectory + "/"
                                     : StringUtils.EMPTY_STRING;
            var outputDirectory = StringUtils.isNotEmpty(userSpecifiedOutputDirectory)
                                      ? userSpecifiedOutputDirectory + "/"
                                      : inputDirectory;
            setupLogging(outputDirectory);
            if (Objects.isNull(zipFiles)) {
                this.zipFiles = readZipFileNamesFromCollectionFile(inputDirectory);
            }
            var customerUri = UriWrapper.fromUri(customer).getUri();
            printIgnoredDcValuesFieldsInInfoLog();
            var brageProcessors = createBrageProcessorThread(zipFiles,
                                                             customerUri,
                                                             enableOnlineValidation,
                                                             noHandleCheck,
                                                             outputDirectory);
            var brageProcessorThreads = brageProcessors.stream().map(Thread::new).collect(Collectors.toList());
            startProcessors(brageProcessorThreads);
            waitForAllProcesses(brageProcessorThreads);
            writeRecordsToFiles(brageProcessors);
            return NORMAL_EXIT_CODE;
        } catch (Exception e) {
            var logger = LoggerFactory.getLogger(BrageProcessor.class);
            logger.error(FAILURE_IN_BRAGE_MIGRATION_COMMAND, e);
            return ERROR_EXIT_CODE;
        }
    }

    private static String[] readZipFileNamesFromCollectionFile(String inputDirectory) {
        var zipfiles = new ArrayList<String>();
        var filenameWithPath = inputDirectory + COLLECTION_FILENAME;
        File collectionsInformationFile = new File(filenameWithPath);
        try (var scanner = new Scanner(collectionsInformationFile)) {
            while (scanner.hasNextLine()) {
                var fileNamePartial = scanner.nextLine();
                if (StringUtils.isNotEmpty(fileNamePartial)) {
                    zipfiles.add(inputDirectory + fileNamePartial + ZIP_FILE_ENDING);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return zipfiles.toArray(new String[0]);
    }

    private static AppenderComponentBuilder createConsoleAppender(
        ConfigurationBuilder<BuiltConfiguration> builder, LayoutComponentBuilder standardPattern) {

        return builder
                   .newAppender("console", "Console")
                   .addComponent(standardPattern);
    }

    @NotNull
    private static AppenderComponentBuilder createHtmlInfoAppender(String outputDirectory,
                                                                   ConfigurationBuilder<BuiltConfiguration> builder) {
        ComponentBuilder triggeringPolicy =
            builder
                .newComponent("Policies")
                .addComponent(
                    builder
                        .newComponent("TimeBasedTriggeringPolicy")
                        .addAttribute("interval", "1")
                        .addAttribute("modulate", "true"))
                .addComponent(
                    builder
                        .newComponent("SizeBasedTriggeringPolicy")
                        .addAttribute("size", "10 MB"));
        ComponentBuilder htmlLayout =
            builder
                .newLayout("HTMLLayout")
                .addAttribute("charset", "UTF-8")
                .addAttribute("title", "Brage migration results");
        ComponentBuilder infoFilter =
            builder.newFilter("LevelRangeFilter", "ACCEPT", "DENY")
                .addAttribute("minLevel", "INFO")
                .addAttribute("maxLevel", "INFO");

        AppenderComponentBuilder rollingFile =
            builder.newAppender("htmlLogger", "RollingFile");
        rollingFile.addAttribute("fileName", outputDirectory + "app-info2.html");
        rollingFile.addAttribute("filePattern", "app-info2-%d{yyyy-MM-dd}.html");
        rollingFile.addComponent(infoFilter);
        rollingFile.addComponent(triggeringPolicy);
        rollingFile.addComponent(htmlLayout);
        return rollingFile;
    }

    private static void setupLogging(String outputDirectory) {
        ConfigurationBuilder<BuiltConfiguration> builder
            = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.addProperty("status", "INFO");
        createAppenders(outputDirectory, builder);
        try {
            builder.writeXmlConfiguration(System.out);
            Configurator.initialize(builder.build());
        } catch (IOException e) {
            System.out.println("Could not setup logs properly: " + e.getMessage());
        }
    }

    private static void createAppenders(String outputDirectory, ConfigurationBuilder<BuiltConfiguration> builder) {
        LayoutComponentBuilder standardPattern =
            builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%m%n");
        ComponentBuilder defaultRolloverStrategy =
            builder.newComponent("DefaultRolloverStrategy")
                .addAttribute("max", "10");
        ComponentBuilder policies =
            builder.newComponent("Policies")
                .addComponent(builder
                                  .newComponent("SizeBasedTriggeringPolicy")
                                  .addAttribute("size", "19500KB"));
        AppenderComponentBuilder console = createConsoleAppender(
            builder, standardPattern);
        AppenderComponentBuilder htmlInfoAppender = createHtmlInfoAppender(
            outputDirectory, builder);
        AppenderComponentBuilder logWarnAppender = createLogWarnAppender(outputDirectory,
                                                                         builder,
                                                                         standardPattern,
                                                                         defaultRolloverStrategy,
                                                                         policies);
        AppenderComponentBuilder errorAppender = createErrorAppender(outputDirectory,
                                                                     builder,
                                                                     standardPattern,
                                                                     defaultRolloverStrategy,
                                                                     policies);

        RootLoggerComponentBuilder rootLogger =
            builder.newRootLogger(Level.INFO);
        rootLogger.add(builder.newAppenderRef("console"));

        LoggerComponentBuilder logger = builder
                                            .newLogger("no.sikt.nva")
                                            .addAttribute("additivity", "true")
                                            .add(builder.newAppenderRef("htmlLogger"))
                                            .add(builder.newAppenderRef("warnLog"))
                                            .add(builder.newAppenderRef("errorLog"));

        builder.add(errorAppender);
        builder.add(logWarnAppender);
        builder.add(htmlInfoAppender);
        builder.add(console);
        builder.add(rootLogger);
        builder.add(logger);
    }

    private static AppenderComponentBuilder createErrorAppender(String outputDirectory,
                                                         ConfigurationBuilder<BuiltConfiguration> builder,
                                                         LayoutComponentBuilder standardPattern,
                                                         ComponentBuilder defaultRolloverStrategy,
                                                         ComponentBuilder policies) {
        ComponentBuilder<FilterComponentBuilder> errorFilter =
            builder.newFilter("LevelRangeFilter", "ACCEPT", "DENY")
                .addAttribute("minLevel", "ERROR")
                .addAttribute("maxLevel", "ERROR");

        AppenderComponentBuilder warnAppender =
            builder.newAppender("errorLog", "RollingFile");
        warnAppender.addAttribute("fileName", outputDirectory + "application-error.log");
        warnAppender.addAttribute("filePattern", "application-error-%d{yyyy-MM-dd}-%i.log");
        warnAppender.addComponent(errorFilter);
        warnAppender.addComponent(policies);
        warnAppender.addComponent(standardPattern);
        warnAppender.addComponent(defaultRolloverStrategy);
        return warnAppender;
    }

    private static AppenderComponentBuilder createLogWarnAppender(String outputDirectory,
                                                           ConfigurationBuilder<BuiltConfiguration> builder,
                                                           LayoutComponentBuilder standardPattern,
                                                           ComponentBuilder defaultRolloverStrategy,
                                                           ComponentBuilder policies) {

        ComponentBuilder<FilterComponentBuilder> warnFilter =
            builder.newFilter("LevelRangeFilter", "ACCEPT", "DENY")
                .addAttribute("minLevel", "WARN")
                .addAttribute("maxLevel", "WARN");

        AppenderComponentBuilder warnAppender =
            builder.newAppender("warnLog", "RollingFile");
        warnAppender.addAttribute("fileName", outputDirectory + "application-warn.log");
        warnAppender.addAttribute("filePattern", "application-warn-%d{yyyy-MM-dd}-%i.log");
        warnAppender.addComponent(warnFilter);
        warnAppender.addComponent(policies);
        warnAppender.addComponent(standardPattern);
        warnAppender.addComponent(defaultRolloverStrategy);
        return warnAppender;
    }

    private void checkForIllegalArguments() {
        if (Objects.nonNull(zipFiles) && zipFiles.length > 0 && StringUtils.isNotEmpty(startingDirectory)) {
            throw new IllegalArgumentException(INCOMPATIBLE_ARGUMENTS_ZIPFILE_AND_INPUT_DIRECTORY);
        }
    }

    private void printIgnoredDcValuesFieldsInInfoLog() {
        var logger = LoggerFactory.getLogger(BrageProcessor.class);
        logger.info(FOLLOWING_FIELDS_ARE_IGNORED + DublinCoreScraper.getIgnoredFieldNames());
    }

    private void writeRecordsToFiles(List<BrageProcessor> brageProcessors) {
        brageProcessors.forEach(this::writeRecordToFile);
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

    private List<BrageProcessor> createBrageProcessorThread(String[] zipFiles, URI customer,
                                                            boolean enableOnlineValidation, boolean noHandleCheck,
                                                            String outputDirectory) {
        var handleTitleMapReader = new HandleTitleMapReader();
        var brageProcessorFactory = new BrageProcessorFactory(handleTitleMapReader.readNveTitleAndHandlesPatch());
        return
            Arrays.stream(zipFiles)
                .map(zipfile -> brageProcessorFactory.createBrageProcessor(zipfile, customer, enableOnlineValidation,
                                                                           noHandleCheck, outputDirectory))
                .collect(Collectors.toList());
    }
}

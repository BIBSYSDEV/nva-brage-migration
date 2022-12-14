package no.sikt.nva.logutils;

//Followed this guide: https://www.baeldung.com/log4j2-programmatic-config

import static no.sikt.nva.logutils.LogConstants.FILE_NAME;
import static no.sikt.nva.logutils.LogConstants.FILE_PATTERN;
import static no.sikt.nva.logutils.LogConstants.MAX_LEVEL;
import static no.sikt.nva.logutils.LogConstants.MIN_LEVEL;
import static no.sikt.nva.logutils.LogConstants.PATTERN_LAYOUT;
import static no.sikt.nva.logutils.LogConstants.ROLLING_FILE;
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

public final class LogSetup {

    private static final String CONSOLE_APPENDER_NAME = "console";
    private static final String WARN_APPENDER_NAME = "warnLog";
    private static final String ERROR_APPENDER_NAME = "errorLog";
    private static final String HTML_APPENDER_NAME = "htmlLogger";

    private LogSetup() {

    }

    public static void setupLogging(String outputDirectory) {
        ConfigurationBuilder<BuiltConfiguration> builder
            = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.addProperty("status", Level.INFO.toString());
        createAppenders(outputDirectory, builder);
        Configurator.initialize(builder.build());
    }

    private static void createAppenders(String outputDirectory, ConfigurationBuilder<BuiltConfiguration> builder) {
        LayoutComponentBuilder standardPattern =
            builder.newLayout(PATTERN_LAYOUT)
                .addAttribute("pattern", "%m%n");
        ComponentBuilder defaultRolloverStrategy =
            builder.newComponent("DefaultRolloverStrategy")
                .addAttribute("max", "10");
        ComponentBuilder policies =
            builder.newComponent("Policies")
                .addComponent(builder
                                  .newComponent("SizeBasedTriggeringPolicy")
                                  .addAttribute("size", "19500KB"));

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
        rootLogger.add(builder.newAppenderRef(CONSOLE_APPENDER_NAME));

        LoggerComponentBuilder logger = builder
                                            .newLogger("no.sikt.nva")
                                            .addAttribute("additivity", "true")
                                            .add(builder.newAppenderRef(ERROR_APPENDER_NAME))
                                            .add(builder.newAppenderRef(WARN_APPENDER_NAME))
                                            .add(builder.newAppenderRef(HTML_APPENDER_NAME));

        AppenderComponentBuilder htmlInfoAppender = createHtmlInfoAppender(
            outputDirectory, builder);
        AppenderComponentBuilder console = createConsoleAppender(
            builder, standardPattern);

        builder.add(errorAppender);
        builder.add(logWarnAppender);
        builder.add(htmlInfoAppender);
        builder.add(console);
        builder.add(rootLogger);
        builder.add(logger);
    }

    private static AppenderComponentBuilder createConsoleAppender(
        ConfigurationBuilder<BuiltConfiguration> builder, LayoutComponentBuilder standardPattern) {

        return builder
                   .newAppender(CONSOLE_APPENDER_NAME, "Console")
                   .addComponent(standardPattern);
    }

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
                .addAttribute(MIN_LEVEL, Level.INFO)
                .addAttribute(MAX_LEVEL, Level.INFO);

        AppenderComponentBuilder rollingFile =
            builder.newAppender(HTML_APPENDER_NAME, ROLLING_FILE);
        rollingFile.addAttribute(FILE_NAME, outputDirectory + "app-info2.html");
        rollingFile.addAttribute(FILE_PATTERN, "app-info2-%d{yyyy-MM-dd}.html");
        rollingFile.addComponent(infoFilter);
        rollingFile.addComponent(triggeringPolicy);
        rollingFile.addComponent(htmlLayout);
        return rollingFile;
    }

    private static AppenderComponentBuilder createLogWarnAppender(String outputDirectory,
                                                                  ConfigurationBuilder<BuiltConfiguration> builder,
                                                                  LayoutComponentBuilder standardPattern,
                                                                  ComponentBuilder defaultRolloverStrategy,
                                                                  ComponentBuilder policies) {

        ComponentBuilder<FilterComponentBuilder> warnFilter =
            builder.newFilter("LevelRangeFilter", "ACCEPT", "DENY")
                .addAttribute(MIN_LEVEL, Level.WARN)
                .addAttribute(MAX_LEVEL, Level.WARN);

        AppenderComponentBuilder warnAppender =
            builder.newAppender(WARN_APPENDER_NAME, ROLLING_FILE);
        warnAppender.addAttribute(FILE_NAME, outputDirectory + "application-warn.log");
        warnAppender.addAttribute(FILE_PATTERN, "application-warn-%d{yyyy-MM-dd}-%i.log");
        warnAppender.addComponent(warnFilter);
        warnAppender.addComponent(policies);
        warnAppender.addComponent(standardPattern);
        warnAppender.addComponent(defaultRolloverStrategy);
        return warnAppender;
    }

    private static AppenderComponentBuilder createErrorAppender(String outputDirectory,
                                                                ConfigurationBuilder<BuiltConfiguration> builder,
                                                                LayoutComponentBuilder standardPattern,
                                                                ComponentBuilder defaultRolloverStrategy,
                                                                ComponentBuilder policies) {
        ComponentBuilder<FilterComponentBuilder> errorFilter =
            builder.newFilter("LevelRangeFilter", "ACCEPT", "DENY")
                .addAttribute(MIN_LEVEL, Level.ERROR)
                .addAttribute(MAX_LEVEL, Level.ERROR);

        AppenderComponentBuilder warnAppender =
            builder.newAppender(ERROR_APPENDER_NAME, ROLLING_FILE);
        warnAppender.addAttribute(FILE_NAME, outputDirectory + "application-error.log");
        warnAppender.addAttribute(FILE_PATTERN, "application-error-%d{yyyy-MM-dd}-%i.log");
        warnAppender.addComponent(errorFilter);
        warnAppender.addComponent(policies);
        warnAppender.addComponent(standardPattern);
        warnAppender.addComponent(defaultRolloverStrategy);
        return warnAppender;
    }
}

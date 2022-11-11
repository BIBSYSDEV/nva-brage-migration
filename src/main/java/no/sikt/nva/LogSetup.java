package no.sikt.nva;

import java.io.IOException;
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

    private LogSetup() {

    }

    public static void setupLogging(String outputDirectory) {
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

    private static AppenderComponentBuilder createConsoleAppender(
        ConfigurationBuilder<BuiltConfiguration> builder, LayoutComponentBuilder standardPattern) {

        return builder
                   .newAppender("console", "Console")
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
}

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
        System.setProperty("logFilename", "someFile");

    }
}

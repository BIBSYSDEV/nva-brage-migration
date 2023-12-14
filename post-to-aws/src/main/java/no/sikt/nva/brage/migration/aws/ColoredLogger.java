package no.sikt.nva.brage.migration.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColoredLogger {

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String GHOST_EMOJI = "\uD83D\uDC7B ";
    public static final String HEART_EMOJI = "\uD83D\uDC9A ";
    private final Logger logger;
    public ColoredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static ColoredLogger create(Class<?> clazz) {
        return new ColoredLogger(clazz);
    }

    public void info(String value) {
        logger.info(HEART_EMOJI + ANSI_GREEN + value + ANSI_RESET);
    }

    public void error(String value) {
        logger.error(GHOST_EMOJI + ANSI_RED + value + ANSI_RESET);
    }

    public void warn(String value) {
        logger.error(GHOST_EMOJI + ANSI_RED + value + ANSI_RESET);
    }
}

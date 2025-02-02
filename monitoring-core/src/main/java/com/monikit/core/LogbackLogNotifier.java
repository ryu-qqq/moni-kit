package com.monikit.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackLogNotifier {

    private static final Logger logger = LoggerFactory.getLogger(LogbackLogNotifier.class);

    private LogbackLogNotifier() {
    }

    public static void notify(LogLevel logLevel, String message) {
        switch (logLevel) {
            case ERROR:
                logger.error(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case TRACE:
                logger.trace(message);
                break;
            default:
                logger.info(message);
                break;
        }
    }
}

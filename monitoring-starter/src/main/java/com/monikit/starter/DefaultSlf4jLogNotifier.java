package com.monikit.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.monikit.core.LogEntry;
import com.monikit.core.LogLevel;
import com.monikit.core.LogNotifier;

/**
 * slf4j 기본 LogNotifier 구현체.
 * <p>
 * - LogLevel에 따라 적절한 로그 메서드를 호출함.
 * - JSON 형식이 깨지지 않도록 메시지를 변형하지 않고 그대로 출력.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */

public class DefaultSlf4jLogNotifier implements LogNotifier {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSlf4jLogNotifier.class);

    @Override
    public void notify(LogLevel logLevel, String message) {
        switch (logLevel) {
            case ERROR -> logger.error(message);
            case WARN -> logger.warn(message);
            case DEBUG -> logger.debug(message);
            case TRACE -> logger.trace(message);
            default -> logger.info(message);
        }
    }

    /**
     * Sends a log entry after converting it to JSON.
     *
     * @param logEntry LogEntry object to be logged
     */
    public void notify(LogEntry logEntry) {
        try {
            String jsonLog = LogEntryJsonConverter.toJson(logEntry);
            notify(logEntry.getLogLevel(), jsonLog);
        } catch (RuntimeException e) {
            logger.error("Failed to serialize LogEntry: {}", e.getMessage(), e);
            notify(LogLevel.ERROR, "Log serialization failed: " + e.getMessage());
        }
    }

}
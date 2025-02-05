package com.monikit.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.monikit.core.LogEntry;
import com.monikit.core.LogLevel;
import com.monikit.core.LogNotifier;

/**
 * Logback을 사용하여 로그를 출력하는 LogNotifier 구현체.
 * <p>
 * - Logback을 기반으로 로그를 남김.
 * - LogLevel에 따라 적절한 로그 메서드를 호출함.
 * - JSON 형식이 깨지지 않도록 메시지를 변형하지 않고 그대로 출력.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
public class LogbackLogNotifier implements LogNotifier {

    private static final Logger logger = LoggerFactory.getLogger(LogbackLogNotifier.class);

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
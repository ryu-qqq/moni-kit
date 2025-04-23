package com.monikit.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.monikit.core.LogEntry;
import com.monikit.core.LogSink;
import com.monikit.core.LogType;

public class Slf4jLogSink implements LogSink {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jLogSink.class);

    @Override
    public boolean supports(LogType logType) {
        return true;
    }

    @Override
    public void send(LogEntry logEntry) {
        switch (logEntry.getLogLevel()) {
            case INFO -> logger.info(logEntry.toString());
            case WARN -> logger.warn(logEntry.toString());
            case ERROR -> logger.error(logEntry.toString());
            default -> logger.debug(logEntry.toString());
        }
    }
}

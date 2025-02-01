package com.monikit.core;

import java.time.Instant;

public class DefaultLogFormat implements LogFormat {
    private final Instant timestamp;
    private final LogLevel level;
    private final String className;
    private final String traceId;

    public DefaultLogFormat(LogLevel level, String className, String traceId) {
        this.timestamp = Instant.now();
        this.level = level;
        this.className = className;
        this.traceId = traceId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getTraceId() {
        return traceId;
    }

}
package com.monikit.slf4j;

import java.util.Collections;
import java.util.List;

import com.monikit.core.LogEntry;
import com.monikit.core.LogLevel;
import com.monikit.core.LogNotifier;
import com.monikit.core.LogSink;

public class Slf4jLogger implements LogNotifier {

    private final List<LogSink> sinks;

    public Slf4jLogger(List<LogSink> sinks) {
        this.sinks = sinks;
    }

    @Override
    public void notify(LogLevel logLevel, String message) {

        sinks.forEach(sink -> sink.send(() -> "[" + logLevel + "] " + message));
    }

    @Override
    public void notify(LogEntry logEntry) {
        for (LogSink sink : sinks) {
            if (sink.supports(logEntry.getLogType())) {
                sink.send(logEntry);
            }
        }
    }

}

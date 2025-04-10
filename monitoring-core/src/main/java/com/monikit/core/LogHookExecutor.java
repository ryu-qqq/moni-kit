package com.monikit.core;

import java.util.List;

public interface LogHookExecutor {
    void executeAdd(LogAddHook hook, LogEntry entry);
    void executeFlush(LogFlushHook hook, List<LogEntry> entries);
}

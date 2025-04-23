package com.monikit.core;

import java.util.List;

public interface LogSinkCustomizer {
    void customize(List<LogSink> sinks);
}

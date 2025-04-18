package com.monikit.core;

import java.util.List;

public interface MetricCollectorCustomizer {
    void customize(List<MetricCollector<? extends LogEntry>> collectors);
}

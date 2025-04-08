package com.monikit.core;

public interface TraceIdProvider {
    String getTraceId();
    void setTraceId(String traceId);
    void clear();
}

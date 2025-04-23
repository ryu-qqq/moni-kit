package com.monikit.starter;

import org.slf4j.MDC;

import com.monikit.core.TraceIdProvider;

public class MDCTraceIdProvider implements TraceIdProvider  {

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null) {
            traceId = "N/A";
        }
        return traceId;
    }

    @Override
    public void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);

    }

    @Override
    public void clear() {
        MDC.remove(TRACE_ID_KEY);
    }


}

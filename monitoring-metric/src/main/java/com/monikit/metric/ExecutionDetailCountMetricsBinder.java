package com.monikit.metric;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * 메서드 호출 횟수를 기록하는 `MeterBinder`
 */

public class ExecutionDetailCountMetricsBinder implements MeterBinder {

    private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    private MeterRegistry meterRegistry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;

    }

    /**
     * 동적으로 Counter를 기록하는 메서드
     */
    public void record(String className, String methodName, String tag) {
        String key = className + "." + methodName + "." + tag;

        Counter counter = counterCache.computeIfAbsent(key, k -> Counter.builder("execution_count")
            .description("Number of method executions")
            .tag("class", className)
            .tag("method", methodName)
            .tag("tag", tag)
            .register(meterRegistry));
        counter.increment();
    }
}

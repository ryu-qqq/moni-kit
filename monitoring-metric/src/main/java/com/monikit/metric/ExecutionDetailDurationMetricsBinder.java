package com.monikit.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;



/**
 * 메서드 실행 시간을 기록하는 `MeterBinder`
 */

public class ExecutionDetailDurationMetricsBinder implements MeterBinder {

    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();
    private MeterRegistry meterRegistry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    /**
     * 동적으로 Timer를 기록하는 메서드
     */
    public void record(String className, String methodName, long durationMs, String tag) {
        String key = className + "." + methodName + "." + tag;

        Timer timer = timerCache.computeIfAbsent(key, k -> Timer.builder("execution_duration")
            .description("Method execution time in ms")
            .tag("class", className)
            .tag("method", methodName)
            .tag("tag", tag)
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry));

        timer.record(durationMs, TimeUnit.MILLISECONDS);
    }

}

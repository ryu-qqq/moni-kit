package com.monikit.starter.batch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * 배치 잡 실행 시간을 수집하는 {@link MeterBinder} 구현체.
 * <p>
 * 메트릭 이름: <code>batch_job_duration</code>
 * </p>
 * <p>
 * 각 잡 이름(jobName)별로 타이머를 등록하고, 실행 시간을 ms 단위로 기록합니다.
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */

public class BatchJobDurationMetricsBinder implements MeterBinder {

    private MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Timer> durationTimerCache = new ConcurrentHashMap<>();

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    public void record(String jobName, long durationMs) {

        Timer timer = durationTimerCache.computeIfAbsent(jobName, key -> Timer.builder("batch_job_duration")
            .description("Batch job execution duration in ms")
            .tag("job", jobName)
            .register(meterRegistry));

        timer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}

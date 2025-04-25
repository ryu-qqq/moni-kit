package com.monikit.starter.batch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * 배치 Step의 실행 시간을 수집하는 MeterBinder.
 * <p>
 * 메트릭 이름: <code>step_duration</code>
 * job/step 태그로 구분된 타이머를 통해 실행 시간(ms)을 기록합니다.
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */
public class StepDurationMetricsBinder implements MeterBinder {

    private MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    public void record(String job, String step, long durationMs) {
        String key = job + "|" + step;
        Timer timer = timerCache.computeIfAbsent(key, k -> Timer.builder("step_duration")
            .description("Step execution duration in ms")
            .tag("job", job)
            .tag("step", step)
            .register(meterRegistry));

        timer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}

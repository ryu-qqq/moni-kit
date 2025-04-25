package com.monikit.starter.batch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * 배치 Step의 read/write/skip 카운트를 수집하는 MeterBinder.
 * <p>
 * - step_read_count
 * - step_write_count
 * - step_skip_count
 * 를 job/step 이름별로 태깅하여 측정합니다.
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */
public class StepCountMetricsBinder implements MeterBinder {

    private MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> readCounterCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> writeCounterCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> skipCounterCache = new ConcurrentHashMap<>();

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    public void record(String job, String step, long read, long write, long skip, String status, String exitCode) {
        if (read > 0) {
            String key = job + "|" + step + "|read";
            Counter counter = readCounterCache.computeIfAbsent(key, k -> Counter.builder("step_read_count")
                .description("Step read count")
                .tag("job", job)
                .tag("step", step)
                .register(meterRegistry));
            counter.increment(read);
        }

        if (write > 0) {
            String key = job + "|" + step + "|write";
            Counter counter = writeCounterCache.computeIfAbsent(key, k -> Counter.builder("step_write_count")
                .description("Step write count")
                .tag("job", job)
                .tag("step", step)
                .register(meterRegistry));
            counter.increment(write);
        }

        if (skip > 0) {
            String key = job + "|" + step + "|skip";
            Counter counter = skipCounterCache.computeIfAbsent(key, k -> Counter.builder("step_skip_count")
                .description("Step skip count")
                .tag("job", job)
                .tag("step", step)
                .register(meterRegistry));
            counter.increment(skip);
        }
    }
}

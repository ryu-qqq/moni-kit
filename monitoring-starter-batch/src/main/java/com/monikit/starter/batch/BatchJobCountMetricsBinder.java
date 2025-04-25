package com.monikit.starter.batch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;


/**
 * 배치 잡 실행 횟수를 수집하는 {@link MeterBinder} 구현체.
 * <p>
 * - 총 실행 횟수(metric: <code>batch_job_total</code>)
 * - 성공 횟수(metric: <code>batch_job_success</code>)
 * - 실패 횟수(metric: <code>batch_job_failure</code>)
 * 를 구분하여 카운팅합니다.
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */
public class BatchJobCountMetricsBinder implements MeterBinder {

    private MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> successCounterCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> failureCounterCache = new ConcurrentHashMap<>();

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    public void record(String jobName, boolean success) {
        String status = success ? "success" : "failure";

        Counter.builder("batch_job_total")
            .description("Total number of batch job executions")
            .tag("job", jobName)
            .tag("status", status)
            .register(meterRegistry)
            .increment();

        Counter counter = (success ? successCounterCache : failureCounterCache)
            .computeIfAbsent(jobName, key -> Counter.builder("batch_job_" + status)
                .description("Batch job " + status + " count")
                .tag("job", jobName)
                .register(meterRegistry));
        counter.increment();
    }

}

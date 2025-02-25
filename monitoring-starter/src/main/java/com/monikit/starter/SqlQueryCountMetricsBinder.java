package com.monikit.starter;

import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * SQL 실행 횟수를 추적하는 `MeterBinder`
 */

public class SqlQueryCountMetricsBinder implements MeterBinder {

    private Counter sqlQueryCounter;
    private final AtomicInteger queryCount = new AtomicInteger(0);

    @Override
    public void bindTo(MeterRegistry registry) {
        this.sqlQueryCounter = Counter.builder("sql_query_total")
            .description("Total number of executed SQL queries")
            .register(registry);
    }

    /**
     * SQL 실행 횟수를 증가시키는 메서드
     */
    public void increment() {
        if (sqlQueryCounter != null) {
            sqlQueryCounter.increment();
            queryCount.incrementAndGet();
        }
    }

    /**
     * 현재 SQL 실행 횟수 반환 (테스트 용도)
     */
    public int getQueryCount() {
        return queryCount.get();
    }

}
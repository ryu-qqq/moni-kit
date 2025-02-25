package com.monikit.starter;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * SQL 실행 시간을 추적하는 `MeterBinder`
 */

public class SqlQueryDurationMetricsBinder implements MeterBinder {

    private Timer sqlQueryTimer;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.sqlQueryTimer = Timer.builder("sql_query_duration")
            .description("SQL query execution time")
            .register(registry);
    }

    /**
     * SQL 실행 시간을 기록하는 메서드
     */
    public void record(long executionTime) {
        if (sqlQueryTimer != null) {
            sqlQueryTimer.record(executionTime, TimeUnit.MILLISECONDS);
        }
    }
}

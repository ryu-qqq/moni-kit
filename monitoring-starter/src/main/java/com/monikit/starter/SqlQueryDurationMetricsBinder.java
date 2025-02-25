package com.monikit.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * **SQL 실행 시간을 추적하는 `MeterBinder`**
 * <p>
 * - **쿼리별(`sql`), 데이터소스별(`dataSource`) 실행 시간을 기록**
 * - **메트릭: `sql_query_duration`**
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */

public class SqlQueryDurationMetricsBinder implements MeterBinder {

    private final ConcurrentMap<String, Timer> timerMap = new ConcurrentHashMap<>();
    private MeterRegistry meterRegistry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    /**
     * **SQL 실행 시간을 기록하는 메서드**
     *
     * @param sql           실행된 SQL 쿼리 (정규화된 형태)
     * @param dataSource    데이터 소스 (DB 연결 정보)
     * @param executionTime 실행 시간 (ms)
     */
    public void record(String sql, String dataSource, long executionTime) {
        if (meterRegistry == null) return;

        String key = sql + "|" + dataSource;

        Timer timer = timerMap.computeIfAbsent(key, k ->
            Timer.builder("sql_query_duration")
                .description("SQL query execution time")
                .tag("query", sql)
                .tag("dataSource", dataSource)
                .register(meterRegistry)
        );

        timer.record(executionTime, TimeUnit.MILLISECONDS);
    }
}

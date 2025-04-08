package com.monikit.metric;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * **SQL 실행 횟수를 추적하는 `MeterBinder`**
 * <p>
 * - **쿼리별(`sql`), 데이터소스별(`dataSource`) 실행 횟수를 기록**
 * - **메트릭: `sql_query_total`**
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */

public class SqlQueryCountMetricsBinder implements MeterBinder {

    private final ConcurrentMap<String, Counter> counterMap = new ConcurrentHashMap<>();
    private MeterRegistry meterRegistry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    /**
     * **쿼리 실행 횟수를 증가시키는 메서드**
     *
     * @param sql        실행된 SQL 쿼리 (정규화된 형태)
     * @param dataSource 데이터 소스 (DB 연결 정보)
     */
    public void increment(String sql, String dataSource) {
        if (meterRegistry == null) return;

        String key = sql + "|" + dataSource;

        counterMap.computeIfAbsent(key, k ->
            Counter.builder("sql_query_total")
                .description("Total number of executed SQL queries")
                .tag("query", sql)
                .tag("dataSource", dataSource)
                .register(meterRegistry)
        ).increment();
    }
}

package com.monikit.starter;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.monikit.core.DatabaseQueryLog;
import com.monikit.starter.config.MoniKitMetricsProperties;
import com.monikit.starter.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class DatabaseQueryMetricCollectorTest {

    private MeterRegistry meterRegistry;
    private DatabaseQueryMetricCollector metricCollector;
    private MoniKitMetricsProperties metricsProperties;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsProperties = new MoniKitMetricsProperties();
        metricsProperties.setMetricsEnabled(true);
        metricsProperties.setQueryMetricsEnabled(true);
        metricsProperties.setSlowQueryThresholdMs(2000);
        metricsProperties.setQuerySamplingRate(10);

        metricCollector = new DatabaseQueryMetricCollector(
            Counter.builder("sql_query_total").register(meterRegistry),
            Timer.builder("sql_query_duration").register(meterRegistry),
            metricsProperties
        );

    }

    @Nested
    @DisplayName("record() 메서드 테스트")
    class RecordQueryMetricsTest {

        @Test
        @DisplayName("SQL 쿼리 메트릭을 정상적으로 기록해야 한다.")
        void shouldRecordQueryMetricsCorrectly() {
            // Given
            DatabaseQueryLog databaseQueryLog = TestLogEntryProvider.databaseQueryLog();

            // When
            metricCollector.record(databaseQueryLog);

            // Then - Counter 검증
            Counter counter = meterRegistry.find("sql_query_total").counter();
            assertNotNull(counter);
            assertEquals(1, counter.count());

            // Then - Timer 검증
            Timer timer = meterRegistry.find("sql_query_duration").timer();
            assertNotNull(timer);
            assertEquals(1, timer.count());
        }

        @Test
        @DisplayName("슬로우 쿼리를 감지해야 한다.")
        void shouldDetectSlowQuery() {
            // Given
            DatabaseQueryLog slowQueryLog = TestLogEntryProvider.slowDatabaseQueryLog();

            // When
            metricCollector.record(slowQueryLog);

            // Then - Timer 검증 (실행 시간 기록됨)
            Timer timer = meterRegistry.find("sql_query_duration").timer();
            assertNotNull(timer);
            assertEquals(1, timer.count());
            assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 2500);
        }

    }
}
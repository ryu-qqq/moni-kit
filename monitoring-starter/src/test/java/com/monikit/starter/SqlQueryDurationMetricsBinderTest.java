package com.monikit.starter;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import java.util.concurrent.TimeUnit;

class SqlQueryDurationMetricsBinderTest {

    private MeterRegistry meterRegistry;
    private SqlQueryDurationMetricsBinder metricsBinder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry(); // 실제 MeterRegistry 사용
        metricsBinder = new SqlQueryDurationMetricsBinder();
        metricsBinder.bindTo(meterRegistry); // 반드시 바인딩 실행
    }

    @Test
    @DisplayName("shouldBindToMeterRegistry")
    void shouldBindToMeterRegistry() {
        // When
        metricsBinder.bindTo(meterRegistry);

        // Then
        assertNotNull(metricsBinder);
    }

    @Nested
    @DisplayName("record() 메서드 테스트")
    class RecordMethodTests {

        @Test
        @DisplayName("shouldRecordSqlQueryExecutionTime")
        void shouldRecordSqlQueryExecutionTime() {
            // Given
            long executionTime = 200L;

            // When
            metricsBinder.record(executionTime);

            // Then
            Timer timer = meterRegistry.find("sql_query_duration").timer();

            assertNotNull(timer, "Timer should be created");
            assertEquals(1, timer.count(), "Timer should record one event");
            assertEquals(executionTime, timer.totalTime(TimeUnit.MILLISECONDS),
                "Total time should match recorded execution time");
        }
    }
}
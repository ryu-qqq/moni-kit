package com.monikit.starter;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SqlQueryCountMetricsBinderTest {

    private MeterRegistry meterRegistry;
    private SqlQueryCountMetricsBinder metricsBinder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry(); // 실제 MeterRegistry 사용
        metricsBinder = new SqlQueryCountMetricsBinder();
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
    @DisplayName("increment() 메서드 테스트")
    class IncrementMethodTests {

        @Test
        @DisplayName("shouldIncrementSqlQueryCounter")
        void shouldIncrementSqlQueryCounter() {
            // Given
            int initialCount = metricsBinder.getQueryCount();

            // When
            metricsBinder.increment();

            // Then
            Counter counter = meterRegistry.find("sql_query_total").counter();

            assertNotNull(counter, "Counter should be created");
            assertEquals(initialCount + 1, metricsBinder.getQueryCount(), "Query count should be incremented");
            assertEquals(1.0, counter.count(), "Counter should be incremented once");
        }
    }
}

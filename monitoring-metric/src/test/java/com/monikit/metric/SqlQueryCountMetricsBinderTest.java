package com.monikit.metric;

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
            String sql = "SELECT * FROM users WHERE id = ?";
            String dataSource = "primary_db";

            // When
            metricsBinder.increment(sql, dataSource);

            // Then
            Counter counter = meterRegistry.find("sql_query_total")
                .tags("query", sql, "dataSource", dataSource)
                .counter();

            assertNotNull(counter, "Counter should be created");
            assertEquals(1.0, counter.count(), "Counter should be incremented once");
        }

        @Test
        @DisplayName("shouldHandleMultipleQueriesAndDataSources")
        void shouldHandleMultipleQueriesAndDataSources() {
            // Given
            String sql1 = "SELECT * FROM users WHERE id = ?";
            String dataSource1 = "primary_db";
            String sql2 = "UPDATE orders SET status = ? WHERE id = ?";
            String dataSource2 = "secondary_db";

            // When
            metricsBinder.increment(sql1, dataSource1);
            metricsBinder.increment(sql2, dataSource2);

            // Then
            Counter counter1 = meterRegistry.find("sql_query_total")
                .tags("query", sql1, "dataSource", dataSource1)
                .counter();
            Counter counter2 = meterRegistry.find("sql_query_total")
                .tags("query", sql2, "dataSource", dataSource2)
                .counter();

            assertNotNull(counter1, "Counter for sql1 should be created");
            assertNotNull(counter2, "Counter for sql2 should be created");

            assertEquals(1.0, counter1.count(), "Counter for sql1 should be incremented once");
            assertEquals(1.0, counter2.count(), "Counter for sql2 should be incremented once");
        }
    }
}

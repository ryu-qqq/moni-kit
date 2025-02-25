package com.monikit.starter;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

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
            String sql = "SELECT * FROM users WHERE id = ?";
            String dataSource = "primary_db";
            long executionTime = 200L;

            // When
            metricsBinder.record(sql, dataSource, executionTime);

            // Then
            Timer timer = meterRegistry.find("sql_query_duration")
                .tags("query", sql, "dataSource", dataSource)
                .timer();

            assertNotNull(timer, "Timer should be created");
            assertEquals(1, timer.count(), "Timer should record one event");
            assertEquals(executionTime, timer.totalTime(TimeUnit.MILLISECONDS),
                "Total time should match recorded execution time");
        }

        @Test
        @DisplayName("shouldHandleMultipleQueriesAndDataSources")
        void shouldHandleMultipleQueriesAndDataSources() {
            // Given
            String sql1 = "SELECT * FROM users WHERE id = ?";
            String dataSource1 = "primary_db";
            String sql2 = "UPDATE orders SET status = ? WHERE id = ?";
            String dataSource2 = "secondary_db";
            long executionTime1 = 150L;
            long executionTime2 = 250L;

            // When
            metricsBinder.record(sql1, dataSource1, executionTime1);
            metricsBinder.record(sql2, dataSource2, executionTime2);

            // Then
            Timer timer1 = meterRegistry.find("sql_query_duration")
                .tags("query", sql1, "dataSource", dataSource1)
                .timer();
            Timer timer2 = meterRegistry.find("sql_query_duration")
                .tags("query", sql2, "dataSource", dataSource2)
                .timer();

            assertNotNull(timer1, "Timer for sql1 should be created");
            assertNotNull(timer2, "Timer for sql2 should be created");

            assertEquals(1, timer1.count(), "Timer for sql1 should record one event");
            assertEquals(executionTime1, timer1.totalTime(TimeUnit.MILLISECONDS),
                "Total time for sql1 should match recorded execution time");

            assertEquals(1, timer2.count(), "Timer for sql2 should record one event");
            assertEquals(executionTime2, timer2.totalTime(TimeUnit.MILLISECONDS),
                "Total time for sql2 should match recorded execution time");
        }
    }
}

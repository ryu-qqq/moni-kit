package com.monikit.starter;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@DisplayName("MoniKitMetricCollector 단위 테스트")
class MoniKitMetricCollectorTest {

    private MeterRegistry meterRegistry;
    private MoniKitMetricCollector metricCollector;


    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricCollector = new MoniKitMetricCollector(meterRegistry);
    }

    @Nested
    @DisplayName("recordHttpRequest() 테스트")
    class RecordHttpRequestTest {

        @Test
        @DisplayName("HTTP 요청 메트릭을 정상적으로 기록해야 한다.")
        void shouldRecordHttpRequestMetricsCorrectly() {
            // Given
            String method = "GET";
            String uri = "/api/products";
            int statusCode = 200;
            long duration = 150L;

            // When
            metricCollector.recordHttpRequest(method, uri, statusCode, duration);

            // Then
            Counter counter = meterRegistry.find("http_requests_total")
                .tags("method", method, "uri", uri, "status", String.valueOf(statusCode))
                .counter();
            assertNotNull(counter);
            assertEquals(1, counter.count());

            Timer timer = meterRegistry.find("http_request_duration")
                .tags("method", method, "uri", uri)
                .timer();
            assertNotNull(timer);
            assertEquals(1, timer.count());
        }
    }

    @Nested
    @DisplayName("recordQueryMetrics() 테스트")
    class RecordQueryMetricsTest {

        @Test
        @DisplayName("SQL 쿼리 메트릭을 정상적으로 기록해야 한다.")
        void shouldRecordQueryMetricsCorrectly() {
            // Given
            String sql = "SELECT * FROM users";
            long executionTime = 300L;
            String dataSourceName = "main-db";

            String queryCategory = QueryMetricUtils.categorizeQuery(sql);

            // When
            metricCollector.recordQueryMetrics(sql, executionTime, dataSourceName);

            // Then
            Counter counter = meterRegistry.find("sql_query_total")
                .tags("query_category", queryCategory, "datasource", dataSourceName)
                .counter();

            assertNotNull(counter, "Counter should not be null. Check the actual queryCategory value.");
            assertEquals(1, counter.count());

            Timer timer = meterRegistry.find("sql_query_duration")
                .tags("query_category", queryCategory, "datasource", dataSourceName)
                .timer();

            assertNotNull(timer, "Timer should not be null. Check the actual queryCategory value.");
            assertEquals(1, timer.count());
        }

    }
}

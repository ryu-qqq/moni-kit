package com.monikit.metric;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class HttpResponseCountMetricsBinderTest  {

    private MeterRegistry meterRegistry;
    private HttpResponseCountMetricsBinder metricsBinder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsBinder = new HttpResponseCountMetricsBinder();
        metricsBinder.bindTo(meterRegistry);
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
        @DisplayName("shouldIncrementCounterForGivenPathAndStatusCode")
        void shouldIncrementCounterForGivenPathAndStatusCode() {
            // Given
            String path = "/api/test";
            int statusCode = 200;

            // When
            metricsBinder.increment(path, statusCode);

            // Then
            Counter counter = meterRegistry.find("http_response_count")
                .tag("path", path)
                .tag("status", String.valueOf(statusCode))
                .counter();

            assertNotNull(counter, "Counter should be created");
            assertEquals(1.0, counter.count(), "Counter should be incremented once");
        }
    }
}

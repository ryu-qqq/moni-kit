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

class HttpResponseDurationMetricsBinderTest  {

    private MeterRegistry meterRegistry;
    private HttpResponseDurationMetricsBinder metricsBinder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsBinder = new HttpResponseDurationMetricsBinder();
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
    @DisplayName("record() 메서드 테스트")
    class RecordMethodTests {

        @Test
        @DisplayName("shouldRecordResponseTimeForGivenPathAndStatusCode")
        void shouldRecordResponseTimeForGivenPathAndStatusCode() {
            // Given
            String path = "/api/test";
            int statusCode = 200;
            long responseTime = 150L;

            // When
            metricsBinder.record(path, statusCode, responseTime);

            // Then
            Timer timer = meterRegistry.find("http_response_duration")
                .tag("path", path)
                .tag("status", String.valueOf(statusCode))
                .timer();

            assertNotNull(timer, "Timer should be created");
            assertEquals(1, timer.count(), "Timer should record one event");
            assertEquals(responseTime, timer.totalTime(TimeUnit.MILLISECONDS), "Total time should match recorded response time");
        }
    }
}

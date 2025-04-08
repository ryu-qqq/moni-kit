package com.monikit.metric;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


class HttpResponseMetricsRecorderTest {

    @Mock
    private HttpResponseCountMetricsBinder countMetricsBinder;

    @Mock
    private HttpResponseDurationMetricsBinder durationMetricsBinder;

    private HttpResponseMetricsRecorder recorder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        recorder = new HttpResponseMetricsRecorder(countMetricsBinder, durationMetricsBinder);
    }

    @Nested
    @DisplayName("record() 메서드 테스트")
    class RecordMethodTests {

        @Test
        @DisplayName("shouldRecordHttpResponseMetrics")
        void shouldRecordHttpResponseMetrics() {
            // Given
            String path = "/api/test";
            int statusCode = 200;
            long responseTime = 150L;

            // When
            recorder.record(path, statusCode, responseTime);

            // Then
            verify(countMetricsBinder, times(1)).increment(path, statusCode);
            verify(durationMetricsBinder, times(1)).record(path, statusCode, responseTime);
        }
    }
}

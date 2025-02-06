package com.monikit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MetricCollectorProviderTest {

    // private MetricCollector mockMetricCollector;
    //
    // @BeforeEach
    // void setUp() {
    //     mockMetricCollector = mock(MetricCollector.class);
    //     MetricCollectorProvider.setMetricCollector(null);
    // }
    //
    // @Test
    // @DisplayName("MetricCollector가 설정되면 올바르게 반환된다")
    // void shouldInitializeAndReturnMetricCollectorWhenSet() {
    //     MetricCollectorProvider.setMetricCollector(mockMetricCollector);
    //
    //     MetricCollector metricCollector = MetricCollectorProvider.getMetricCollector();
    //
    //     assertTrue(mockMetricCollector.getClass().isInstance(metricCollector), "MetricCollector should be the one that was set");
    // }
    //
    // @Test
    // @DisplayName("한 번 설정된 MetricCollector는 덮어쓰지 않는다")
    // void shouldNotOverwriteMetricCollectorOnceSet() {
    //     MetricCollectorProvider.setMetricCollector(mockMetricCollector);
    //
    //     MetricCollector newMockMetricCollector = mock(MetricCollector.class);
    //     MetricCollectorProvider.setMetricCollector(newMockMetricCollector);
    //
    //     MetricCollector metricCollector = MetricCollectorProvider.getMetricCollector();
    //
    //     assertTrue(mockMetricCollector.getClass().isInstance(metricCollector), "MetricCollector should be the one that was set");
    // }

}
package com.monikit.starter;

import com.monikit.core.MetricCollector;

/**
 * 메트릭 수집이 비활성화된 경우 사용하는 `MetricCollector` 구현체.
 * <p>
 * - `monikit.metrics.enabled=false` 설정 시, 이 빈이 사용됨.
 * - 메트릭을 기록하지 않고, 아무 동작도 하지 않음.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class NoOpMetricCollector implements MetricCollector {

    @Override
    public void recordHttpRequest(String method, String uri, int statusCode, long duration) {
        // Do nothing (메트릭 비활성화 상태)
    }

    @Override
    public void recordQueryMetrics(String sql, long executionTime, String dataSourceName) {
        // Do nothing (메트릭 비활성화 상태)
    }
}
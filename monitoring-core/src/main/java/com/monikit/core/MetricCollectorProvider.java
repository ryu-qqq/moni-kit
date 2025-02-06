package com.monikit.core;

/**
 * MetricCollector의 싱글톤 인스턴스를 관리하는 클래스.
 * <p>
 * 이 클래스는 MetricCollector의 전역 접근을 관리하며,
 * 외부에서 초기화 후에 어디서나 접근할 수 있도록 합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class MetricCollectorProvider {

    private static MetricCollector instance;

    /**
     * MetricCollector 인스턴스를 설정합니다.
     *
     * @param metricCollector MetricCollector 인스턴스
     */
    public static void setMetricCollector(MetricCollector metricCollector) {
        if (instance == null) {
            instance = metricCollector;
        }
    }

    /**
     * MetricCollector 인스턴스를 반환합니다.
     *
     * @return MetricCollector 인스턴스
     * @throws IllegalStateException MetricCollector가 초기화되지 않은 경우
     */
    public static MetricCollector getMetricCollector() {
        if (instance == null) {
            throw new IllegalStateException("MetricCollector is not initialized");
        }
        return instance;
    }

}
package com.monikit.core;

/**
 * 모든 메트릭 수집기를 위한 공통 인터페이스.
 * <p>
 * - HTTP 요청 메트릭과 SQL 쿼리 메트릭을 추상화하여 다양한 메트릭 시스템과 연동할 수 있도록 설계.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public interface MetricCollector {

    /**
     * HTTP 요청 메트릭 수집.
     *
     * @param method HTTP 메서드 (GET, POST 등)
     * @param uri 요청 URI
     * @param statusCode 응답 상태 코드 (200, 500 등)
     * @param duration 실행 시간 (ms)
     */
    void recordHttpRequest(String method, String uri, int statusCode, long duration);


    /**
     * SQL 쿼리 실행 메트릭을 수집.
     *
     * @param sql 실행된 SQL 쿼리
     * @param executionTime 실행 시간 (ms)
     * @param dataSourceName 데이터 소스 이름
     */
    void recordQueryMetrics(String sql, long executionTime, String dataSourceName);

}

package com.monikit.starter;

public class QueryMetricsRecorder {

    private final SqlQueryCountMetricsBinder countMetricsBinder;
    private final SqlQueryDurationMetricsBinder durationMetricsBinder;

    public QueryMetricsRecorder(SqlQueryCountMetricsBinder countMetricsBinder,
                                SqlQueryDurationMetricsBinder durationMetricsBinder) {
        this.countMetricsBinder = countMetricsBinder;
        this.durationMetricsBinder = durationMetricsBinder;
    }

    /**
     * **쿼리 실행 메트릭을 기록**
     *
     * @param sql           실행된 SQL 쿼리 (정규화된 형태)
     * @param dataSource    데이터 소스 (DB 연결 정보)
     * @param executionTime 실행 시간 (ms)
     */
    public void record(String sql, String dataSource, long executionTime) {
        countMetricsBinder.increment(sql, dataSource);
        durationMetricsBinder.record(sql, dataSource, executionTime);
    }

}

package com.monikit.metric;

/**
 * 메서드 실행 시간 및 호출 횟수를 기록하는 Recorder 클래스.
 * <p>
 * - execution_count: 호출 횟수
 * - execution_duration: 실행 시간(ms)
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */

public class ExecutionMetricRecorder {

    private final ExecutionDetailCountMetricsBinder executionDetailCountMetricsBinder;
    private final ExecutionDetailDurationMetricsBinder executionDetailDurationMetricsBinder;

    public ExecutionMetricRecorder(ExecutionDetailCountMetricsBinder executionDetailCountMetricsBinder,
                                   ExecutionDetailDurationMetricsBinder executionDetailDurationMetricsBinder) {
        this.executionDetailCountMetricsBinder = executionDetailCountMetricsBinder;
        this.executionDetailDurationMetricsBinder = executionDetailDurationMetricsBinder;
    }

    public void record(String className, String methodName, long durationMs, String tag) {
        executionDetailCountMetricsBinder.record(className, methodName, tag);
        executionDetailDurationMetricsBinder.record(className, methodName, durationMs, tag);
    }

}

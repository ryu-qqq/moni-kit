package com.monikit.starter.batch;

/**
 * 배치 잡 실행 관련 메트릭을 수집하는 Recorder.
 * <p>
 * 내부적으로 두 개의 바인더를 사용하여 각 메트릭을 기록한다:
 * <ul>
 *     <li>{@link BatchJobCountMetricsBinder} - 잡 실행 횟수 및 성공/실패 카운트</li>
 *     <li>{@link BatchJobDurationMetricsBinder} - 잡 실행 시간(duration) 기록</li>
 * </ul>
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */

public class BatchJobMetricsRecorder {

    private final BatchJobCountMetricsBinder batchJobCountMetricsBinder;
    private final BatchJobDurationMetricsBinder batchJobDurationMetricsBinder;

    public BatchJobMetricsRecorder(BatchJobCountMetricsBinder batchJobCountMetricsBinder,
                                   BatchJobDurationMetricsBinder batchJobDurationMetricsBinder) {
        this.batchJobCountMetricsBinder = batchJobCountMetricsBinder;
        this.batchJobDurationMetricsBinder = batchJobDurationMetricsBinder;
    }

    public void record(String jobName, boolean success, long duration){
        batchJobCountMetricsBinder.record(jobName, success);
        batchJobDurationMetricsBinder.record(jobName, duration);
    }

}

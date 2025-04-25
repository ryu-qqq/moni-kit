package com.monikit.starter.batch;

/**
 * 배치 Step 실행 메트릭을 수집하는 통합 Recorder 클래스.
 * <p>
 * 내부적으로 Step 단위 Count 및 Duration 바인더를 활용하여
 * 처리량과 실행 시간 관련 메트릭을 기록합니다.
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */
public class BatchStepMetricsRecorder {

    private final StepCountMetricsBinder stepCountMetricsBinder;
    private final StepDurationMetricsBinder stepDurationMetricsBinder;

    public BatchStepMetricsRecorder(StepCountMetricsBinder stepCountMetricsBinder,
                                    StepDurationMetricsBinder stepDurationMetricsBinder) {
        this.stepCountMetricsBinder = stepCountMetricsBinder;
        this.stepDurationMetricsBinder = stepDurationMetricsBinder;
    }

    public void record(String job, String step, long durationMs,
                       long readCount, long writeCount, long skipCount,
                       String status, String exitCode) {

        stepCountMetricsBinder.record(job, step, readCount, writeCount, skipCount, status, exitCode);
        stepDurationMetricsBinder.record(job, step, durationMs);
    }

}

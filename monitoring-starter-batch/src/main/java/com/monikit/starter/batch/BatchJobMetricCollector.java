package com.monikit.starter.batch;

import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.LogType;
import com.monikit.core.hook.MetricCollector;
import com.monikit.core.model.BatchJobLog;




/**
 * 배치 잡 실행 메트릭을 수집하는 Recorder 클래스
 * <p>
 * - 잡 실행 횟수 (`batch_job_total`)
 * - 성공/실패 카운터 (`batch_job_success`, `batch_job_failure`)
 * - 실행 시간 (`batch_job_duration`)
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */

public class BatchJobMetricCollector implements MetricCollector<BatchJobLog> {

    private final MoniKitMetricsProperties metricsProperties;
    private final BatchJobMetricsRecorder batchJobMetricsRecorder;

    public BatchJobMetricCollector(MoniKitMetricsProperties metricsProperties,
                                   BatchJobMetricsRecorder batchJobMetricsRecorder) {
        this.metricsProperties = metricsProperties;
        this.batchJobMetricsRecorder = batchJobMetricsRecorder;
    }

    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.BATCH_JOB;
    }

    @Override
    public void record(BatchJobLog logEntry) {
        if (!metricsProperties.isMetricsEnabled()) {
            return;
        }

        String jobName = logEntry.getJobName();
        boolean success = "COMPLETED".equalsIgnoreCase(logEntry.getStatus());
        long duration = logEntry.getExecutionTime();

        batchJobMetricsRecorder.record(jobName, success, duration);
    }


}
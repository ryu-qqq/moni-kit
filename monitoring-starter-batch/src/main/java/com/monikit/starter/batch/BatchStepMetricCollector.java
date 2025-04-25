package com.monikit.starter.batch;

import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.LogType;
import com.monikit.core.hook.MetricCollector;
import com.monikit.core.model.BatchStepLog;

/**
 * 배치 Step 실행 로그를 수집하여 메트릭으로 전환하는 Collector.
 *
 * <p>
 * - 조건: metrics.enabled && job-metrics.enabled
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */
public class BatchStepMetricCollector implements MetricCollector<BatchStepLog> {

    private final MoniKitMetricsProperties metricsProperties;
    private final BatchStepMetricsRecorder batchStepMetricsRecorder;

    public BatchStepMetricCollector(MoniKitMetricsProperties metricsProperties,
                                    BatchStepMetricsRecorder batchStepMetricsRecorder) {
        this.metricsProperties = metricsProperties;
        this.batchStepMetricsRecorder = batchStepMetricsRecorder;
    }

    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.BATCH_STEP;
    }

    @Override
    public void record(BatchStepLog logEntry) {
        if (!metricsProperties.isMetricsEnabled()) {
            return;
        }

        String job = logEntry.getJobName();
        String step = logEntry.getStepName();
        long duration = logEntry.getEndTime().toEpochMilli() - logEntry.getStartTime().toEpochMilli();

        batchStepMetricsRecorder.record(
            job,
            step,
            duration,
            logEntry.getReadCount(),
            logEntry.getWriteCount(),
            logEntry.getSkipCount(),
            logEntry.getStatus(),
            logEntry.getExitCode()
        );
    }

}

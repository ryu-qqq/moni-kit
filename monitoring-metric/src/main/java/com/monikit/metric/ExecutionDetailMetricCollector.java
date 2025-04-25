package com.monikit.metric;

import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.LogType;
import com.monikit.core.hook.MetricCollector;
import com.monikit.core.model.ExecutionDetailLog;

/**
 * 메서드 실행 시간(ExecutionDetailLog)을 수집하는 MetricCollector 구현체.
 * <p>
 * - 메서드별 실행 횟수: execution_count
 * - 메서드별 실행 시간: execution_duration
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */

public class ExecutionDetailMetricCollector implements MetricCollector<ExecutionDetailLog> {

    private final MoniKitMetricsProperties metricsProperties;
    private final ExecutionMetricRecorder executionMetricRecorder;

    public ExecutionDetailMetricCollector(MoniKitMetricsProperties metricsProperties, ExecutionMetricRecorder executionMetricRecorder) {
        this.metricsProperties = metricsProperties;
        this.executionMetricRecorder = executionMetricRecorder;
    }

    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.EXECUTION_DETAIL;
    }

    @Override
    public void record(ExecutionDetailLog logEntry) {
        if (!metricsProperties.isMetricsEnabled()) {
            return;
        }

        executionMetricRecorder.record(
            logEntry.getClassName(),
            logEntry.getMethodName(),
            logEntry.getExecutionTime(),
            logEntry.getTag()
        );
    }

}
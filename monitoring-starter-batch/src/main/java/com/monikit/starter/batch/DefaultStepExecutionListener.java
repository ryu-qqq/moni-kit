package com.monikit.starter.batch;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.core.annotation.Order;

import com.monikit.core.BatchStepLog;
import com.monikit.core.LogEntry;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.LogNotifier;
import com.monikit.core.MetricCollector;
import com.monikit.core.TraceIdProvider;

@Order(0)
class DefaultStepExecutionListener implements StepExecutionListener {

    private final LogNotifier logNotifier;
    private final MetricCollector<LogEntry> metricCollector;
    private final TraceIdProvider traceIdProvider;
    private final LogEntryContextManager contextManager;

    public DefaultStepExecutionListener(
        @Nullable LogNotifier logNotifier,
        @Nullable MetricCollector<LogEntry> metricCollector,
        @Nullable TraceIdProvider traceIdProvider,
        @Nullable LogEntryContextManager contextManager
    ) {
        this.logNotifier = logNotifier;
        this.metricCollector = metricCollector;
        this.traceIdProvider = traceIdProvider;
        this.contextManager = contextManager;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Do nothing
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Instant start = stepExecution.getStartTime().toInstant(ZoneOffset.UTC);
        Instant end = stepExecution.getEndTime() != null ? stepExecution.getEndTime().toInstant(ZoneOffset.UTC) : Instant.now();

        BatchStepLog log = BatchStepLog.create(
            traceIdProvider.getTraceId(),
            stepExecution.getJobExecution().getJobInstance().getJobName(),
            stepExecution.getStepName(),
            start,
            end,
            stepExecution.getReadCount(),
            stepExecution.getWriteCount(),
            stepExecution.getSkipCount(),
            stepExecution.getExitStatus().getExitCode(),
            stepExecution.getStatus().name(),
            LogLevel.INFO
        );

        contextManager.addLog(log);
        safe(() -> logNotifier.notify(log));
        safe(() -> metricCollector.record(log));

        return stepExecution.getExitStatus();
    }

    private void safe(Runnable r) {
        try {
            if (r != null) r.run();
        } catch (Exception ignored) {}
    }
}


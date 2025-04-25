package com.monikit.starter.batch;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.core.annotation.Order;

import com.monikit.core.model.BatchStepLog;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;

import jakarta.annotation.Nullable;

@Order(0)
public class DefaultStepExecutionListener implements StepExecutionListener {

    private final TraceIdProvider traceIdProvider;
    private final LogEntryContextManager contextManager;

    public DefaultStepExecutionListener(
        @Nullable TraceIdProvider traceIdProvider,
        @Nullable LogEntryContextManager contextManager
    ) {
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
        Instant end = stepExecution.getEndTime() != null
            ? stepExecution.getEndTime().toInstant(ZoneOffset.UTC)
            : Instant.now();

        LogLevel level = resolveLogLevel(stepExecution.getExitStatus());

        BatchStepLog log = BatchStepLog.of(
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
            level
        );

        contextManager.addLog(log);
        return stepExecution.getExitStatus();
    }

    private LogLevel resolveLogLevel(ExitStatus exitStatus) {
        String code = exitStatus.getExitCode();
        if (code.contains("FAILURE") || code.contains("EXCEPTION") || code.contains("ERROR") || code.contains("TIMEOUT")) {
            return LogLevel.ERROR;
        }
        if (code.contains("WARNING") || code.contains("SKIP")) {
            return LogLevel.WARN;
        }
        return LogLevel.INFO;
    }
}

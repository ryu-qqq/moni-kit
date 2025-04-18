package com.monikit.starter.batch;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.annotation.Order;

import com.monikit.core.BatchJobLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;

import jakarta.annotation.Nullable;

@Order(0)
public class DefaultJobExecutionListener implements JobExecutionListener {

    private final TraceIdProvider traceIdProvider;
    private final LogEntryContextManager contextManager;

    public DefaultJobExecutionListener(
        @Nullable TraceIdProvider traceIdProvider,
        @Nullable LogEntryContextManager contextManager
    ) {
        this.traceIdProvider = traceIdProvider;
        this.contextManager = contextManager;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String traceId = resolveTraceId(jobExecution);
        traceIdProvider.setTraceId(traceId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Instant start = jobExecution.getStartTime().toInstant(ZoneOffset.UTC);
        Instant end = jobExecution.getEndTime() != null
            ? jobExecution.getEndTime().toInstant(ZoneOffset.UTC)
            : Instant.now();

        long executionTime = end.toEpochMilli() - start.toEpochMilli();
        LogLevel level = resolveLogLevel(jobExecution.getStatus(), jobExecution.getExitStatus());

        BatchJobLog log = BatchJobLog.create(
            traceIdProvider.getTraceId(),
            jobExecution.getJobInstance().getJobName(),
            start,
            end,
            executionTime,
            jobExecution.getStatus().name(),
            jobExecution.getExitStatus().getExitCode(),
            jobExecution.getExitStatus().getExitDescription(),
            level
        );

        contextManager.addLog(log);
        contextManager.flush();
        traceIdProvider.clear();
    }

    private String resolveTraceId(JobExecution jobExecution) {
        String param = jobExecution.getJobParameters().getString("traceId");
        return (param != null && !param.isBlank())
            ? param
            : UUID.randomUUID().toString();
    }

    private LogLevel resolveLogLevel(BatchStatus status, ExitStatus exitStatus) {
        if (status == BatchStatus.FAILED || status == BatchStatus.STOPPED) {
            return LogLevel.ERROR;
        }

        if (status == BatchStatus.UNKNOWN || status == BatchStatus.ABANDONED) {
            return LogLevel.WARN;
        }

        String exitCode = exitStatus.getExitCode();

        if (exitCode != null && (
            exitCode.contains("EXCEPTION") ||
                exitCode.contains("FAILURE") ||
                exitCode.contains("ERROR") ||
                exitCode.contains("TIMEOUT"))
        ) {
            return LogLevel.ERROR;
        }

        return LogLevel.INFO;
    }

}
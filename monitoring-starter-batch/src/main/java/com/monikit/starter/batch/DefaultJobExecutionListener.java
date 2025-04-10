package com.monikit.starter.batch;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import com.monikit.core.BatchJobLog;
import com.monikit.core.ErrorLogNotifier;
import com.monikit.core.ExceptionLog;
import com.monikit.core.LogEntry;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.LogNotifier;
import com.monikit.core.MetricCollector;
import com.monikit.core.TraceIdProvider;

@Order(0)
public class MonikitJobExecutionListener implements JobExecutionListener {

    private final LogNotifier logNotifier;
    private final MetricCollector<LogEntry> metricCollector;
    private final TraceIdProvider traceIdProvider;
    private final LogEntryContextManager contextManager;
    private final ErrorLogNotifier errorLogNotifier;

    public MonikitJobExecutionListener(
        @Nullable LogNotifier logNotifier,
        @Nullable MetricCollector<LogEntry> metricCollector,
        @Nullable TraceIdProvider traceIdProvider,
        @Nullable LogEntryContextManager contextManager,
        @Nullable ErrorLogNotifier errorLogNotifier
    ) {
        this.logNotifier = logNotifier;
        this.metricCollector = metricCollector;
        this.traceIdProvider = traceIdProvider;
        this.contextManager = contextManager;
        this.errorLogNotifier = errorLogNotifier;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String traceId = resolveTraceId(jobExecution);
        traceIdProvider.setTraceId(traceId);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Instant start = jobExecution.getStartTime().toInstant();
        Instant end = jobExecution.getEndTime() != null ? jobExecution.getEndTime().toInstant() : Instant.now();
        long executionTime = end.toEpochMilli() - start.toEpochMilli();

        BatchJobLog log = BatchJobLog.create(
            traceIdProvider.getTraceId(),
            jobExecution.getJobInstance().getJobName(),
            start,
            end,
            executionTime,
            jobExecution.getStatus().name(),
            jobExecution.getExitStatus().getExitCode(),
            jobExecution.getExitStatus().getExitDescription(),
            LogLevel.INFO
        );

        contextManager.addLog(log);
        safe(() -> logNotifier.notify(log));
        safe(() -> metricCollector.record(log));

        if (jobExecution.getStatus().isUnsuccessful()) {
            safe(() -> errorLogNotifier.onErrorLogDetected(new ExceptionLog(log)));
        }

        contextManager.flush();
        traceIdProvider.clear();
    }

    private String resolveTraceId(JobExecution jobExecution) {
        String param = jobExecution.getJobParameters().getString("traceId");

        if(param != null && !param.isBlank()){
            return param;
        }else{
            return UUID.randomUUID().toString();
        }

    }

    private void safe(Runnable r) {
        try {
            if (r != null) r.run();
        } catch (Exception ignored) {}
    }

}

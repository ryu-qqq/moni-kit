package com.monikit.core;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * 배치 작업 실행 정보를 기록하는 로그 클래스.
 * <p>
 * 실행된 배치 작업 이름, 실행 시작 및 종료 시간, 실행 상태, 실행 시간을 기록하여
 * 배치 성능 및 오류 분석을 지원한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public class BatchJobLog extends AbstractLogEntry {
    private final String jobName;
    private final Instant startTime;
    private final Instant endTime;
    private final long executionTime;
    private final String status;
    private final String exitCode;
    private final String errorMessage;

    protected BatchJobLog(String traceId, String jobName, Instant startTime, Instant endTime,
                          long executionTime, String status, String exitCode, String errorMessage, LogLevel logLevel) {
        super(traceId, logLevel);
        this.jobName = jobName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.executionTime = executionTime;
        this.status = status;
        this.exitCode = exitCode;
        this.errorMessage = errorMessage;
    }

    public static BatchJobLog create(String traceId, String jobName, Instant startTime, Instant endTime,
                                     long executionTime, String status, String exitCode,
                                     String errorMessage, LogLevel logLevel) {
        return new BatchJobLog(traceId, jobName, startTime, endTime, executionTime, status, exitCode, errorMessage, logLevel);
    }

    public String getJobName() {
        return jobName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getStatus() {
        return status;
    }

    public String getExitCode() {
        return exitCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public LogType getLogType() {
        return LogType.BATCH_JOB;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("jobName", jobName);
        logMap.put("startTime", startTime.toString());
        logMap.put("endTime", endTime.toString());
        logMap.put("executionTime", executionTime + "ms");
        logMap.put("status", status);
        logMap.put("exitCode", exitCode);
        logMap.put("errorMessage", errorMessage);
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        BatchJobLog that = (BatchJobLog) object;
        return executionTime
            == that.executionTime
            && Objects.equals(jobName, that.jobName)
            && Objects.equals(startTime, that.startTime)
            && Objects.equals(endTime, that.endTime)
            && Objects.equals(status, that.status)
            && Objects.equals(exitCode, that.exitCode)
            && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, startTime, endTime, executionTime, status, exitCode, errorMessage);
    }
}

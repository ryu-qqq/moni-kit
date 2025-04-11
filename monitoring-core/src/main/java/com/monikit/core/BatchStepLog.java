package com.monikit.core;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;


/**
 * Step 단위 배치 로그 엔트리.
 * <p>
 * 배치 작업 내 개별 스텝의 실행 정보를 기록합니다.
 * 스텝 이름, 처리 건수, 스킵 건수, 실행 시간, 상태 등을 포함하여
 * 스텝 단위 성능 및 오류 분석을 지원합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class BatchStepLog extends AbstractLogEntry {

    private final String jobName;
    private final String stepName;
    private final Instant startTime;
    private final Instant endTime;
    private final long readCount;
    private final long writeCount;
    private final long skipCount;
    private final String exitCode;
    private final String status;

    protected BatchStepLog(String traceId, String jobName, String stepName, Instant startTime, Instant endTime,
                           long readCount, long writeCount, long skipCount, String exitCode, String status,
                           LogLevel logLevel) {
        super(traceId, logLevel);
        this.jobName = jobName;
        this.stepName = stepName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.readCount = readCount;
        this.writeCount = writeCount;
        this.skipCount = skipCount;
        this.exitCode = exitCode;
        this.status = status;
    }

    public static BatchStepLog create(String traceId, String jobName, String stepName, Instant startTime, Instant endTime,
                                      long readCount, long writeCount, long skipCount,
                                      String exitCode, String status, LogLevel logLevel) {
        return new BatchStepLog(traceId, jobName, stepName, startTime, endTime, readCount, writeCount, skipCount,
            exitCode, status, logLevel);
    }

    public String getJobName() {
        return jobName;
    }

    public String getStepName() {
        return stepName;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public long getReadCount() {
        return readCount;
    }

    public long getWriteCount() {
        return writeCount;
    }

    public long getSkipCount() {
        return skipCount;
    }

    public String getExitCode() {
        return exitCode;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public LogType getLogType() {
        return LogType.BATCH_STEP;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("jobName", jobName);
        logMap.put("stepName", stepName);
        logMap.put("startTime", startTime.toString());
        logMap.put("endTime", endTime.toString());
        logMap.put("readCount", readCount);
        logMap.put("writeCount", writeCount);
        logMap.put("skipCount", skipCount);
        logMap.put("exitCode", exitCode);
        logMap.put("status", status);
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        BatchStepLog that = (BatchStepLog) object;
        return readCount
            == that.readCount
            && writeCount
            == that.writeCount
            && skipCount
            == that.skipCount
            && Objects.equals(jobName, that.jobName)
            && Objects.equals(stepName, that.stepName)
            && Objects.equals(startTime, that.startTime)
            && Objects.equals(endTime, that.endTime)
            && Objects.equals(exitCode, that.exitCode)
            && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, stepName, startTime, endTime, readCount, writeCount, skipCount, exitCode, status);
    }
}
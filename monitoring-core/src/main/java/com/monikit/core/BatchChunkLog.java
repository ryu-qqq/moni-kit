package com.monikit.core;

import java.util.Map;
import java.util.Objects;


/**
 * Chunk 단위 배치 로그 엔트리.
 * <p>
 * 배치 Step 내 Chunk 단위 처리에 대한 실행 로그를 기록합니다.
 * 각 Chunk의 인덱스, 처리 건수, 실패 여부, 예외 메시지를 포함하여
 * 더 세밀한 단위의 성능 및 오류 추적이 가능합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class BatchChunkLog extends AbstractLogEntry {

    private final String jobName;
    private final String stepName;
    private final int chunkIndex;
    private final long readCount;
    private final long writeCount;
    private final boolean failed;
    private final String errorMessage;

    protected BatchChunkLog(String traceId, String jobName, String stepName, int chunkIndex,
                            long readCount, long writeCount, boolean failed, String errorMessage,
                            LogLevel logLevel) {
        super(traceId, logLevel);
        this.jobName = jobName;
        this.stepName = stepName;
        this.chunkIndex = chunkIndex;
        this.readCount = readCount;
        this.writeCount = writeCount;
        this.failed = failed;
        this.errorMessage = errorMessage;
    }

    public static BatchChunkLog create(String traceId, String jobName, String stepName, int chunkIndex,
                                       long readCount, long writeCount, boolean failed, String errorMessage,
                                       LogLevel logLevel) {
        return new BatchChunkLog(traceId, jobName, stepName, chunkIndex, readCount, writeCount, failed, errorMessage, logLevel);
    }

    @Override
    public LogType getLogType() {
        return LogType.BATCH_CHUNK;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("jobName", jobName);
        logMap.put("stepName", stepName);
        logMap.put("chunkIndex", chunkIndex);
        logMap.put("readCount", readCount);
        logMap.put("writeCount", writeCount);
        logMap.put("failed", failed);
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
        BatchChunkLog that = (BatchChunkLog) object;
        return chunkIndex
            == that.chunkIndex
            && readCount
            == that.readCount
            && writeCount
            == that.writeCount
            && failed
            == that.failed
            && Objects.equals(jobName, that.jobName)
            && Objects.equals(stepName, that.stepName)
            && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, stepName, chunkIndex, readCount, writeCount, failed, errorMessage);
    }
}


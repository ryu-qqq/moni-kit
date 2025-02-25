package com.monikit.core;

import java.time.Instant;
import java.util.Map;

/**
 * 모든 로그 엔트리가 상속해야 하는 추상 클래스.
 * <p>
 * 공통 필드 (timestamp, traceId, logLevel, threadName, threadId)를 관리하며,
 * JSON 변환 로직을 제공한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public abstract class AbstractLogEntry implements LogEntry {

    private final Instant timestamp;
    private final String traceId;
    private final LogLevel logLevel;
    private final String threadName;
    private final long threadId;

    /**
     * 하위 클래스에서만 호출 가능하도록 protected 설정.
     * 외부에서 직접 인스턴스를 생성하는 것을 방지하고,
     * static factory method를 사용하도록 유도함.
     *
     * @param traceId 트랜잭션 ID
     * @param logLevel 로그 레벨
     */
    protected AbstractLogEntry(String traceId, LogLevel logLevel) {
        this.timestamp = Instant.now();
        this.traceId = traceId;
        this.logLevel = logLevel;
        this.threadName = Thread.currentThread().getName();
        this.threadId = Thread.currentThread().threadId();
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * 하위 클래스에서 추가 필드를 JSON에 포함할 수 있도록 메서드 제공.
     *
     * @param logMap JSON 변환을 위한 Map
     */
    protected abstract void addExtraFields(Map<String, Object> logMap);

}

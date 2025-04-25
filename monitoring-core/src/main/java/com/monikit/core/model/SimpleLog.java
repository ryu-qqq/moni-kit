package com.monikit.core.model;

import java.util.Map;
import java.util.Objects;

import com.monikit.core.LogLevel;
import com.monikit.core.LogType;

/**
 * 가장 단순한 형태의 구조화 로그 엔트리.
 * <p>
 * 로그 레벨, Trace ID, 단일 메시지 필드만 포함하며,
 * 복잡한 context 없이 경량 로그를 전송할 때 사용된다.
 * </p>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>{@code
 *     SimpleLog log = SimpleLog.create("trace-123", LogLevel.INFO, "단순 로그 메시지");
 *     logNotifier.notify(log);
 * }</pre>
 *
 * <p>LogType은 항상 {@link LogType#SIMPLE}로 고정된다.</p>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
public class SimpleLog extends AbstractLogEntry {
    private final String message;

    public SimpleLog(String traceId, LogLevel logLevel, String message) {
        super(traceId, logLevel);
        this.message = message;
    }

    public static SimpleLog of(String traceId, LogLevel logLevel, String message) {
        return new SimpleLog(traceId, logLevel, message);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public LogType getLogType() {
        return LogType.SIMPLE;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("message", message);
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        SimpleLog simpleLog = (SimpleLog) object;
        return Objects.equals(message, simpleLog.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message);
    }


}

package com.monikit.core;

import java.util.Map;
import java.util.Objects;

/**
 * 메서드 실행 시간을 기록하는 로그 클래스.
 * <p>
 * 특정 메서드가 실행되는 데 걸린 시간을 측정하여 성능 모니터링 및 최적화에 활용된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public abstract class ExecutionTimeLog extends AbstractLogEntry {
    private final String className;
    private final String methodName;
    private final long executionTime;


    protected ExecutionTimeLog(String traceId, LogLevel logLevel, String className, String methodName, long executionTime) {
        super(traceId, logLevel);
        this.className = className;
        this.methodName = methodName;
        this.executionTime = executionTime;
    }

    @Override
    public LogType getLogType() {
        return LogType.EXECUTION_TIME;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("className", className);
        logMap.put("methodName", methodName);
        logMap.put("executionTime", executionTime + "ms");
    }

}

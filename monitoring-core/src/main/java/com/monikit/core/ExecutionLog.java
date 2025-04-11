package com.monikit.core;

import java.util.Map;


/**
 * 메서드 실행 정보를 기록하는 공통 Execution 로그 추상 클래스.
 * <p>
 * ExecutionDetailLog 에서 공통으로 사용하는 필드 및 로직을 정의함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class ExecutionLog extends AbstractLogEntry {
    protected final String className;
    protected final String methodName;
    protected final long executionTime;

    protected ExecutionLog(String traceId, LogLevel logLevel, String className, String methodName, long executionTime) {
        super(traceId, logLevel);
        this.className = className;
        this.methodName = methodName;
        this.executionTime = executionTime;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public LogType getLogType() {
        return LogType.EXECUTION_TIME;
    }


    public static ExecutionLog create(String traceId, String className, String methodName,
                                            long executionTime) {
        return new ExecutionLog(traceId, LogLevel.INFO, className, methodName, executionTime);
    }


    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("className", className);
        logMap.put("methodName", methodName);
        logMap.put("executionTime", executionTime + "ms");
    }

}

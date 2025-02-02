package com.monikit.core;

import java.util.Map;

/**
 * 메서드 실행 시간과 함께 입력값 및 출력값을 기록하는 로그 클래스.
 * <p>
 * 특정 메서드가 실행될 때, 어떤 입력값을 받았고 어떤 값을 반환했는지를 포함하여
 * 성능 분석 및 디버깅에 활용된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class ExecutionDetailLog extends AbstractLogEntry {
    private final String className;
    private final String methodName;
    private final long executionTime;
    private final String inputParams;
    private final String outputValue;

    protected ExecutionDetailLog(String traceId, String className, String methodName, long executionTime,
                                 String inputParams, String outputValue, LogLevel logLevel) {
        super(traceId, logLevel);
        this.className = className;
        this.methodName = methodName;
        this.executionTime = executionTime;
        this.inputParams = inputParams;
        this.outputValue = outputValue;
    }

    @Override
    public LogType getLogType() {
        return LogType.EXECUTION_DETAIL;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("className", className);
        logMap.put("methodName", methodName);
        logMap.put("executionTime", executionTime + "ms");
        logMap.put("inputParams", inputParams);
        logMap.put("outputValue", outputValue);
    }

    public static ExecutionDetailLog create(String traceId, String className, String methodName,
                                            long executionTime, String inputParams, String outputValue, LogLevel logLevel) {
        return new ExecutionDetailLog(traceId, className, methodName, executionTime, inputParams, outputValue, logLevel);
    }
}

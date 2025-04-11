package com.monikit.core;

import java.util.Map;
import java.util.Objects;

/**
 * 메서드 실행 시간과 함께 입력값 및 출력값을 기록하는 상세 로그 클래스.
 * <p>
 * 실행 시간이 설정된 임계값(threshold)을 초과한 경우에만 로깅되도록 설계됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class ExecutionDetailLog extends ExecutionLog {

    private final String input;
    private final String output;
    private final boolean thresholdExceeded;
    private final long threshold;

    public ExecutionDetailLog(String traceId, String className, String methodName,
                              long executionTime, String input, String output,
                              LogLevel logLevel, boolean thresholdExceeded, long threshold) {
        super(traceId, logLevel, className, methodName, executionTime);
        this.input = input;
        this.output = output;
        this.thresholdExceeded = thresholdExceeded;
        this.threshold = threshold;
    }

    public static ExecutionDetailLog create(String traceId, String className, String methodName,
                                            long executionTime, String input, String output, long threshold) {
        boolean exceeded = executionTime > threshold;
        LogLevel level = exceeded ? LogLevel.WARN : LogLevel.INFO;
        return new ExecutionDetailLog(traceId, className, methodName, executionTime, input, output, level, exceeded, threshold);
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public boolean isThresholdExceeded() {
        return thresholdExceeded;
    }

    public long getThreshold() {
        return threshold;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        super.addExtraFields(logMap);
        logMap.put("input", input);
        logMap.put("output", output);
        logMap.put("threshold", threshold + "ms");
        logMap.put("thresholdExceeded", thresholdExceeded);
    }

    @Override
    public LogType getLogType() {
        return LogType.EXECUTION_DETAIL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionDetailLog that = (ExecutionDetailLog) o;
        return executionTime == that.executionTime &&
            thresholdExceeded == that.thresholdExceeded &&
            threshold == that.threshold &&
            Objects.equals(className, that.className) &&
            Objects.equals(methodName, that.methodName) &&
            Objects.equals(input, that.input) &&
            Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, executionTime, input, output, thresholdExceeded, threshold);
    }

    @Override
    public String toString() {
        return "ExecutionDetailLog{" +
            "className='" + className + '\'' +
            ", methodName='" + methodName + '\'' +
            ", executionTime=" + executionTime +
            ", input='" + input + '\'' +
            ", output='" + output + '\'' +
            ", threshold=" + threshold +
            ", thresholdExceeded=" + thresholdExceeded +
            '}';
    }
}

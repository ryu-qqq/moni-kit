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
    private final String tag;

    public ExecutionDetailLog(String traceId, String className, String methodName,
                              long executionTime, String input, String output,
                              LogLevel logLevel, boolean thresholdExceeded, long threshold, String tag) {
        super(traceId, logLevel, className, methodName, executionTime);
        this.input = input;
        this.output = output;
        this.thresholdExceeded = thresholdExceeded;
        this.threshold = threshold;
        this.tag = tag;
    }

    public static ExecutionDetailLog create(String traceId, String className, String methodName,
                                            long executionTime, String input, String output, long threshold, String tag) {
        boolean exceeded = executionTime > threshold;
        LogLevel level = exceeded ? LogLevel.WARN : LogLevel.INFO;
        return new ExecutionDetailLog(traceId, className, methodName, executionTime, input, output, level, exceeded, threshold, tag);
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

    public String getTag() {
        return tag;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        super.addExtraFields(logMap);
        logMap.put("input", input);
        logMap.put("output", output);
        logMap.put("threshold", threshold + "ms");
        logMap.put("thresholdExceeded", thresholdExceeded);
        logMap.put("tag", tag);

    }

    @Override
    public LogType getLogType() {
        return LogType.EXECUTION_DETAIL;
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        ExecutionDetailLog that = (ExecutionDetailLog) object;
        return thresholdExceeded
            == that.thresholdExceeded
            && threshold
            == that.threshold
            && Objects.equals(input, that.input)
            && Objects.equals(output, that.output)
            && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output, thresholdExceeded, threshold, tag);
    }

    @Override
    public String toString() {
        return "ExecutionDetailLog{"
            +
            "input='"
            + input
            + '\''
            +
            ", output='"
            + output
            + '\''
            +
            ", thresholdExceeded="
            + thresholdExceeded
            +
            ", threshold="
            + threshold
            +
            ", tag='"
            + tag
            + '\''
            +
            '}';
    }
}

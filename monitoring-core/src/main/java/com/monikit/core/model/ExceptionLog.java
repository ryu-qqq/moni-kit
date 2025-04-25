package com.monikit.core.model;

import java.util.Map;
import java.util.Objects;

import com.monikit.core.LogLevel;
import com.monikit.core.LogType;

/**
 * 예외 발생 시 스택 트레이스를 기록하는 로그 클래스.
 * <p>
 * - 애플리케이션 내에서 발생한 예외를 기록하여 디버깅 및 장애 분석에 활용됩니다.
 * - {@code Throwable}을 받아, 내부적으로 예외 타입, 메시지, 스택 트레이스를 문자열로 저장합니다.
 * - 별도의 ErrorCategory 없이, 예외 클래스명(exceptionType)을 통해 메트릭 분석 및 필터링이 가능합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class ExceptionLog extends AbstractLogEntry {
    private final String sourceClass;
    private final String sourceMethod;
    private final String exceptionType;
    private final String message;
    private final String stackTrace;

    public ExceptionLog(String traceId, Throwable exception) {
        super(traceId, LogLevel.ERROR);
        Throwable rootCause = getRootCause(exception);
        StackTraceElement[] stackTraceElements = rootCause.getStackTrace();
        this.sourceClass = stackTraceElements.length > 0 ? stackTraceElements[0].getClassName() : "Unknown";
        this.sourceMethod = stackTraceElements.length > 0 ? stackTraceElements[0].getMethodName() : "Unknown";
        this.exceptionType = exception.getClass().getSimpleName();
        this.message = exception.getMessage();
        this.stackTrace = getStackTraceAsString(exception);
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    @Override
    public LogType getLogType() {
        return LogType.EXCEPTION;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("exceptionType", exceptionType);
        logMap.put("message", message);
        logMap.put("stackTrace", stackTrace);
    }

    public static ExceptionLog of(String traceId, Throwable exception){
        return new ExceptionLog(traceId, exception);
    }

    private String getStackTraceAsString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTraceElements = e.getStackTrace();

        int maxElements = 10;
        for (int i = 0; i < Math.min(stackTraceElements.length, maxElements); i++) {
            sb.append(stackTraceElements[i].toString()).append("\n");
        }
        if (stackTraceElements.length > maxElements) {
            sb.append("... 이하 생략 ...\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExceptionLog that = (ExceptionLog) o;
        return Objects.equals(exceptionType, that.exceptionType) &&
            Objects.equals(message, that.message) &&
            Objects.equals(stackTrace, that.stackTrace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exceptionType, message, stackTrace);
    }

}

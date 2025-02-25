package com.monikit.core;

import java.util.Map;
import java.util.Objects;

/**
 * 예외 발생 시 스택 트레이스를 기록하는 로그 클래스.
 * <p>
 * 애플리케이션 내에서 발생한 예외를 기록하여 디버깅 및 장애 분석에 활용된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public class ExceptionLog extends AbstractLogEntry {
    private final String sourceClass;
    private final String sourceMethod;
    private final ErrorCategory errorCategory;

    private final String exceptionMessage;
    private final String stackTrace;

    protected ExceptionLog(String traceId, Throwable exception, ErrorCategory errorCategory) {
        super(traceId, LogLevel.ERROR);
        Throwable rootCause = getRootCause(exception);

        StackTraceElement[] stackTraceElements = rootCause.getStackTrace();
        this.sourceClass = stackTraceElements.length > 0 ? stackTraceElements[0].getClassName() : "Unknown";
        this.sourceMethod = stackTraceElements.length > 0 ? stackTraceElements[0].getMethodName() : "Unknown";
        this.errorCategory = errorCategory;
        this.exceptionMessage = rootCause.getMessage();
        this.stackTrace = getStackTraceAsString(rootCause);
    }

    @Override
    public LogType getLogType() {
        return LogType.EXCEPTION;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("exceptionMessage", exceptionMessage);
        logMap.put("stackTrace", stackTrace);
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    private static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
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


    public String getSourceClass() {
        return sourceClass;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public ErrorCategory getErrorCategory() {
        return errorCategory;
    }

    public static ExceptionLog create(String traceId, Throwable exception, ErrorCategory errorCategory) {
        return new ExceptionLog(traceId, exception, errorCategory);
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        ExceptionLog that = (ExceptionLog) object;
        return Objects.equals(sourceClass, that.sourceClass)
            && Objects.equals(sourceMethod, that.sourceMethod)
            && errorCategory
            == that.errorCategory
            && Objects.equals(exceptionMessage, that.exceptionMessage)
            && Objects.equals(stackTrace, that.stackTrace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceClass, sourceMethod, errorCategory, exceptionMessage, stackTrace);
    }

    @Override
    public String toString() {
        return "ExceptionLog{"
            +
            "sourceClass='"
            + sourceClass
            + '\''
            +
            ", sourceMethod='"
            + sourceMethod
            + '\''
            +
            ", errorCategory="
            + errorCategory
            +
            ", exceptionMessage='"
            + exceptionMessage
            + '\''
            +
            ", stackTrace='"
            + stackTrace
            + '\''
            +
            '}';
    }
}
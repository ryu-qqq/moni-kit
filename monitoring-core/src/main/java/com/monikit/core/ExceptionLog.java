package com.monikit.core;

import java.util.Map;

/**
 * 예외 발생 시 스택 트레이스를 기록하는 로그 클래스.
 * <p>
 * 애플리케이션 내에서 발생한 예외를 기록하여 디버깅 및 장애 분석에 활용된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class ExceptionLog extends AbstractLogEntry {
    private final String exceptionMessage;
    private final String stackTrace;

    protected ExceptionLog(String traceId, Exception exception) {
        super(traceId, LogLevel.ERROR);
        this.exceptionMessage = exception.getMessage();
        this.stackTrace = getStackTraceAsString(exception);
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

    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    public static ExceptionLog create(String traceId, Exception exception) {
        return new ExceptionLog(traceId, exception);
    }
}

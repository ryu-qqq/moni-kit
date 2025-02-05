package com.monikit.core;

import java.util.concurrent.Callable;

/**
 * LogEntryContext를 관리하는 매니저 클래스.
 * <p>
 * 외부에서 직접 LogEntryContext를 조작할 수 없으며, 이 클래스를 통해서만 로그를 추가하고 관리한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class LogEntryContextManager {

    private static final int MAX_LOG_SIZE = 300;
    private static LogNotifier logNotifier;
    private static ErrorLogNotifier errorLogNotifier = logEntry -> {};

    /**
     * LogNotifier를 설정한다 (monitoring-starter에서 주입 가능).
     *
     * @param notifier 사용할 LogNotifier 구현체
     */
    public static void setLogNotifier(LogNotifier notifier) {
        logNotifier = notifier;
    }

    /**
     * 로그를 추가할 때, 로그 개수가 너무 많으면 컨텍스트를 초기화 후 다시 추가한다.
     *
     * @param logEntry 저장할 로그 객체
     */
    public static void addLog(LogEntry logEntry) {
        if (LogEntryContext.size() >= MAX_LOG_SIZE) {
            logNotifier.notify(LogLevel.WARN, "LogEntryContext cleared due to size limit");
            flush();
            LogEntryContext.clear();
        }

        LogEntryContext.addLog(logEntry);
    }

    /**
     * 요청이 끝날 때 실행된 모든 로그를 출력하고 컨텍스트를 정리한다.
     */
    public static void flush() {
        for (LogEntry log : LogEntryContext.getLogs()) {
            logNotifier.notify(log);
            if(log.getLogLevel().isEmergency()){
                errorLogNotifier.onErrorLogDetected(log);
            }
        }

        LogEntryContext.clear();
        LogEntryContext.setErrorOccurred(false);
    }

    /**
     * 부모 스레드의 컨텍스트를 자식 스레드로 전달하는 Runnable을 생성한다.
     *
     * @param task 실행할 Runnable
     * @return 부모 스레드의 컨텍스트가 복사된 새로운 Runnable
     */
    public static Runnable propagateToChildThread(Runnable task) {
        return LogEntryContext.propagateToChildThread(task);
    }


    /**
     * 부모 스레드의 컨텍스트를 자식 스레드로 전달하는 Callable을 생성한다.
     * @param task 실행할 Callable
     * @return 부모 스레드의 컨텍스트가 복사된 새로운 Callable
     * @param <T>
     */

    public static <T> Callable<T> propagateToChildThread(Callable<T> task) {
        return LogEntryContext.propagateToChildThread(task);
    }

    /**
     * 예외를 로깅하는 메서드 (AOP에서 처리하지 않고 이곳에서 예외를 기록하도록 변경)
     *
     * @param traceId 트레이스 ID
     * @param exception 발생한 예외
     */
    public static void logException(String traceId, Throwable exception) {
        if (LogEntryContext.hasError()) {
            return;
        }

        LogEntryContext.addLog(ExceptionLog.create(traceId, exception));
        LogEntryContext.setErrorOccurred(true);
    }


}

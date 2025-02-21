package com.monikit.core;

/**
 * 요청 단위의 로그 컨텍스트를 관리하는 기본 구현체.
 * <p>
 * - {@link LogEntryContextManager}를 구현하여 로그 컨텍스트 관리 기능을 제공한다.
 * - 멀티스레드 환경에서도 로그 컨텍스트를 유지할 수 있도록 {@code propagateToChildThread()} 제공.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class DefaultLogEntryContextManager implements LogEntryContextManager {

    private static final int MAX_LOG_SIZE = 300;
    private final LogNotifier logNotifier;
    private final ErrorLogNotifier errorLogNotifier;

    public DefaultLogEntryContextManager(LogNotifier logNotifier, ErrorLogNotifier errorLogNotifier) {
        this.logNotifier = logNotifier;
        this.errorLogNotifier = errorLogNotifier;
    }

    @Override
    public void addLog(LogEntry logEntry) {
        if (LogEntryContext.size() >= MAX_LOG_SIZE) {
            logNotifier.notify(LogLevel.WARN, "LogEntryContext cleared due to size limit");
            flush();
        }
        LogEntryContext.addLog(logEntry);
    }

    @Override
    public void flush() {
        for (LogEntry log : LogEntryContext.getLogs()) {
            logNotifier.notify(log);
            if (log.getLogLevel().isEmergency() && log instanceof ExceptionLog exceptionLog) {
                errorLogNotifier.onErrorLogDetected(exceptionLog);
            }
        }
        clear();
    }

    @Override
    public void clear() {
        LogEntryContext.clear();
    }



}
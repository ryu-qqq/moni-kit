package com.monikit.starter;

import com.monikit.core.LogEntryContextManager;

/**
 * 요청 단위로 LogEntryContext를 자동으로 관리하는 클래스.
 * <p>
 * - try-with-resources를 활용하여 요청이 끝나면 자동으로 flush()가 호출된다.
 * - DI를 통해 {@link LogEntryContextManager}를 주입받아 사용.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class LogContextScope implements AutoCloseable {

    private final LogEntryContextManager logEntryContextManager;

    public LogContextScope(LogEntryContextManager logEntryContextManager) {
        this.logEntryContextManager = logEntryContextManager;
        logEntryContextManager.clear();
    }

    @Override
    public void close() {
        logEntryContextManager.flush();
    }

}
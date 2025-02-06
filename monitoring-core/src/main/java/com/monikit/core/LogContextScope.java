package com.monikit.core;

/**
 * 요청 단위로 LogEntryContext를 자동으로 관리하는 클래스.
 * <p>
 * try-with-resources를 활용하여 요청이 끝나면 자동으로 flush()가 호출된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class LogContextScope implements AutoCloseable {

    public LogContextScope() {}

    @Override
    public void close() {
        LogEntryContextManager.flush();
    }

}

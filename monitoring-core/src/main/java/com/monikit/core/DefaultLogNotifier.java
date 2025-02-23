package com.monikit.core;


/**
 * 기본 로그 노티파이어 (System.out.println() 기반).
 * <p>
 * monitoring-starter가 없을 때 기본적으로 사용된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class DefaultLogNotifier implements LogNotifier {

    private static final DefaultLogNotifier INSTANCE = new DefaultLogNotifier();

    private DefaultLogNotifier() {}

    public static DefaultLogNotifier getInstance() {
        return INSTANCE;
    }

    @Override
    public void notify(LogLevel logLevel, String message) {
        System.out.println(message);
    }

    @Override
    public void notify(LogEntry logEntry) {
        System.out.println(logEntry.toString());
    }

}
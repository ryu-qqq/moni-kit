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

    @Override
    public void notify(LogLevel logLevel, String message) {
        System.out.println(message);
    }
}

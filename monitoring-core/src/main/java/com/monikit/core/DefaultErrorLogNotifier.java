package com.monikit.core;

/**
 * 기본 에러 로그 노티파이어
 * <p>
 * monitoring-starter가 없을 때 기본적으로 사용된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */

public class DefaultErrorLogNotifier implements ErrorLogNotifier {

    private static final DefaultErrorLogNotifier INSTANCE = new DefaultErrorLogNotifier();

    private DefaultErrorLogNotifier() {}

    public static DefaultErrorLogNotifier getInstance() {
        return INSTANCE;
    }

    @Override
    public void onErrorLogDetected(ExceptionLog logEntry) {
        System.out.printf("Error, %s%n", logEntry.toString());
    }

}

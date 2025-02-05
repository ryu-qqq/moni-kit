package com.monikit.core;

/**
 * 에러 로그 감지를 위한 후크 인터페이스.
 * - 특정 에러 로그가 감지되었을 때 실행될 콜백을 제공한다.
 */
public interface ErrorLogNotifier {
    void onErrorLogDetected(LogEntry logEntry);
}

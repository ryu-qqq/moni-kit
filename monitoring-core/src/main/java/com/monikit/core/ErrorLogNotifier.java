package com.monikit.core;

/**
 * 에러 로그 감지를 위한 후크 인터페이스.
 * <p>
 * 1.1.0 이후 {@link LogNotifier}와 {@link LogSink} 기반 아키텍처로 통합되었기 때문에,
 * 이 인터페이스는 더 이상 사용되지 않으며 확장성 있는 알림 전략은 {@link LogSink}로 대체되었습니다.
 * </p>
 *
 * @deprecated 1.1.0부터 {@link LogSink}로 대체됨. 예외 감지 및 알림은 이제 LogSink 전략에 따라 유연하게 처리됩니다.
 */
@Deprecated
public interface ErrorLogNotifier {
    /**
     * 예외 로그가 감지되었을 때 호출됩니다.
     * 더 이상 사용되지 않으며, {@link LogSink#send(LogEntry)}를 활용한 전송 방식으로 대체되었음.
     *
     * @param logEntry 예외 로그 엔트리
     */

    void onErrorLogDetected(ExceptionLog logEntry);

}

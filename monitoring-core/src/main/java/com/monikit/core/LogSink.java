package com.monikit.core;

/**
 * 로그 전송 채널(Sink)을 추상화한 인터페이스.
 * <p>
 * 각 로그 타입(LogType)에 따라 Slack, S3, Console 등 다양한 전송 채널로
 * 로그를 전달할 수 있도록 확장 가능한 전략 인터페이스입니다.
 * </p>
 * <p>
 * - 예: SlackSink는 EXCEPTION 타입만 처리하고,
 *   ConsoleSink는 모든 로그를 처리하도록 설정할 수 있습니다.
 * - {@link LogType} 기반 분기처리로 유연한 분산 로깅이 가능합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
public interface LogSink {

    /**
     * 지원하는 로그 타입 여부를 반환합니다.
     *
     * @param logType 로그 타입
     * @return 해당 타입을 처리할 수 있으면 true
     */
    boolean supports(LogType logType);

    /**
     * 로그를 실제 전송합니다.
     *
     * @param logEntry 로그 엔트리
     */
    void send(LogEntry logEntry);

}

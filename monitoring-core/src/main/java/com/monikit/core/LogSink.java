package com.monikit.core;

/**
 * 로그 전송 채널(Sink)을 추상화한 인터페이스.
 * <p>
 * 로그가 실제 외부 시스템(예: Slack, S3, File, Kafka 등)으로 **전송되는 목적지**를 정의합니다.
 * {@link LogNotifier}를 통해 전송되는 로그는 등록된 Sink들 중 {@link #supports(LogType)}가 true인 대상에게 전달됩니다.
 * </p>
 *
 * <h3>책임</h3>
 * <ul>
 *   <li>로그의 최종 전송 처리 (I/O 포함)</li>
 *   <li>{@link LogType} 기준으로 처리 여부 결정 (예: EXCEPTION만 전송)</li>
 *   <li>단일 로그 단위로 동작</li>
 * </ul>
 *
 * <p>
 * <b>예시:</b> SlackSink는 ERROR/EXCEPTION 로그만 전송, ConsoleSink는 모든 로그 출력 등
 * </p>
 *
 * @see LogAddHook
 * @see LogFlushHook
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

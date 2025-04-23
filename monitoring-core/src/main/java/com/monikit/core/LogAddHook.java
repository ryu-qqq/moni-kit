package com.monikit.core;


/**
 * 로그가 컨텍스트에 추가될 때 호출되는 실시간 후처리 훅 인터페이스.
 * <p>
 * {@link LogEntryContextManager#addLog(LogEntry)} 시점에
 * 로그가 저장되자마자 실행되며, **단일 로그 단위로 후처리**가 필요할 때 사용됩니다.
 * </p>
 *
 * <h3>주요 사용처</h3>
 * <ul>
 *   <li>특정 로그 레벨 감지 → 실시간 Slack 알림</li>
 *   <li>에러 발생 시 상태 토글, Redis 연동</li>
 *   <li>메트릭 수집 트리거 (예: Micrometer 카운터 증가)</li>
 * </ul>
 *
 * <p>
 * <b>주의:</b> 로그 전송은 {@link LogSink}가 담당하며,
 * 이 훅은 **전송 목적이 아닌 부가 처리**를 위한 용도입니다.
 * </p>
 *
 * @see LogSink
 * @see LogFlushHook
 * @since 1.1.0
 */

public interface LogAddHook {
    /**
     * 로그가 컨텍스트에 추가될 때 호출됩니다.
     *
     * @param logEntry 추가된 로그
     */
    void onAdd(LogEntry logEntry);
}
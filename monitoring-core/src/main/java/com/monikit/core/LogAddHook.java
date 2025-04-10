package com.monikit.core;


/**
 * 로그가 컨텍스트에 추가될 때 호출되는 후처리 훅 인터페이스.
 * <p>
 * 로그가 {@link LogEntryContextManager#addLog(LogEntry)}를 통해 추가되는 시점에
 * 개별 로그에 대한 후처리를 수행하고자 할 때 구현합니다.
 * - 에러 알림 전송 (Slack, SMS)
 * - 연동 상태 조정
 * - 로그 유형별 동적 대응
 * </p>
 *
 * <p>
 * 이 훅은 로그가 저장될 때마다 호출되며, 실시간 대응이 필요한
 * 알림, 트리거, 모니터링 이벤트 처리에 적합합니다.
 * </p>
 *
 * @author ryu-qqq
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
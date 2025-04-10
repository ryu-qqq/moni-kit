package com.monikit.core;

import java.util.List;


/**
 * 로그가 flush 될 때 호출되는 후처리 훅 인터페이스.
 * <p>
 * 로그 컨텍스트가 flush 될 때, 수집된 로그 전체에 대해
 * 추가 작업을 수행하고자 할 때 구현합니다.
 * - 로그 집계
 * - 외부 시스템 전송 (예: S3, DB)
 * - 통계 수집, 알림 등
 * </p>
 *
 * <p>
 * {@link LogEntryContextManager#flush()} 내부에서 호출되며,
 * 단일 요청 단위의 모든 로그를 대상으로 후처리 작업을 수행할 수 있습니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */


public interface LogFlushHook {
    /**
     * 로그가 flush 될 때 호출됩니다.
     *
     * @param logEntries flush 대상 로그 목록
     */
    void onFlush(List<LogEntry> logEntries);
}

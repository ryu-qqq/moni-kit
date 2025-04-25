package com.monikit.core.hook;

import java.util.List;

import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.notifier.LogNotifier;
import com.monikit.core.notifier.LogSink;
import com.monikit.core.model.LogEntry;

/**
 * 로그 컨텍스트가 flush될 때 호출되는 일괄 후처리 훅 인터페이스.
 * <p>
 * {@link LogEntryContextManager#flush()} 호출 시,
 * **요청 단위 전체 로그 리스트**를 대상으로 후처리를 수행할 때 사용됩니다.
 * </p>
 *
 * <h3>주요 사용처</h3>
 * <ul>
 *   <li>DB 또는 S3 등에 로그 일괄 저장</li>
 *   <li>압축 후 전송, 로그 집계</li>
 *   <li>요청 단위 통계 계산</li>
 * </ul>
 *
 * <p>
 * <b>주의:</b> 로그는 이미 {@link LogNotifier}를 통해 전송되었을 수 있으며,
 * 이 훅은 후속적인 저장/분석용으로 활용됩니다.
 * </p>
 *
 * @see LogSink
 * @see LogAddHook
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

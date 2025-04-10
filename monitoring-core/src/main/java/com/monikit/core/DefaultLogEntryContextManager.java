package com.monikit.core;

import java.util.List;

/**
 * 요청 단위의 로그 컨텍스트를 관리하는 기본 구현체.
 * <p>
 * {@link LogEntryContextManager}를 구현하여, 로그를 수집하고 요청 단위로 관리하며,
 * {@link LogNotifier}, {@link LogAddHook}, {@link LogFlushHook}을 통해 유연한 후처리를 지원합니다.
 * </p>
 *
 * <p>
 * 주요 특징:
 * </p>
 * <ul>
 *     <li>로그 추가 시 {@link LogAddHook} 리스트를 통해 알림, 통계, 이벤트 후처리를 유연하게 수행</li>
 *     <li>flush 시 {@link LogFlushHook}을 통해 로그 집계 및 외부 전송 등 배치성 후처리 지원</li>
 *     <li>{@link LogNotifier}를 통해 로그를 전송하거나 기록 (예: Console, Slack, File 등)</li>
 *     <li>{@code MAX_LOG_SIZE} 초과 시 자동으로 flush 수행</li>
 *     <li>멀티스레드 환경에서도 로그 컨텍스트를 안전하게 유지할 수 있도록 설계됨</li>
 * </ul>
 *
 * <p>
 * 이 클래스는 직접 MetricCollector를 관리하지 않으며,
 * 필요 시 {@link MetricCollectorLogAddHook}과 같은 Hook을 통해 메트릭 수집 기능을 확장할 수 있습니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class DefaultLogEntryContextManager implements LogEntryContextManager {

    private static final int MAX_LOG_SIZE = 300;
    private final LogNotifier logNotifier;
    private final List<LogAddHook> addHooks;
    private final List<LogFlushHook> flushHooks;

    public DefaultLogEntryContextManager(LogNotifier logNotifier, List<LogAddHook> addHooks, List<LogFlushHook> flushHooks) {
        this.logNotifier = logNotifier;
        this.addHooks = addHooks;
        this.flushHooks = flushHooks;
    }

    @Override
    public void addLog(LogEntry logEntry) {
        if (LogEntryContext.size() >= MAX_LOG_SIZE) {
            logNotifier.notify(LogLevel.WARN, "LogEntryContext cleared due to size limit");
            flush();
        }
        LogEntryContext.addLog(logEntry);
        addHooks.forEach(h -> h.onAdd(logEntry));
    }


    @Override
    public void flush() {
        List<LogEntry> logs = LogEntryContext.getLogs().stream().toList();
        for (LogEntry log : logs) {
            logNotifier.notify(log);
        }

        flushHooks.forEach(h -> h.onFlush(logs));
        clear();
    }

    @Override
    public void clear() {
        LogEntryContext.clear();
    }

}
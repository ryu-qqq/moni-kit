package com.monikit.slf4j;

import java.util.List;

import com.monikit.core.LogLevel;
import com.monikit.core.notifier.LogNotifier;
import com.monikit.core.notifier.LogSink;
import com.monikit.core.TraceIdProvider;
import com.monikit.core.model.LogEntry;
import com.monikit.core.model.SimpleLog;

/**
 * SLF4J 기반 {@link LogNotifier} 기본 구현체.
 * <p>
 * {@link LogEntry} 타입의 로그를 {@link LogSink} 리스트를 통해 전송합니다.
 * - {@link LogSink#supports(com.monikit.core.LogType)} 를 통해 로그 타입별로 전송 여부를 판단합니다.
 * </p>
 *
 * <p>
 * 또한, 단순 메시지 기반 로그는 {@link SimpleLog} 형태로 변환하여 처리되며,
 * {@link TraceIdProvider}를 통해 traceId를 자동 주입합니다.
 * </p>
 *
 *
 * @see LogSink
 * @see SimpleLog
 * @see com.monikit.core.TraceIdProvider
 * @since 1.1.2
 */

public class Slf4jLogger implements LogNotifier {

    private final List<LogSink> sinks;
    private final TraceIdProvider traceIdProvider;


    /**
     * Slf4jLogger 생성자.
     *
     * @param sinks 로그 전송 대상 Sink 목록
     * @param traceIdProvider 현재 Trace ID 제공자
     */

    public Slf4jLogger(List<LogSink> sinks, TraceIdProvider traceIdProvider) {
        this.sinks = sinks;
        this.traceIdProvider = traceIdProvider;
    }

    /**
     * 단순 문자열 로그를 {@link SimpleLog}로 감싸서 전송합니다.
     *
     * @param logLevel 로그 레벨
     * @param message  출력할 로그 메시지
     */

    @Override
    public void notify(LogLevel logLevel, String message) {
        SimpleLog simpleLog = SimpleLog.of(traceIdProvider.getTraceId(), logLevel, message);
        sinks.forEach(sink -> sink.send(simpleLog));
    }

    /**
     * 구조화 로그를 등록된 Sink들에 전송합니다.
     *
     * @param logEntry 전송할 로그 객체
     */

    @Override
    public void notify(LogEntry logEntry) {
        for (LogSink sink : sinks) {
            if (sink.supports(logEntry.getLogType())) {
                sink.send(logEntry);
            }
        }
    }

}

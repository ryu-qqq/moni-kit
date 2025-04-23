package com.monikit.core;

import java.util.List;

/**
 * LogNotifier의 기본 구현체.
 * <p>
 * 사용자 정의 LogNotifier 빈이 없을 경우 기본으로 동작하며,
 * 등록된 {@link LogSink} 리스트를 기반으로 로그를 분기 전송합니다.
 * - 별도 LogSink가 없으면 System.out 으로 출력합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

/**
 * @deprecated SLF4J 기반 로그 시스템이 기본값으로 전환되었습니다.
 *             {@link com.monikit.slf4j.Slf4jLogger} 사용을 권장합니다.
 */
@Deprecated
public class DefaultLogNotifier implements LogNotifier {

    private final List<LogSink> sinks;

    public DefaultLogNotifier(List<LogSink> sinks) {
        this.sinks = sinks;
    }

    @Override
    public void notify(LogLevel logLevel, String message) {
        System.out.println("[" + logLevel + "] " + message);
    }

    @Override
    public void notify(LogEntry logEntry) {
        if (sinks == null || sinks.isEmpty()) {
            System.out.println("[monikit] " + logEntry);
            return;
        }

        for (LogSink sink : sinks) {
            if (sink.supports(logEntry.getLogType())) {
                sink.send(logEntry);
            }
        }
    }
}

package com.monikit.core.notifier;

import java.util.List;

import com.monikit.core.DefaultTraceIdProvider;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;
import com.monikit.core.model.LogEntry;
import com.monikit.core.model.SimpleLog;


/**
 * {@link LogNotifier}의 기본 구현체.
 *
 * <p>
 * 이 클래스는 {@link LogSink} 리스트를 주입받아, 로그 타입에 따라 적절한 Sink에 로그를 전송합니다.
 * </p>
 *
 * <h3>책임</h3>
 * <ul>
 *   <li>문자열 또는 {@link LogEntry} 객체를 받아 로그 전송</li>
 *   <li>{@link LogSink#supports} 메서드를 통해 처리 대상 Sink 분기</li>
 * </ul>
 *
 * <p>
 * {@code LogSink} 리스트는 외부에서 조립되며, {@code LogSinkCustomizer}를 통해 확장 가능합니다.
 * </p>
 *
 * @author ryu
 * @since 1.1.2
 */
public class DefaultLogNotifier implements LogNotifier {

    private final TraceIdProvider traceIdProvider;
    private final List<LogSink> sinks;

    public DefaultLogNotifier(List<LogSink> sinks, TraceIdProvider traceIdProvider) {
        this.traceIdProvider = traceIdProvider;
        this.sinks = sinks;
    }

    @Override
    public void notify(LogLevel level, String msg) {
        notify(SimpleLog.of(traceIdProvider.getTraceId(), level, msg));
    }

    @Override
    public void notify(LogEntry entry) {
        for (LogSink sink : sinks) {
            if (sink.supports(entry.getLogType())) {
                sink.send(entry);
            }
        }
    }

}

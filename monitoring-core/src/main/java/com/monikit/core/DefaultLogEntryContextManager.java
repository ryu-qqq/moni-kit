package com.monikit.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 요청 단위의 로그 컨텍스트를 관리하는 기본 구현체.
 * <p>
 * - {@link LogEntryContextManager}를 구현하여 로그 컨텍스트 관리 기능을 제공한다.
 * - 멀티스레드 환경에서도 로그 컨텍스트를 유지할 수 있도록 {@code propagateToChildThread()} 제공.
 * - {@link MetricCollector}를 활용하여 로그 수집 시 자동으로 관련 메트릭을 기록하도록 개선됨.
 * - {@link LogType}을 기반으로 등록된 메트릭 수집기를 빠르게 조회할 수 있도록 {@code Map<LogType, List<MetricCollector>>} 구조로 변경.
 * - {@code Optional.ofNullable()} 및 {@code getOrDefault()} 활용하여 빈 리스트 처리 시 NPE(NullPointerException) 방지.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */

public class DefaultLogEntryContextManager implements LogEntryContextManager {

    private static final int MAX_LOG_SIZE = 300;
    private final LogNotifier logNotifier;
    private final ErrorLogNotifier errorLogNotifier;
    private final Map<LogType, List<MetricCollector<? extends LogEntry>>> metricCollectorMap;


    public DefaultLogEntryContextManager(LogNotifier logNotifier, ErrorLogNotifier errorLogNotifier,
                                         List<MetricCollector<? extends LogEntry>> metricCollectors) {
        this.logNotifier = logNotifier;
        this.errorLogNotifier = errorLogNotifier;
        this.metricCollectorMap = Optional.ofNullable(metricCollectors)
            .orElse(Collections.emptyList())
            .stream()
            .flatMap(collector -> Arrays.stream(LogType.values())
                .filter(collector::supports)
                .map(type -> Map.entry(type, collector)))
            .collect(Collectors.groupingBy(Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    @Override
    public void addLog(LogEntry logEntry) {
        if (LogEntryContext.size() >= MAX_LOG_SIZE) {
            logNotifier.notify(LogLevel.WARN, "LogEntryContext cleared due to size limit");
            flush();
        }
        LogEntryContext.addLog(logEntry);

        List<MetricCollector<LogEntry>> collectors = (List<MetricCollector<LogEntry>>)
            (List<?>) metricCollectorMap.getOrDefault(logEntry.getLogType(), Collections.emptyList());

        collectors.forEach(collector -> collector.record(logEntry));
    }


    @Override
    public void flush() {
        for (LogEntry log : LogEntryContext.getLogs()) {
            logNotifier.notify(log);
            if (log.getLogLevel().isEmergency() && log instanceof ExceptionLog exceptionLog) {
                errorLogNotifier.onErrorLogDetected(exceptionLog);
            }
        }
        clear();
    }

    @Override
    public void clear() {
        LogEntryContext.clear();
    }



}
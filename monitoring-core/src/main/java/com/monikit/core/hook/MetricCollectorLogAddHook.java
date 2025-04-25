package com.monikit.core.hook;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.monikit.core.context.DefaultLogEntryContextManager;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.LogType;
import com.monikit.core.model.LogEntry;

/**
 * {@link MetricCollector}를 {@link LogAddHook} 기반 구조로 실행하기 위한 어댑터 클래스.
 * <p>
 * 로그가 {@link LogEntryContextManager#addLog(LogEntry)}를 통해 추가될 때,
 * 해당 로그의 {@link LogType}을 기준으로 지원되는 MetricCollector 들을 찾아
 * {@link MetricCollector#record(LogEntry)} 메서드를 호출합니다.
 * </p>
 *
 * <p>
 * 이 클래스는 기존의 MetricCollector 기능을 Hook 구조로 분리하여 적용 가능하게 하며,
 * {@link DefaultLogEntryContextManager}가 MetricCollector에 직접 의존하지 않도록 도와줍니다.
 * </p>
 *
 * <p>
 * 내부적으로 {@code Map<LogType, List<MetricCollector>>} 구조를 구성하여
 * 로그 타입 기반 빠른 조회 및 분기 처리를 수행합니다.
 * </p>
 *
 * @see LogAddHook
 * @see MetricCollector
 * @since 1.1.0
 */

public class MetricCollectorLogAddHook implements LogAddHook {

    private final Map<LogType, List<MetricCollector<? extends LogEntry>>> metricCollectorMap;

    public MetricCollectorLogAddHook(List<MetricCollector<? extends LogEntry>> metricCollectors) {
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
    public void onAdd(LogEntry logEntry) {
        List<MetricCollector<LogEntry>> collectors = (List<MetricCollector<LogEntry>>)
            (List<?>) metricCollectorMap.getOrDefault(logEntry.getLogType(), Collections.emptyList());
        collectors.forEach(c -> c.record(logEntry));
    }

}

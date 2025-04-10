package com.monikit.core;

import com.monikit.core.utils.TestLogEntryProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

@DisplayName("MetricCollectorLogAddHook 테스트")
class MetricCollectorLogAddHookTest {

    @Test
    @DisplayName("지원하는 로그 타입의 MetricCollector가 호출되어야 한다")
    void shouldCallMetricCollectorWhenSupported() {
        MetricCollector<LogEntry> mockCollector = mock(MetricCollector.class);
        when(mockCollector.supports(LogType.EXECUTION_TIME)).thenReturn(true);

        MetricCollectorLogAddHook hook = new MetricCollectorLogAddHook(List.of(mockCollector));
        LogEntry log = TestLogEntryProvider.executionTimeLog();

        hook.onAdd(log);

        verify(mockCollector, times(1)).record(log);
    }

    @Test
    @DisplayName("지원하지 않는 로그 타입이면 MetricCollector는 호출되지 않아야 한다")
    void shouldNotCallMetricCollectorWhenUnsupported() {
        MetricCollector<LogEntry> mockCollector = mock(MetricCollector.class);
        when(mockCollector.supports(LogType.EXCEPTION)).thenReturn(false);

        MetricCollectorLogAddHook hook = new MetricCollectorLogAddHook(List.of(mockCollector));
        LogEntry log = TestLogEntryProvider.exceptionLog();

        hook.onAdd(log);

        verify(mockCollector, never()).record(any());
    }
}
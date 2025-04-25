package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.core.hook.LogAddHook;
import com.monikit.core.model.LogEntry;
import com.monikit.core.LogType;
import com.monikit.core.hook.MetricCollector;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("MetricCollectorHookAutoConfiguration 조건부 등록 테스트")
class MetricCollectorHookAutoConfigurationTest {

    MetricCollector<LogEntry> mockCollector = mock(MetricCollector.class);

    private final ApplicationContextRunner baseRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MetricCollectorHookAutoConfiguration.class))
            .withBean(MetricCollector.class, () -> {
                when(mockCollector.supports(LogType.EXECUTION_TIME)).thenReturn(true);
                return mockCollector;
            });

    @Nested
    @DisplayName("metrics-enabled=true 일 때")
    class WhenMetricsEnabledTrue {
        @Test
        @DisplayName("MetricCollectorLogAddHook이 자동으로 등록되어야 한다")
        void shouldRegisterHook() {
            baseRunner
                .withPropertyValues("monikit.metrics.metrics-enabled=true")
                .run(context -> {
                    LogAddHook hook = context.getBean(LogAddHook.class);
                    assertNotNull(hook, "LogAddHook 빈이 등록되어야 한다");
                });
        }
    }

    @Nested
    @DisplayName("metrics-enabled=false 일 때")
    class WhenMetricsDisabled {
        @Test
        @DisplayName("MetricCollectorLogAddHook이 등록되지 않아야 한다")
        void shouldNotRegisterHook() {
            baseRunner
                .withPropertyValues("monikit.metrics.metrics-enabled=false")
                .run(context -> {
                    assertThrows(NoSuchBeanDefinitionException.class, () -> {
                        context.getBean(LogAddHook.class);
                    }, "LogAddHook 빈이 존재하지 않아야 한다");
                });
        }
    }

    @Nested
    @DisplayName("metrics-enabled 설정이 없을 때")
    class WhenMetricsEnabledMissing {
        @Test
        @DisplayName("기본값 true로 간주되어 등록되어야 한다 (matchIfMissing=true)")
        void shouldRegisterHookByDefault() {
            baseRunner
                .run(context -> {
                    assertInstanceOf(LogAddHook.class,
                        context.getBean(LogAddHook.class));
                });
        }
    }
}
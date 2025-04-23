package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.core.DefaultLogEntryContextManager;
import com.monikit.core.LogAddHook;
import com.monikit.core.LogEntry;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogFlushHook;
import com.monikit.core.LogNotifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class LogEntryContextManagerConfigTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogEntryContextManagerConfig.class))
            .withBean(LogNotifier.class, () -> mock(LogNotifier.class))
            .withBean(LogAddHook.class, () -> log -> {})
            .withBean(LogFlushHook.class, () -> logs -> {});

    @Nested
    @DisplayName("LogEntryContextManager 자동 등록 테스트")
    class LogEntryContextManagerAutoConfigurationTests {

        @Test
        @DisplayName("사용자 정의 빈이 없을 때 DefaultLogEntryContextManager가 자동 등록되어야 한다")
        void shouldRegisterDefaultLogEntryContextManagerWhenNoCustomBean() {
            contextRunner
                .run(context -> {
                    assertTrue(context.containsBean("logEntryContextManager"));
                    assertNotNull(context.getBean(LogEntryContextManager.class));
                    assertInstanceOf(DefaultLogEntryContextManager.class,
                        context.getBean(LogEntryContextManager.class));
                });
        }

        @Test
        @DisplayName("사용자 정의 LogEntryContextManager가 있을 경우 DefaultLogEntryContextManager는 등록되지 않아야 한다")
        void shouldNotRegisterDefaultLogEntryContextManagerWhenCustomBeanExists() {
            contextRunner
                .withBean(LogEntryContextManager.class, () -> new LogEntryContextManager() {
                    @Override
                    public void addLog(LogEntry logEntry) {}

                    @Override
                    public void flush() {}

                    @Override
                    public void clear() {}
                })
                .run(context -> {
                    assertTrue(context.containsBean("logEntryContextManager"));
                    assertFalse(context.getBean(LogEntryContextManager.class) instanceof DefaultLogEntryContextManager);
                });
        }
    }

}
package com.monikit.starter.config;

import java.util.Collections;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.core.DefaultLogEntryContextManager;
import com.monikit.core.DefaultLogNotifier;
import com.monikit.core.DefaultThreadContextHandler;
import com.monikit.core.LogAddHook;
import com.monikit.core.LogEntry;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogFlushHook;
import com.monikit.core.LogNotifier;
import com.monikit.core.ThreadContextHandler;

import static org.junit.jupiter.api.Assertions.*;

class LogEntryContextManagerConfigTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogEntryContextManagerConfig.class))
            .withBean(LogNotifier.class, () -> new DefaultLogNotifier(Collections.emptyList()))
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

    @Nested
    @DisplayName("ThreadContextHandler 자동 등록 테스트")
    class ThreadContextHandlerAutoConfigurationTests {

        @Test
        @DisplayName("사용자 정의 빈이 없을 때 DefaultThreadContextHandler가 자동 등록되어야 한다")
        void shouldRegisterDefaultThreadContextHandlerWhenNoCustomBean() {
            contextRunner
                .run(context -> {
                    assertTrue(context.containsBean("threadContextHandler"));
                    assertNotNull(context.getBean(ThreadContextHandler.class));
                    assertInstanceOf(DefaultThreadContextHandler.class, context.getBean(ThreadContextHandler.class));
                });
        }

        @Test
        @DisplayName("사용자 정의 ThreadContextHandler가 있을 경우 DefaultThreadContextHandler는 등록되지 않아야 한다")
        void shouldNotRegisterDefaultThreadContextHandlerWhenCustomBeanExists() {
            contextRunner
                .withBean(ThreadContextHandler.class, () -> new ThreadContextHandler() {
                    @Override
                    public Runnable propagateToChildThread(Runnable task) {
                        return null;
                    }

                    @Override
                    public <T> Callable<T> propagateToChildThread(Callable<T> task) {
                        return null;
                    }

                    @Override
                    public <T> ThrowingCallable<T> propagateToChildThreadThrowable(ThrowingCallable<T> task) {
                        return null;
                    }

                    @Override
                    public void logException(String traceId, Throwable exception) {}
                })
                .run(context -> {
                    assertTrue(context.containsBean("threadContextHandler"));
                    assertFalse(context.getBean(ThreadContextHandler.class) instanceof DefaultThreadContextHandler);
                });
        }
    }
}
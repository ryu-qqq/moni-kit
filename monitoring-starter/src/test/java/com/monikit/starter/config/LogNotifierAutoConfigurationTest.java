package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.core.DefaultLogNotifier;
import com.monikit.core.LogNotifier;
import com.monikit.starter.DefaultSlf4jLogNotifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LogNotifierAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogNotifierAutoConfiguration.class));

    @Nested
    @DisplayName("LogNotifier 자동 등록 테스트")
    class LogNotifierAutoConfigurationTests {


        @Test
        @DisplayName("사용자가 LogNotifier 빈을 직접 등록한 경우 해당 빈을 사용해야 한다")
        void shouldUseCustomLogNotifierIfProvided() {
            contextRunner
                .withBean(LogNotifier.class, () -> mock(LogNotifier.class))
                .run(context -> {
                    LogNotifier logNotifier = context.getBean(LogNotifier.class);
                    assertNotNull(logNotifier);
                    assertFalse(logNotifier instanceof DefaultLogNotifier);
                    assertFalse(logNotifier instanceof DefaultSlf4jLogNotifier);
                });
        }

        @Test
        @DisplayName("SLF4J가 존재하는 경우 DefaultSlf4jLogNotifier가 자동 등록되어야 한다")
        void shouldRegisterDefaultSlf4jLogNotifierIfSlf4jExists() {
            contextRunner
                .run(context -> {
                    assertTrue(context.containsBean("slf4jLogNotifier"));
                    LogNotifier logNotifier = context.getBean(LogNotifier.class);
                    assertNotNull(logNotifier);
                    assertInstanceOf(DefaultSlf4jLogNotifier.class, logNotifier);
                });
        }

        @Test
        @DisplayName("SLF4J가 없고 LogNotifier도 없는 경우 DefaultLogNotifier가 자동 등록되어야 한다")
        void shouldRegisterDefaultLogNotifierIfNoSlf4jAndNoCustomBean() {
            new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LogNotifierAutoConfiguration.class))
                .withClassLoader(new NoSlf4jClassLoader()) // SLF4J 제거
                .run(context -> {
                    assertTrue(context.containsBean("defaultLogNotifier"));
                    LogNotifier logNotifier = context.getBean(LogNotifier.class);
                    assertNotNull(logNotifier);
                    assertInstanceOf(DefaultLogNotifier.class, logNotifier);
                });
        }
    }

    /**
     * SLF4J가 없는 환경을 시뮬레이션하기 위한 커스텀 ClassLoader
     */
    static class NoSlf4jClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.startsWith("org.slf4j")) {
                throw new ClassNotFoundException("Simulating missing SLF4J");
            }
            return super.loadClass(name);
        }
    }
}
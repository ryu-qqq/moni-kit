package com.monikit.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoniKitLoggingPropertiesTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @TestConfiguration
    @EnableConfigurationProperties(MoniKitLoggingProperties.class)
    static class TestConfig {}

    @Nested
    @DisplayName("MoniKitLoggingProperties 자동 바인딩 테스트")
    class MoniKitLoggingPropertiesBindingTests {

        @Test
        @DisplayName("기본 설정값이 적용되어야 한다 (logEnabled = true)")
        void shouldApplyDefaultLogEnabledValue() {
            contextRunner.run(context -> {
                MoniKitLoggingProperties properties = context.getBean(MoniKitLoggingProperties.class);
                assertNotNull(properties);
                assertTrue(properties.isLogEnabled(), "기본값(logEnabled)은 true여야 한다.");
            });
        }

        @Test
        @DisplayName("application.properties에서 설정한 값이 반영되어야 한다")
        void shouldBindPropertiesCorrectly() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=false") // ✅ kebab-case 사용
                .run(context -> {
                    MoniKitLoggingProperties properties = context.getBean(MoniKitLoggingProperties.class);
                    assertNotNull(properties);
                    assertFalse(properties.isLogEnabled(), "설정값이 false로 반영되어야 한다.");
                });
        }

        @Test
        @DisplayName("datasourceLoggingEnabled가 기본값으로 true여야 한다.")
        void shouldApplyDefaultDatasourceLoggingEnabledValue() {
            contextRunner.run(context -> {
                MoniKitLoggingProperties properties = context.getBean(MoniKitLoggingProperties.class);
                assertNotNull(properties);
                assertTrue(properties.isDatasourceLoggingEnabled(), "기본값(datasourceLoggingEnabled)은 true여야 한다.");
            });
        }

        @Test
        @DisplayName("application.properties에서 설정한 값이 반영되어야 한다 (datasourceLoggingEnabled=false)")
        void shouldBindDatasourceLoggingEnabledProperty() {
            contextRunner
                .withPropertyValues("monikit.logging.datasource-logging-enabled=false")
                .run(context -> {
                    MoniKitLoggingProperties properties = context.getBean(MoniKitLoggingProperties.class);
                    assertNotNull(properties);
                    assertFalse(properties.isDatasourceLoggingEnabled(), "설정값(datasourceLoggingEnabled)이 false로 반영되어야 한다.");
                });
        }

        @Test
        @DisplayName("slowQueryThresholdMs가 기본값 1000으로 설정되어야 한다.")
        void shouldApplyDefaultSlowQueryThresholdMsValue() {
            contextRunner.run(context -> {
                MoniKitLoggingProperties properties = context.getBean(MoniKitLoggingProperties.class);
                assertNotNull(properties);
                assertTrue(properties.getSlowQueryThresholdMs() == 1000, "기본값(slowQueryThresholdMs)은 1000이어야 한다.");
            });
        }

        @Test
        @DisplayName("application.properties에서 설정한 값이 반영되어야 한다 (slowQueryThresholdMs=2000)")
        void shouldBindSlowQueryThresholdMsProperty() {
            contextRunner
                .withPropertyValues("monikit.logging.slow-query-threshold-ms=2000")
                .run(context -> {
                    MoniKitLoggingProperties properties = context.getBean(MoniKitLoggingProperties.class);
                    assertNotNull(properties);
                    assertTrue(properties.getSlowQueryThresholdMs() == 2000, "설정값(slowQueryThresholdMs)이 2000으로 반영되어야 한다.");
                });
        }
    }
}
package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class MoniKitMetricsPropertiesTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @TestConfiguration
    @EnableConfigurationProperties(MoniKitMetricsProperties.class)
    static class TestConfig {}

    @Nested
    @DisplayName("MoniKitMetricsProperties 자동 바인딩 테스트")
    class MoniKitMetricsPropertiesBindingTests {

        @Test
        @DisplayName("기본 설정값이 적용되어야 한다 (metricsEnabled = true)")
        void shouldApplyDefaultMetricsEnabledValue() {
            contextRunner.run(context -> {
                MoniKitMetricsProperties properties = context.getBean(MoniKitMetricsProperties.class);
                assertNotNull(properties);
                assertTrue(properties.isMetricsEnabled(), "기본값(metricsEnabled)은 true여야 한다.");
            });
        }

        @Test
        @DisplayName("application.properties에서 설정한 값이 반영되어야 한다")
        void shouldBindPropertiesCorrectly() {
            contextRunner
                .withPropertyValues("monikit.metrics.metrics-enabled=false") // ✅ kebab-case 사용
                .run(context -> {
                    MoniKitMetricsProperties properties = context.getBean(MoniKitMetricsProperties.class);
                    assertNotNull(properties);
                    assertFalse(properties.isMetricsEnabled(), "설정값이 false로 반영되어야 한다.");
                });
        }
    }
}
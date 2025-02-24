package com.monikit.starter.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.core.MetricCollector;
import com.monikit.starter.MoniKitMetricCollector;
import com.monikit.starter.NoOpMetricCollector;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@DisplayName("MetricCollectorAutoConfiguration 테스트")
class MetricCollectorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class));

    @Nested
    @DisplayName("monikit.metrics.metricsEnabled=true일 때")
    class WhenMetricsEnabled {

        @Test
        @DisplayName("MeterRegistry가 존재하면 MoniKitMetricCollector가 등록되어야 한다.")
        void shouldRegisterMoniKitMetricCollectorWhenMeterRegistryExists() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .withBean(MeterRegistry.class, () -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)) // MeterRegistry 등록
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(MoniKitMetricCollector.class);
                });
        }

        @Test
        @DisplayName("MeterRegistry가 없으면 NoOpMetricCollector가 등록되어야 한다.")
        void shouldRegisterNoOpMetricCollectorWhenMeterRegistryIsMissing() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(NoOpMetricCollector.class);
                });
        }
    }

    @Nested
    @DisplayName("monikit.metrics.metricsEnabled=false일 때")
    class WhenMetricsDisabled {

        @Test
        @DisplayName("무조건 NoOpMetricCollector가 등록되어야 한다.")
        void shouldRegisterNoOpMetricCollector() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(NoOpMetricCollector.class);
                });
        }
    }

    @Test
    @DisplayName("MetricCollector 빈이 단 하나만 등록되는지 검증")
    void shouldRegisterOnlyOneMetricCollector() {
        contextRunner
            .withPropertyValues("monikit.metrics.metricsEnabled=true")
            .withBean(MeterRegistry.class, () -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT))
            .run(context -> {
                assertThat(context).hasSingleBean(MetricCollector.class);
            });
    }
}

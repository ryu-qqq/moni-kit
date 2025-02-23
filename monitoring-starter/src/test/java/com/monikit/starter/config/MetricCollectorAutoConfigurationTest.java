package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.core.MetricCollector;
import com.monikit.starter.MoniKitMetricCollector;
import com.monikit.starter.NoOpMetricCollector;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@DisplayName("MetricCollectorAutoConfiguration 테스트")
class MetricCollectorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class))
        .withBean(MeterRegistry.class, () -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));

    @Nested
    @DisplayName("MoniKitMetricCollector 등록 테스트")
    class MoniKitMetricCollectorTests {

        @Test
        @DisplayName("metricsEnabled=true일 때 MoniKitMetricCollector가 등록되어야 한다.")
        void shouldRegisterMoniKitMetricCollectorWhenEnabled() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(MoniKitMetricCollector.class);
                });
        }

        @Test
        @DisplayName("사용자가 직접 MeterRegistry를 등록해도 MoniKitMetricCollector가 등록되어야 한다.")
        void shouldUseUserDefinedMeterRegistry() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(MoniKitMetricCollector.class);
                });
        }
    }

    @Nested
    @DisplayName("NoOpMetricCollector 등록 테스트")
    class NoOpMetricCollectorTests {

        @Test
        @DisplayName("metricsEnabled=false일 때 NoOpMetricCollector가 기본값으로 등록되어야 한다.")
        void shouldRegisterNoOpMetricCollectorWhenDisabled() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(NoOpMetricCollector.class);
                });
        }
    }
}
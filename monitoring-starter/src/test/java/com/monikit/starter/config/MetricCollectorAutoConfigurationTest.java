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
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class));

    @Nested
    @DisplayName("MoniKitMetricCollector 등록 테스트")
    class MoniKitMetricCollectorTests {

        @Test
        @DisplayName("metricsEnabled=true && MeterRegistry가 존재할 때 MoniKitMetricCollector가 등록되어야 한다.")
        void shouldRegisterMoniKitMetricCollectorWhenEnabledAndMeterRegistryExists() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .withBean(MeterRegistry.class, () -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT))
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

    @Nested
    @DisplayName("SmartInitializingSingleton 검증 테스트")
    class SmartInitializingSingletonTests {

        @Test
        @DisplayName("모든 빈이 초기화된 후 MetricCollector가 정상적으로 등록되었는지 검증해야 한다.")
        void shouldEnsureMetricCollectorIsInitializedAfterAllBeans() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .withBean(MeterRegistry.class, () -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT))
                .run(context -> {
                    MetricCollector metricCollector = context.getBean(MetricCollector.class);
                    assertThat(metricCollector).isInstanceOf(MoniKitMetricCollector.class);
                });
        }
    }
}

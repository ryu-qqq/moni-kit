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
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
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
        @DisplayName("metricsEnabled=true일 때 MoniKitMetricCollector가 등록되어야 한다.")
        void shouldRegisterMoniKitMetricCollectorWhenEnabled() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .withBean(MeterRegistry.class, () -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)) // PrometheusMeterRegistry 빈 등록
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(MoniKitMetricCollector.class);
                });
        }

        @Test
        @DisplayName("사용자가 직접 CompositeMeterRegistry를 제공하면 기존 레지스트리가 유지되면서 MoniKitMetricCollector가 등록되어야 한다.")
        void shouldUseCompositeMeterRegistry() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .withBean(MeterRegistry.class, () -> {
                    CompositeMeterRegistry compositeRegistry = new CompositeMeterRegistry();
                    compositeRegistry.add(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)); // 기본 MeterRegistry 추가
                    return compositeRegistry;
                })
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(MoniKitMetricCollector.class);

                    MeterRegistry meterRegistry = context.getBean(MeterRegistry.class);
                    assertThat(meterRegistry).isInstanceOf(CompositeMeterRegistry.class);

                    CompositeMeterRegistry compositeMeterRegistry = (CompositeMeterRegistry) meterRegistry;
                    assertThat(compositeMeterRegistry.getRegistries()).isNotEmpty();
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
                .withBean(MeterRegistry.class, () -> new PrometheusMeterRegistry(PrometheusConfig.DEFAULT))
                .run(context -> {
                    assertThat(context).hasSingleBean(MetricCollector.class);
                    assertThat(context.getBean(MetricCollector.class)).isInstanceOf(NoOpMetricCollector.class);
                });
        }

        @Test
        @DisplayName("metricsEnabled=true이지만 MeterRegistry가 없으면 MoniKitMetricCollector가 등록되지 않는다.")
        void shouldNotRegisterMoniKitMetricCollectorWithoutMeterRegistry() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MoniKitMetricCollector.class);
                    assertThat(context).doesNotHaveBean(MetricCollector.class); // NoOpMetricCollector도 등록되지 않음
                });
        }
    }
}

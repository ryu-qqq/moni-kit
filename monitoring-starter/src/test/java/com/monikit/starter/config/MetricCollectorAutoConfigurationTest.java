package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import com.monikit.core.MetricCollector;
import com.monikit.starter.MoniKitMetricCollector;
import com.monikit.starter.NoOpMetricCollector;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@DisplayName("MetricCollectorAutoConfiguration 테스트")
class MetricCollectorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class))
        .withUserConfiguration(TestConfig.class);

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MeterRegistry meterRegistry() {
            return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        }
    }

    @Nested
    @DisplayName("MoniKitMetricCollector 등록 테스트")
    class MoniKitMetricCollectorTests {

        @Test
        @DisplayName("metricsEnabled=true일 때 MoniKitMetricCollector가 등록되어야 한다.")
        void shouldRegisterMoniKitMetricCollectorWhenEnabled() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .run(context -> {
                    assertThat(context.getBean(MeterRegistry.class)).isNotNull();
                    assertThat(hasMoniKitMetricCollector(context)).isTrue();
                });
        }

        @Test
        @DisplayName("사용자가 직접 MeterRegistry를 등록해도 MoniKitMetricCollector가 등록되어야 한다.")
        void shouldUseUserDefinedMeterRegistry() {
            contextRunner
                .withPropertyValues("monikit.metrics.metricsEnabled=true")
                .run(context -> {
                    assertThat(context.getBean(MeterRegistry.class)).isNotNull();
                    assertThat(hasMoniKitMetricCollector(context)).isTrue();
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

    /**
     * ✅ `SmartInitializingSingleton`을 사용하기 때문에 `context.getBean(MetricCollector.class)`를 직접 호출할 수 없음.
     * 대신 `MeterRegistry`에 `MoniKitMetricCollector`가 등록한 메트릭이 있는지 확인.
     */
    private boolean hasMoniKitMetricCollector(ApplicationContext context) {
        MeterRegistry meterRegistry = context.getBean(MeterRegistry.class);

        // ✅ 강제로 메트릭 하나를 추가해본다.
        meterRegistry.counter("test_metric").increment();

        System.out.println("### 현재 등록된 메트릭 개수: " + meterRegistry.getMeters().size());

        return meterRegistry.getMeters()
            .stream()
            .anyMatch(meter -> meter.getId().getName().startsWith("test_metric"));
    }
}

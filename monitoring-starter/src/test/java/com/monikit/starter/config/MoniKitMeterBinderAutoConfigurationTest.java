package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.starter.DatabaseQueryMetricCollector;
import com.monikit.starter.HttpInboundResponseMetricCollector;
import com.monikit.starter.HttpOutboundResponseMetricCollector;
import com.monikit.starter.HttpResponseMetricsRecorder;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class MoniKitMeterBinderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withAllowBeanDefinitionOverriding(true)
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class))
        .withUserConfiguration(MockBeanConfig.class) // ✅ 빈 설정 추가
        .withPropertyValues(
            "monikit.metrics.queryMetricsEnabled=true",
            "monikit.metrics.httpMetricsEnabled=true"
        );

    @Test
    @DisplayName("shouldRegisterDatabaseQueryMetricCollectorWhenEnabled")
    void shouldRegisterDatabaseQueryMetricCollectorWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DatabaseQueryMetricCollector.class);
            assertThat(context.getBean(DatabaseQueryMetricCollector.class)).isNotNull();
        });
    }

    @Test
    @DisplayName("shouldRegisterHttpInboundResponseMetricCollectorWhenEnabled")
    void shouldRegisterHttpInboundResponseMetricCollectorWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(HttpInboundResponseMetricCollector.class);
            assertThat(context.getBean(HttpInboundResponseMetricCollector.class)).isNotNull();
        });
    }

    @Test
    @DisplayName("shouldRegisterHttpOutboundResponseMetricCollectorWhenEnabled")
    void shouldRegisterHttpOutboundResponseMetricCollectorWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(HttpOutboundResponseMetricCollector.class);
            assertThat(context.getBean(HttpOutboundResponseMetricCollector.class)).isNotNull();
        });
    }

    @Test
    @DisplayName("shouldRegisterHttpResponseMetricsRecorderWhenEnabled")
    void shouldRegisterHttpResponseMetricsRecorderWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(HttpResponseMetricsRecorder.class);
            assertThat(context.getBean(HttpResponseMetricsRecorder.class)).isNotNull();
        });
    }

}
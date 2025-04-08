package com.monikit.metric.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.metric.DatabaseQueryMetricCollector;
import com.monikit.metric.HttpInboundResponseMetricCollector;
import com.monikit.metric.HttpOutboundResponseMetricCollector;
import com.monikit.metric.HttpResponseMetricsRecorder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MoniKitMeterBinderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withAllowBeanDefinitionOverriding(true)
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class))
        .withUserConfiguration(MetricMockBeanConfig.class)
        .withPropertyValues(
            "monikit.metrics.queryMetricsEnabled=true",
            "monikit.metrics.httpMetricsEnabled=true"
        );

    @Test
    @DisplayName("shouldRegisterDatabaseQueryMetricCollectorWhenEnabled")
    void shouldRegisterDatabaseQueryMetricCollectorWhenEnabled() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(DatabaseQueryMetricCollector.class));
        });
    }

    @Test
    @DisplayName("shouldRegisterHttpInboundResponseMetricCollectorWhenEnabled")
    void shouldRegisterHttpInboundResponseMetricCollectorWhenEnabled() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(HttpInboundResponseMetricCollector.class));
        });
    }

    @Test
    @DisplayName("shouldRegisterHttpOutboundResponseMetricCollectorWhenEnabled")
    void shouldRegisterHttpOutboundResponseMetricCollectorWhenEnabled() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(HttpOutboundResponseMetricCollector.class));
        });
    }

    @Test
    @DisplayName("shouldRegisterHttpResponseMetricsRecorderWhenEnabled")
    void shouldRegisterHttpResponseMetricsRecorderWhenEnabled() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(HttpResponseMetricsRecorder.class));
        });
    }

}
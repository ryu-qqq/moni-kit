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
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricCollectorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class))
        .withUserConfiguration(MetricMockBeanConfig.class);

    @Test
    @DisplayName("shouldRegisterDatabaseQueryMetricCollectorWhenEnabled")
    void shouldRegisterDatabaseQueryMetricCollectorWhenEnabled() {
        contextRunner
            .withPropertyValues("monikit.metrics.queryMetricsEnabled=true")
            .run(context -> {
                assertTrue(context.containsBean("databaseQueryMetricCollector"));
                assertNotNull(context.getBean(DatabaseQueryMetricCollector.class));
            });
    }

    @Test
    @DisplayName("shouldRegisterHttpInboundResponseMetricCollectorWhenEnabled")
    void shouldRegisterHttpInboundResponseMetricCollectorWhenEnabled() {
        contextRunner
            .withPropertyValues("monikit.metrics.httpMetricsEnabled=true")
            .run(context -> {
                assertTrue(context.containsBean("httpInboundResponseMetricCollector"));
                assertNotNull(context.getBean(HttpInboundResponseMetricCollector.class));
            });
    }

    @Test
    @DisplayName("shouldRegisterHttpOutboundResponseMetricCollectorWhenEnabled")
    void shouldRegisterHttpOutboundResponseMetricCollectorWhenEnabled() {
        contextRunner
            .withPropertyValues("monikit.metrics.httpMetricsEnabled=true")
            .run(context -> {
                assertTrue(context.containsBean("httpOutboundResponseMetricCollector"));
                assertNotNull(context.getBean(HttpOutboundResponseMetricCollector.class));
            });
    }

    @Test
    @DisplayName("shouldRegisterHttpResponseMetricsRecorderWhenEnabled")
    void shouldRegisterHttpResponseMetricsRecorderWhenEnabled() {
        contextRunner
            .withPropertyValues("monikit.metrics.httpMetricsEnabled=true")
            .run(context -> {
                assertNotNull(context.getBean(HttpResponseMetricsRecorder.class));
            });
    }


    @Test
    @DisplayName("shouldNotRegisterDuplicateBeans")
    void shouldNotRegisterDuplicateBeans() {
        contextRunner
            .withPropertyValues(
                "monikit.metrics.queryMetricsEnabled=true",
                "monikit.metrics.httpMetricsEnabled=true"
            )
            .run(context -> {
                assertNotNull(context.getBean(DatabaseQueryMetricCollector.class));
                assertNotNull(context.getBean(HttpInboundResponseMetricCollector.class));
                assertNotNull(context.getBean(HttpOutboundResponseMetricCollector.class));
                assertNotNull(context.getBean(HttpResponseMetricsRecorder.class));
            });
    }
}

package com.monikit.starter.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.starter.*;

class MetricCollectorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class))
        .withUserConfiguration(MockBeanConfig.class);

    @Test
    @DisplayName("shouldRegisterDatabaseQueryMetricCollectorWhenEnabled")
    void shouldRegisterDatabaseQueryMetricCollectorWhenEnabled() {
        contextRunner
            .withPropertyValues("monikit.metrics.queryMetricsEnabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(DatabaseQueryMetricCollector.class);
                assertThat(context.getBean(DatabaseQueryMetricCollector.class)).isNotNull();
            });
    }

    @Test
    @DisplayName("shouldRegisterHttpInboundResponseMetricCollectorWhenEnabled")
    void shouldRegisterHttpInboundResponseMetricCollectorWhenEnabled() {
        contextRunner
            .withPropertyValues("monikit.metrics.httpMetricsEnabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(HttpInboundResponseMetricCollector.class);
                assertThat(context.getBean(HttpInboundResponseMetricCollector.class)).isNotNull();
            });
    }

    @Test
    @DisplayName("shouldRegisterHttpOutboundResponseMetricCollectorWhenEnabled")
    void shouldRegisterHttpOutboundResponseMetricCollectorWhenEnabled() {
        contextRunner
            .withPropertyValues("monikit.metrics.httpMetricsEnabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(HttpOutboundResponseMetricCollector.class);
                assertThat(context.getBean(HttpOutboundResponseMetricCollector.class)).isNotNull();
            });
    }

    @Test
    @DisplayName("shouldRegisterHttpResponseMetricsRecorderWhenEnabled")
    void shouldRegisterHttpResponseMetricsRecorderWhenEnabled() {
        contextRunner
            .withPropertyValues("monikit.metrics.httpMetricsEnabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(HttpResponseMetricsRecorder.class);
                assertThat(context.getBean(HttpResponseMetricsRecorder.class)).isNotNull();
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
                assertThat(context).hasSingleBean(HttpResponseMetricsRecorder.class);
                assertThat(context).hasSingleBean(HttpInboundResponseMetricCollector.class);
                assertThat(context).hasSingleBean(HttpOutboundResponseMetricCollector.class);
                assertThat(context).hasSingleBean(DatabaseQueryMetricCollector.class);
            });
    }
}

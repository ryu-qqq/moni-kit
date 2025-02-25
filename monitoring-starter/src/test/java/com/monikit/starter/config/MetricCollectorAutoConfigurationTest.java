package com.monikit.starter.config;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.monikit.core.MetricCollector;
import com.monikit.starter.DatabaseQueryMetricCollector;
import com.monikit.starter.HttpInboundResponseMetricCollector;
import com.monikit.starter.HttpOutboundResponseMetricCollector;
import com.monikit.starter.HttpResponseMetricsRecorder;

class MetricCollectorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withAllowBeanDefinitionOverriding(true)
        .withConfiguration(AutoConfigurations.of(MetricCollectorAutoConfiguration.class))
        .withUserConfiguration(MockTestConfiguration.class)
        .withPropertyValues(
            "monikit.metrics.query.enabled=true",
            "monikit.metrics.httpMetricsEnabled=true",
            "monikit.metrics.externalMallMetricsEnabled=true"
        );

    @Nested
    @DisplayName("SQL Query Metrics 빈 등록 테스트")
    class SqlQueryMetrics {

        @Test
        @DisplayName("should register sqlQueryCounter bean successfully")
        void shouldRegisterSqlQueryCounterBeanSuccessfully() {
            contextRunner.run(context -> {
                Counter counter = context.getBean("sqlQueryCounter", Counter.class);
                assertNotNull(counter);
            });
        }

        @Test
        @DisplayName("should register sqlQueryTimer bean successfully")
        void shouldRegisterSqlQueryTimerBeanSuccessfully() {
            contextRunner.run(context -> {
                Timer timer = context.getBean("sqlQueryTimer", Timer.class);
                assertNotNull(timer);
            });
        }

        @Test
        @DisplayName("should register DatabaseQueryMetricCollector bean successfully")
        void shouldRegisterDatabaseQueryMetricCollectorBeanSuccessfully() {
            contextRunner.run(context -> {
                MetricCollector<?> collector = context.getBean(DatabaseQueryMetricCollector.class);
                assertNotNull(collector);
                assertInstanceOf(DatabaseQueryMetricCollector.class, collector);
            });
        }
    }

    @Nested
    @DisplayName("HTTP Inbound Response Metrics 빈 등록 테스트")
    class HttpInboundResponseMetrics {


        @Test
        @DisplayName("should register HttpInboundResponseMetricCollector bean successfully")
        void shouldRegisterHttpInboundResponseMetricCollectorBeanSuccessfully() {
            contextRunner.run(context -> {
                MetricCollector<?> collector = context.getBean(HttpInboundResponseMetricCollector.class);
                assertNotNull(collector);
                assertInstanceOf(HttpInboundResponseMetricCollector.class, collector);
            });
        }
    }

    @Nested
    @DisplayName("HTTP Outbound Response Metrics 빈 등록 테스트")
    class HttpOutboundResponseMetrics {


        @Test
        @DisplayName("should register HttpOutboundResponseMetricCollector bean successfully")
        void shouldRegisterHttpOutboundResponseMetricCollectorBeanSuccessfully() {
            contextRunner.run(context -> {
                MetricCollector<?> collector = context.getBean(HttpOutboundResponseMetricCollector.class);
                assertNotNull(collector);
                assertInstanceOf(HttpOutboundResponseMetricCollector.class, collector);
            });
        }
    }

    /**
     * 테스트 전용 모의 빈 설정
     */
    /**
     * 테스트 전용 모의 빈 설정
     */
    @TestConfiguration
    static class MockTestConfiguration {

        @Bean
        public MeterRegistry meterRegistry() {
            return new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        }

        @Bean
        public MoniKitMetricsProperties moniKitMetricsProperties() {
            return new MoniKitMetricsProperties();
        }

        @Bean
        public HttpResponseMetricsRecorder responseStatusCounter() {
            return new HttpResponseMetricsRecorder(meterRegistry());
        }
    }
}

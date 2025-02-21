package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.core.MetricCollector;
import com.monikit.starter.NoOpMetricCollector;
import com.monikit.starter.PrometheusMetricCollector;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * 사용자가 원하는 `MetricCollector`를 주입할 수 있도록 자동 설정하는 클래스.
 * <p>
 * - 사용자가 직접 `MetricCollector`를 구현하면 그것을 우선 사용.
 * - Micrometer가 존재하고, 별도 설정이 없으면 `PrometheusMetricCollector`를 기본값으로 사용.
 * - `monikit.metrics.enabled=false` 설정 시, `NoOpMetricCollector`를 사용하여 메트릭을 비활성화 가능.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class MetricCollectorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectorAutoConfiguration.class);
    private final MoniKitMetricsProperties properties;

    public MetricCollectorAutoConfiguration(MoniKitMetricsProperties properties) {
        this.properties = properties;
    }

    /**
     * `MetricCollector`의 기본 빈을 등록
     * - 사용자가 직접 `MetricCollector`를 구현하면 그것을 사용.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(MetricCollector.class)
    public MetricCollector metricCollector(MeterRegistry meterRegistry) {
        if (!properties.isMetricsEnabled()) {
            logger.info("Metrics collection is disabled. Using NoOpMetricCollector.");
            return new NoOpMetricCollector();
        }
        logger.info("Metrics collection is enabled. Using PrometheusMetricCollector.");
        return new PrometheusMetricCollector(meterRegistry);
    }

    /**
     * Micrometer(Prometheus) 기반 `PrometheusMetricCollector` 등록
     * - `MeterRegistry`가 존재할 때만 자동 등록.
     * - `MetricCollector`가 이미 존재하면 등록되지 않음.
     */
    @Bean
    @ConditionalOnMissingBean(MetricCollector.class)
    @ConditionalOnClass(MeterRegistry.class)
    public MetricCollector prometheusMetricCollector(MeterRegistry meterRegistry) {
        logger.info("Prometheus detected, registering PrometheusMetricCollector.");
        return new PrometheusMetricCollector(meterRegistry);
    }

    /**
     * 메트릭이 비활성화된 경우 `NoOpMetricCollector`를 기본값으로 등록
     */
    @Bean
    @ConditionalOnMissingBean(MetricCollector.class)
    @ConditionalOnProperty(name = "monikit.metrics.enabled", havingValue = "false", matchIfMissing = false)
    public MetricCollector noOpMetricCollector() {
        logger.info("Metrics are disabled via configuration, using NoOpMetricCollector.");
        return new NoOpMetricCollector();
    }
}
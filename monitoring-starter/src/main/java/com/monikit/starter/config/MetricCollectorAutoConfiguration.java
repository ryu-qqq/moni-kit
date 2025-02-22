package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.core.MetricCollector;
import com.monikit.starter.NoOpMetricCollector;
import com.monikit.starter.PrometheusMetricCollector;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

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

    /**
     * PrometheusMetricCollector 등록
     * - Micrometer(Prometheus)가 존재하고, `monikit.metrics.metricsEnabled=true`일 때만 등록.
     * - `MetricCollector`가 이미 존재하면 `CompositeMetricCollector`를 통해 여러 개를 관리.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(PrometheusMeterRegistry.class)
    public MetricCollector prometheusMetricCollector(PrometheusMeterRegistry meterRegistry) {
        logger.info("Prometheus detected, registering PrometheusMetricCollector.");
        return new PrometheusMetricCollector(meterRegistry);
    }

    /**
     * 메트릭이 비활성화된 경우 `NoOpMetricCollector`를 기본값으로 등록
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "false", matchIfMissing = false)
    public MetricCollector noOpMetricCollector() {
        logger.info("Metrics are disabled via configuration, using NoOpMetricCollector.");
        return new NoOpMetricCollector();
    }
}
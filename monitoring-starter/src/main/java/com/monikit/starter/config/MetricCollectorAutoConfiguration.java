package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.core.MetricCollector;
import com.monikit.core.MetricCollectorProvider;
import com.monikit.starter.NoOpMetricCollector;
import com.monikit.starter.PrometheusMetricCollector;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * 사용자가 원하는 `MetricCollector`를 주입할 수 있도록 자동 설정하는 클래스.
 * <p>
 * - `PrometheusMetricCollector`를 기본 제공하지만, 사용자가 직접 `MetricCollector`를 등록할 수도 있음.
 * - `monikit.metrics.enabled=false` 설정 시, `NoOpMetricCollector`를 사용하여 에러 방지.
 * </p>
 */
@Configuration
public class MetricCollectorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectorAutoConfiguration.class);
    private final MoniKitMetricsProperties properties;

    public MetricCollectorAutoConfiguration(MoniKitMetricsProperties properties) {
        this.properties = properties;
    }


    @Bean
    @Primary
    public MetricCollector metricCollector(MeterRegistry meterRegistry) {
        MetricCollector metricCollector;

        if (!properties.isEnabled()) {
            logger.info("Metrics collection is disabled. Using NoOpMetricCollector.");
            metricCollector = new NoOpMetricCollector();
        } else {
            logger.info("Metrics collection is enabled. Using PrometheusMetricCollector.");
            metricCollector = new PrometheusMetricCollector(meterRegistry);
        }

        MetricCollectorProvider.setMetricCollector(metricCollector);

        return metricCollector;
    }
}

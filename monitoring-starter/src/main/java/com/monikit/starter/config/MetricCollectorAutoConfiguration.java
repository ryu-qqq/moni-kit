package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.core.MetricCollector;
import com.monikit.starter.MoniKitMetricCollector;
import com.monikit.starter.NoOpMetricCollector;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * 사용자가 원하는 `MetricCollector`를 주입할 수 있도록 자동 설정하는 클래스.
 * <p>
 * - `monikit.metrics.metricsEnabled=true`이면 무조건 `MoniKitMetricCollector`가 등록됨.
 * - 사용자가 직접 `MeterRegistry`를 정의했더라도 이를 활용하여 `MoniKitMetricCollector`가 빈으로 등록됨.
 * - `monikit.metrics.metricsEnabled=false`이면 `NoOpMetricCollector`를 기본값으로 사용.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1
 */
@Configuration
public class MetricCollectorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectorAutoConfiguration.class);

    /**
     * `metricsEnabled=true`이면 `MoniKitMetricCollector`가 무조건 등록됨.
     * 사용자가 `MeterRegistry`를 등록했든 안 했든 관계없이 동작.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    public MetricCollector moniKitMetricCollector(MeterRegistry meterRegistry) {
        logger.info("Metrics are enabled. Registering MoniKitMetricCollector with provided MeterRegistry.");
        return new MoniKitMetricCollector(meterRegistry);
    }

    /**
     * `metricsEnabled=false`이면 `NoOpMetricCollector`를 기본값으로 사용.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "false", matchIfMissing = false)
    public MetricCollector noOpMetricCollector() {
        logger.info("Metrics are disabled via configuration, using NoOpMetricCollector.");
        return new NoOpMetricCollector();
    }
}
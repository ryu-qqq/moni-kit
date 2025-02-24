package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import com.monikit.core.MetricCollector;
import com.monikit.starter.MoniKitMetricCollector;
import com.monikit.starter.NoOpMetricCollector;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

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
     * MeterRegistry가 존재하면 이를 활용하고, 없으면 NoOpMetricCollector 사용.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "true", matchIfMissing = true)
    public MetricCollector moniKitMetricCollector(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        return new MoniKitMetricCollector(meterRegistryProvider.getIfAvailable());
    }

    /**
     * `metricsEnabled=false`이면 `NoOpMetricCollector`를 기본값으로 사용.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.metrics.metricsEnabled", havingValue = "false", matchIfMissing = false)
    @Primary
    public MetricCollector noOpMetricCollector() {
        logger.info("Metrics are disabled via configuration, using NoOpMetricCollector.");
        return new NoOpMetricCollector();
    }

    /**
     * 모든 빈이 초기화된 후 `MetricCollector`가 정상적으로 초기화되었는지 확인하는 후처리 로직.
     */
    @Bean
    public SmartInitializingSingleton initializeMetricsCollector(ObjectProvider<MetricCollector> metricCollectorProvider) {
        return () -> {
            MetricCollector metricCollector = metricCollectorProvider.getIfAvailable();
            if (metricCollector != null) {
                logger.info("SmartInitializingSingleton: MetricCollector [{}] initialized", metricCollector.getClass().getSimpleName());
            } else {
                logger.warn("SmartInitializingSingleton: No MetricCollector registered!");
            }
        };
    }
}
package com.monikit.starter.config;

import io.micrometer.core.instrument.MeterRegistry;

import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.monikit.core.MetricCollector;
import com.monikit.starter.NoOpMetricCollector;
import com.monikit.starter.PrometheusMetricCollector;

/**
 * 사용자가 원하는 `MetricCollector`를 주입할 수 있도록 자동 설정하는 클래스.
 * <p>
 * - `PrometheusMetricCollector`를 기본 제공하지만, 사용자가 직접 `MetricCollector`를 등록할 수도 있음.
 * - `monikit.metrics.enabled=false` 설정 시, `NoOpMetricCollector`를 사용하여 에러 방지.
 * </p>
 */
@AutoConfiguration
public class MetricCollectorAutoConfiguration {

    private final MoniKitMetricsProperties properties;

    public MetricCollectorAutoConfiguration(MoniKitMetricsProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(MetricCollector.class)
    public MetricCollector metricCollector(MeterRegistry meterRegistry) {
        if (!properties.isEnabled()) {
            return new NoOpMetricCollector(); // 메트릭 수집 비활성화
        }
        return new PrometheusMetricCollector(meterRegistry);
    }
}

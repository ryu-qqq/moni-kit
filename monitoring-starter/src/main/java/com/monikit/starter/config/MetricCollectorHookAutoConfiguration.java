package com.monikit.starter.config;

import jakarta.annotation.Nullable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.hook.LogAddHook;
import com.monikit.core.model.LogEntry;
import com.monikit.core.hook.MetricCollector;
import com.monikit.core.hook.MetricCollectorCustomizer;
import com.monikit.core.hook.MetricCollectorLogAddHook;

/**
 * MetricCollectorLogAddHook을 자동으로 등록하는 설정 클래스.
 * <p>
 * - 설정값 <code>monikit.metrics.metrics-enabled=true</code>일 때만 활성화됩니다.
 * - 사용자가 직접 등록하지 않은 경우, 기본 Hook으로 등록되어 메트릭 수집이 가능하도록 합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@Configuration
@AutoConfigureAfter({
    com.monikit.metric.config.MetricCollectorAutoConfiguration.class
})
public class MetricCollectorHookAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectorHookAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(MetricCollectorLogAddHook.class)
    @ConditionalOnProperty(
        prefix = "monikit.metrics",
        name = "metrics-enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public LogAddHook metricCollectorLogAddHook(List<MetricCollector<? extends LogEntry>> collectors,
                                                @Nullable List<MetricCollectorCustomizer> customizers
                                                ) {
        if (customizers != null) {
            for (MetricCollectorCustomizer customizer : customizers) {
                customizer.customize(collectors);
            }
        }

        logger.info("[MoniKit] Registering MetricCollectorLogAddHook with {} collector(s)", collectors.size());
        collectors.forEach(c -> logger.info(" - Registered MetricCollector: {}", c.getClass().getSimpleName()));
        return new MetricCollectorLogAddHook(collectors);
    }

}
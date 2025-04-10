package com.monikit.starter.config;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.monikit.config.MoniKitMetricsProperties;

/**
 * MoniKit의 메트릭 설정을 자동 등록하는 AutoConfiguration 클래스.
 * <p>
 * application.yml에 정의된 <code>monikit.metrics.*</code> 설정을 자동으로 바인딩합니다.
 * </p>
 *
 * @author ryu
 * @since 1.1.0
 */

@AutoConfiguration
@EnableConfigurationProperties(MoniKitMetricsProperties.class)
public class MoniKitMetricsAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MoniKitMetricsAutoConfiguration.class);
    private final MoniKitMetricsProperties props;

    public MoniKitMetricsAutoConfiguration(MoniKitMetricsProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void logConfiguration() {
        logger.info("[MoniKit] Metrics Configuration Loaded:");
        logger.info(" - metricsEnabled: {}", props.isMetricsEnabled());
        logger.info(" - queryMetricsEnabled: {}", props.isQueryMetricsEnabled());
        logger.info(" - httpMetricsEnabled: {}", props.isHttpMetricsEnabled());
        logger.info(" - slowQueryThresholdMs: {}", props.getSlowQueryThresholdMs());
        logger.info(" - querySamplingRate: {}%", props.getQuerySamplingRate());
    }

}
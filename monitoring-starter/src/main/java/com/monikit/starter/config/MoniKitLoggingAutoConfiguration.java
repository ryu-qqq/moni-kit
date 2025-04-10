package com.monikit.starter.config;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.monikit.config.MoniKitLoggingProperties;

/**
 * MoniKit 로깅 설정을 자동 등록하는 AutoConfiguration.
 * <p>
 * monikit.logging.* 설정을 자동으로 {@link MoniKitLoggingProperties}에 바인딩하고,
 * 필요한 경우 로깅 관련 로직을 확장할 수 있습니다.
 * </p>
 *
 * @author ryu
 * @since 1.1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(MoniKitLoggingProperties.class)
public class MoniKitLoggingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MoniKitLoggingAutoConfiguration.class);
    private final MoniKitLoggingProperties props;

    public MoniKitLoggingAutoConfiguration(MoniKitLoggingProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void logConfiguration() {
        logger.info("[MoniKit] Logging Configuration Loaded:");
        logger.info(" - logEnabled: {}", props.isLogEnabled());
        logger.info(" - traceEnabled: {}", props.isTraceEnabled());
        logger.info(" - detailedLogging: {}", props.isDetailedLogging());
        logger.info(" - summaryLogging: {}", props.isSummaryLogging());
        logger.info(" - thresholdMillis: {}", props.getThresholdMillis());
        logger.info(" - datasourceLoggingEnabled: {}", props.isDatasourceLoggingEnabled());
        logger.info(" - slowQueryThresholdMs: {}", props.getSlowQueryThresholdMs());
        logger.info(" - criticalQueryThresholdMs: {}", props.getCriticalQueryThresholdMs());

        if (!props.isLogEnabled() &&
            (props.isTraceEnabled() || props.isDetailedLogging() || props.isDatasourceLoggingEnabled())) {
            logger.warn("[MoniKit] logEnabled is disabled (false), but some logging settings (datasourceLoggingEnabled, traceEnabled, detailedLogging) are enabled. Logging may not be recorded.");
        }
    }

}

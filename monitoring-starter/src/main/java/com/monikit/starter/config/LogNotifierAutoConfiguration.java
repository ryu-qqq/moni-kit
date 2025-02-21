package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.core.DefaultLogNotifier;
import com.monikit.core.LogNotifier;
import com.monikit.starter.LogbackLogNotifier;

/**
 * `LogNotifier`의 구현체를 자동으로 주입하는 설정 클래스.
 * <p>
 * - 사용자가 `LogNotifier` 빈을 등록하면 이를 사용함.
 * - Logback이 있을 경우 `LogbackLogNotifier`를 기본값으로 사용.
 * - Logback이 없을 경우 `DefaultLogNotifier`를 사용.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class LogNotifierAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LogNotifierAutoConfiguration.class);

    /**
     * Logback이 존재할 경우 `LogbackLogNotifier`를 기본값으로 사용.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(LogNotifier.class)
    @ConditionalOnClass(name = "ch.qos.logback.classic.Logger")
    public LogNotifier logbackLogNotifier() {
        logger.info("Using LogbackLogNotifier as the LogNotifier implementation.");
        return new LogbackLogNotifier();
    }

    /**
     * Logback이 없을 경우 `DefaultLogNotifier`를 기본값으로 사용.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(LogNotifier.class)
    @ConditionalOnMissingClass("ch.qos.logback.classic.Logger")
    public LogNotifier defaultLogNotifier() {
        logger.info("Using DefaultLogNotifier as the LogNotifier implementation.");
        return DefaultLogNotifier.getInstance();
    }
}
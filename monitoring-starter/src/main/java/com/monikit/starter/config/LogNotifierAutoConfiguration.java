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
import com.monikit.starter.DefaultSlf4jLogNotifier;

/**
 * `LogNotifier`의 구현체를 자동으로 주입하는 설정 클래스.
 * <p>
 * - 사용자가 `LogNotifier` 빈을 등록하면 이를 우선 사용.
 * - `slf4j`가 있으면 `DefaultSlf4jLogNotifier`를 기본값으로 사용.
 * - `slf4j`도 없고 `LogNotifier`도 없으면 `DefaultLogNotifier`를 기본값으로 사용.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class LogNotifierAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LogNotifierAutoConfiguration.class);

    /**
     * `slf4j`가 존재할 경우 `DefaultSlf4jLogNotifier`를 기본값으로 설정.
     */
    @Bean
    @Primary
    @ConditionalOnClass(name = "org.slf4j.Logger")
    @ConditionalOnMissingBean(LogNotifier.class)
    public LogNotifier slf4jLogNotifier() {
        logger.info("SLF4J detected. Using DefaultSlf4jLogNotifier as the LogNotifier implementation.");
        return new DefaultSlf4jLogNotifier();
    }

    /**
     * `slf4j`가 없고, `LogNotifier`도 없을 경우 `DefaultLogNotifier`를 기본값으로 설정.
     */
    @Bean
    @ConditionalOnMissingClass("org.slf4j.Logger")
    @ConditionalOnMissingBean(LogNotifier.class)
    public LogNotifier defaultLogNotifier() {
        System.out.println("No SLF4J found. Using DefaultLogNotifier.");
        return DefaultLogNotifier.getInstance();
    }
}
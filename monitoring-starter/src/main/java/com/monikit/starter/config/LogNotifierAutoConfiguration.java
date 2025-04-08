package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.DefaultLogNotifier;
import com.monikit.core.LogNotifier;

/**
 * `LogNotifier`의 기본 구현을 제공하는 자동 구성 클래스.
 * <p>
 * - 사용자가 `LogNotifier` 빈을 직접 등록하지 않은 경우, 기본 구현체인 {@link DefaultLogNotifier}를 제공한다.
 * - SLF4J 여부와 무관하게 사용자의 선택을 우선시하며, 프레임워크에 종속되지 않도록 설계되었다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
@Configuration
public class LogNotifierAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LogNotifierAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(LogNotifier.class)
    public LogNotifier defaultLogNotifier() {
        logger.info("No custom LogNotifier found. Using DefaultLogNotifier.");
        return DefaultLogNotifier.getInstance();
    }

}
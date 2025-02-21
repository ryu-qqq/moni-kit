package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.DefaultErrorLogNotifier;
import com.monikit.core.ErrorLogNotifier;

/**
 * `ErrorLogNotifier`의 구현체를 자동으로 주입하는 설정 클래스.
 * <p>
 * - 사용자가 `ErrorLogNotifier` 빈을 등록하면 이를 우선 사용함.
 * - 별도의 빈이 없을 경우 기본적으로 `DefaultErrorLogNotifier`를 주입함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class ErrorLogNotifierAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogNotifierAutoConfiguration.class);

    /**
     * `ErrorLogNotifier`의 기본값으로 `DefaultErrorLogNotifier`를 제공.
     */
    @Bean
    @ConditionalOnMissingBean(ErrorLogNotifier.class)
    public ErrorLogNotifier defaultErrorLogNotifier() {
        logger.info("No custom ErrorLogNotifier found. Using DefaultErrorLogNotifier.");
        return DefaultErrorLogNotifier.getInstance();
    }

}
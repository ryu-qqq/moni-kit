package com.monikit.starter.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.core.DefaultErrorLogNotifier;
import com.monikit.core.DefaultLogNotifier;
import com.monikit.core.ErrorLogNotifier;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogNotifier;
import com.monikit.starter.LogbackLogNotifier;

import jakarta.annotation.PostConstruct;

/**
 * ErrorLogNotifier의 구현체를 자동으로 주입하는 설정 클래스.
 * <p>
 * - 사용자가 ErrorLogNotifier 빈을 등록하면 이를 우선 사용함.
 * - 별도의 빈이 없을 경우 기본적으로 DefaultErrorLogNotifier를 주입함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class ErrorLogNotifierAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogNotifierAutoConfiguration.class);
    private final ApplicationContext applicationContext;

    public ErrorLogNotifierAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 사용자가 ErrorLogNotifier 빈을 등록하지 않았을 경우, 기본 구현체로 DefaultErrorLogNotifier를 제공한다.
     *
     * @return DefaultErrorLogNotifier instance
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(ErrorLogNotifier.class)
    public ErrorLogNotifier defaultErrorLogNotifier() {
        logger.info("No custom ErrorLogNotifier found. Using DefaultErrorLogNotifier.");
        return new DefaultErrorLogNotifier();
    }

    /**
     * 서버 시작 시 자동으로 ErrorLogNotifier를 LogEntryContextManager에 등록한다.
     */
    @PostConstruct
    public void init() {
        logger.info("🔄 Initializing ErrorLogNotifier...");

        Map<String, ErrorLogNotifier> notifiers = applicationContext.getBeansOfType(ErrorLogNotifier.class);

        if (notifiers.size() > 1) {
            logger.warn("Multiple ErrorLogNotifier beans detected: {}", notifiers.keySet());
        }

        ErrorLogNotifier selectedNotifier = notifiers.values().iterator().next();
        logger.info("Using ErrorLogNotifier: {}", selectedNotifier.getClass().getSimpleName());

        LogEntryContextManager.setErrorLogNotifier(selectedNotifier);
    }

}

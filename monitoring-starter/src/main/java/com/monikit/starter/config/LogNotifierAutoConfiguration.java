package com.monikit.starter.config;


import jakarta.annotation.PostConstruct;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogNotifier;
import com.monikit.starter.LogbackLogNotifier;

/**
 * LogNotifier의 구현체를 자동으로 주입하는 설정 클래스.
 * <p>
 * - 사용자가 LogNotifier 빈을 등록하면 이를 사용함.
 * - 별도의 빈이 없을 경우 기본적으로 LogbackLogNotifier를 주입함.
 * - 여러 개의 LogNotifier 빈이 존재할 경우, 어떤 것이 선택되는지 관리함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@AutoConfiguration
public class LogNotifierAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LogNotifierAutoConfiguration.class);
    private final ApplicationContext applicationContext;

    public LogNotifierAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * LogNotifier 의 특정 구현체가 없다면  `LogbackLogNotifier` 를 기본 구현체로 사용한다.
     *
     * @return LogNotifier instance
     */

    @Bean
    @Primary
    @ConditionalOnMissingBean(LogNotifier.class)
    @ConditionalOnClass(name = "ch.qos.logback.classic.Logger")
    public LogNotifier logNotifier() {
        return new LogbackLogNotifier();
    }

    @PostConstruct
    public void init() {
        Map<String, LogNotifier> logNotifiers = applicationContext.getBeansOfType(LogNotifier.class);
        if (logNotifiers.size() > 1) {
            logger.warn("⚠️ Multiple LogNotifier beans detected. Please ensure the correct implementation is used.");
        }

        LogNotifier notifier = applicationContext.getBean(LogNotifier.class);
        LogEntryContextManager.setLogNotifier(notifier);
    }

}

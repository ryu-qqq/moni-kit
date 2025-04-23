package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.TraceIdProvider;
import com.monikit.starter.MDCTraceIdProvider;

/**
 * `TraceIdProvider` 자동 구성 클래스.
 * <p>
 * - 사용자 정의 빈이 없을 경우, 기본 구현체인 {@link MDCTraceIdProvider}를 자동으로 등록함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
@Configuration
public class TraceIdProviderAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TraceIdProviderAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(TraceIdProvider.class)
    public TraceIdProvider traceIdProvider() {
        logger.info("[MoniKit] No custom TraceIdProvider found. Using Default MDCTraceIdProvider.");
        return new MDCTraceIdProvider();
    }

}
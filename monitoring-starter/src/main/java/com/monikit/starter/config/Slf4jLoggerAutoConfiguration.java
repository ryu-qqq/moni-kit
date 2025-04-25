package com.monikit.starter.config;

import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.notifier.LogNotifier;
import com.monikit.core.notifier.LogSink;
import com.monikit.core.notifier.LogSinkCustomizer;
import com.monikit.core.TraceIdProvider;
import com.monikit.slf4j.Slf4jLogSink;
import com.monikit.slf4j.Slf4jLogger;



/**
 * SLF4J 기반 {@link LogNotifier}의 기본 구현을 자동 구성하는 클래스입니다.
 * <p>
 * 사용자가 {@link LogNotifier} 빈을 명시적으로 등록하지 않은 경우,
 * SLF4J를 활용한 {@link Slf4jLogger}를 기본 로거로 등록합니다.
 * </p>
 *
 * <p>다음과 같은 기능을 포함합니다:</p>
 * <ul>
 *     <li>{@link LogSinkCustomizer}를 통한 Sink 확장 지원</li>
 *     <li>{@link TraceIdProvider} 연동을 통한 Trace ID 자동 전파</li>
 *     <li>커스터마이저 및 Sink 목록을 로깅하여 초기 상태를 가시적으로 확인 가능</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
@Configuration
@ConditionalOnMissingBean(LogNotifier.class)
public class Slf4jLoggerAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jLoggerAutoConfiguration.class);


    @Bean
    public LogNotifier slf4jLogNotifier(@Nullable List<LogSink> sinks,
                                        @Nullable List<LogSinkCustomizer> customizers,
                                        TraceIdProvider traceIdProvider) {

        List<LogSink> effectiveSinks = new ArrayList<>();
        if (sinks != null) {
            effectiveSinks.addAll(sinks);
        }

        if (customizers != null) {
            for (LogSinkCustomizer customizer : customizers) {
                customizer.customize(effectiveSinks);
            }
            logger.info("[MoniKit] {} LogSinkCustomizer(s) applied.", customizers.size());
        }

        boolean hasSlf4jSink = effectiveSinks.stream()
            .anyMatch(sink -> sink.getClass().equals(Slf4jLogSink.class));

        if (!hasSlf4jSink) {
            logger.info("[MoniKit] No Slf4jLogSink found. Registering default Slf4jLogSink.");
            effectiveSinks.add(new Slf4jLogSink());
        }

        logger.info("[MoniKit] Registering Slf4jLogger with {} LogSink(s).", effectiveSinks.size());
        effectiveSinks.forEach(sink -> logger.info(" - Registered Sink: {}", sink.getClass().getSimpleName()));

        return new Slf4jLogger(effectiveSinks, traceIdProvider);
    }
}
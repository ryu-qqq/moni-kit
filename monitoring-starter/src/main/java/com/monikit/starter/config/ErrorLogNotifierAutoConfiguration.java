package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.DefaultErrorLogNotifier;
import com.monikit.core.ErrorLogNotifier;

/**
 * {@link ErrorLogNotifier}의 기본 구현체를 자동 주입하는 설정 클래스.
 * <p>
 * ⚠️ <strong>이 클래스는 더 이상 사용되지 않으며, {@link com.monikit.core.LogSink} 기반 구조로 대체되었습니다.</strong>
 * </p>
 *
 * <p>
 * 기존에는 예외 로그 감지를 위한 후처리를 위해 {@link ErrorLogNotifier} 빈을 주입받아 사용했지만,
 * <strong>1.1.0 이후부터는 {@link com.monikit.core.LogSink}를 통한 분기 처리 전략</strong>으로 일원화되었습니다.
 * </p>
 *
 * <p>
 * 이 설정은 <code>@Deprecated</code> 처리되었으며, 향후 버전에서 제거될 예정입니다.
 * <strong>예외 로그에 대한 Slack 전송, 통계, 알림 처리 등은 {@link com.monikit.core.LogSink}를 활용하여 구현하십시오.</strong>
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 * @deprecated 1.1.0부터 {@link com.monikit.core.LogSink} 구조로 대체됨. 향후 제거 예정.
 */

@Deprecated
public class ErrorLogNotifierAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLogNotifierAutoConfiguration.class);

    /**
     * `ErrorLogNotifier`의 기본값으로 `DefaultErrorLogNotifier`를 제공.
     */
    public ErrorLogNotifier defaultErrorLogNotifier() {
        logger.info("No custom ErrorLogNotifier found. Using DefaultErrorLogNotifier.");
        return DefaultErrorLogNotifier.getInstance();
    }


}
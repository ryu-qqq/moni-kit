package com.monikit.starter.config;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.core.DefaultErrorLogNotifier;
import com.monikit.core.ErrorLogNotifier;
import com.monikit.core.ExceptionLog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ErrorLogNotifierAutoConfiguration 테스트")
class ErrorLogNotifierAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ErrorLogNotifierAutoConfiguration.class));

    @Test
    @DisplayName("사용자가 ErrorLogNotifier를 직접 등록하면 DefaultErrorLogNotifier가 주입되지 않아야 한다.")
    void shouldUseUserDefinedErrorLogNotifierIfExists() {
        ApplicationContextRunner customContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ErrorLogNotifierAutoConfiguration.class))
            .withBean(ErrorLogNotifier.class, CustomErrorLogNotifier::new); // 사용자 정의 ErrorLogNotifier 등록

        customContextRunner.run(context -> {
            ErrorLogNotifier notifier = context.getBean(ErrorLogNotifier.class);
            assertNotNull(notifier);
            assertTrue(notifier instanceof CustomErrorLogNotifier);
            assertFalse(notifier instanceof DefaultErrorLogNotifier);
        });
    }

    static class CustomErrorLogNotifier implements ErrorLogNotifier {
        @Override
        public void onErrorLogDetected(ExceptionLog logEntry) {
            System.out.println("Custom Notifier: " + logEntry.toString());
        }
    }

}

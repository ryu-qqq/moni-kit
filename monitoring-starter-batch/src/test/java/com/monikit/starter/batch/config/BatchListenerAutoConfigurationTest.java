package com.monikit.starter.batch.config;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.batch.DefaultJobExecutionListener;
import com.monikit.starter.batch.DefaultStepExecutionListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("BatchListenerAutoConfiguration 조건부 등록 테스트")
class BatchListenerAutoConfigurationConditionalTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BatchListenerAutoConfiguration.class))
            .withBean(LogEntryContextManager.class, () -> mock(LogEntryContextManager.class))
            .withBean(TraceIdProvider.class, () -> mock(TraceIdProvider.class));

    @Nested
    @DisplayName("log-enabled = true 일 때")
    class WhenLogEnabled {

        @Test
        @DisplayName("JobExecutionListener가 등록되어야 한다")
        void shouldRegisterJobExecutionListener() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(JobExecutionListener.class);
                    assertThat(context.getBean(JobExecutionListener.class))
                        .isInstanceOf(DefaultJobExecutionListener.class);
                });
        }

        @Test
        @DisplayName("StepExecutionListener가 등록되어야 한다")
        void shouldRegisterStepExecutionListener() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(StepExecutionListener.class);
                    assertThat(context.getBean(StepExecutionListener.class))
                        .isInstanceOf(DefaultStepExecutionListener.class);
                });
        }
    }

    @Nested
    @DisplayName("log-enabled = false 일 때")
    class WhenLogDisabled {

        @Test
        @DisplayName("JobExecutionListener는 등록되지 않아야 한다")
        void shouldNotRegisterJobExecutionListener() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JobExecutionListener.class);
                });
        }

        @Test
        @DisplayName("StepExecutionListener는 등록되지 않아야 한다")
        void shouldNotRegisterStepExecutionListener() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(StepExecutionListener.class);
                });
        }
    }

    @Nested
    @DisplayName("log-enabled 설정이 없을 때")
    class WhenLogEnabledMissing {

        @Test
        @DisplayName("기본값 false로 간주되어 등록되지 않아야 한다")
        void shouldNotRegisterWhenMissingAndMatchIfMissingIsFalse() {
            contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JobExecutionListener.class);
                    assertThat(context).doesNotHaveBean(StepExecutionListener.class);
                });
        }
    }
}
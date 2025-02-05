# Monitoring-Starter

## MoniKit Starter 패키지 주요 클래스 설명


`monitoring-starter`는 **monitoring-core** 모듈에 의존성을 주입하여 로그를 관리하는 역할을 합니다.
이 부분에서는 실제 로그를 출력하고 메트릭을 수집하는 구현체가 제공됩니다. 각 관련 설정 파일은 config 디렉토리 안에 있습니다.

---


1. `MoniKitLoggingProperties`

MoniKit 로깅 설정을 관리하는 클래스.

- 상세 로깅 여부 (`detailedLogging`)
- SQL 쿼리 성능 로깅 설정 (`slowQueryThresholdMs`, `criticalQueryThresholdMs`)
- 데이터베이스 로깅 활성화 여부 (`datasourceLoggingEnabled`)

```java
package com.monikit.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "monikit.logging")
public class MoniKitLoggingProperties {
    private boolean detailedLogging = false;
    private long slowQueryThresholdMs = 1000;
    private long criticalQueryThresholdMs = 5000;
    private boolean datasourceLoggingEnabled = true;

    public boolean isDetailedLogging() { return detailedLogging; }
    public void setDetailedLogging(boolean detailedLogging) { this.detailedLogging = detailedLogging; }
    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }
    public long getCriticalQueryThresholdMs() { return criticalQueryThresholdMs; }
    public void setCriticalQueryThresholdMs(long criticalQueryThresholdMs) { this.criticalQueryThresholdMs = criticalQueryThresholdMs; }
    public boolean isDatasourceLoggingEnabled() { return datasourceLoggingEnabled; }
    public void setDatasourceLoggingEnabled(boolean datasourceLoggingEnabled) { this.datasourceLoggingEnabled = datasourceLoggingEnabled; }
}
```

---


2. `MoniKitMetricsProperties`

MoniKit 메트릭 수집 설정을 관리하는 클래스.

- 메트릭 수집 활성화 여부 (enabled)

```java
package com.monikit.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "monikit.metrics")
public class MoniKitMetricsProperties {
   private boolean enabled = true;

   public boolean isEnabled() { return enabled; }
   public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
```

---


3. `DataSourceLoggingConfig`

`LoggingDataSource` 를 자동으로 감싸서 적용하는 설정 클래스.

```java
package com.monikit.starter.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceLoggingConfig {

   private static final Logger logger = LoggerFactory.getLogger(DataSourceLoggingConfig.class);

   @Bean
   @Primary
   public DataSource loggingDataSource(DataSource originalDataSource) {
      logger.info("Using DataSource: {}", originalDataSource.getClass().getSimpleName());
      DataSource loggingDataSource = new LoggingDataSource(originalDataSource);
      logger.info("Wrapped DataSource with LoggingDataSource: {}", loggingDataSource.getClass().getSimpleName());

      return loggingDataSource;
   }
}
```

---


4. `FilterAutoConfiguration`

필터를 자동으로 등록하는 설정 클래스.

```java
package com.monikit.starter.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterAutoConfiguration {

   @Bean
   public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
      FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
      registrationBean.setFilter(new TraceIdFilter());
      registrationBean.setOrder(1);
      registrationBean.addUrlPatterns("/*");
      return registrationBean;
   }

   @Bean
   public FilterRegistrationBean<LogContextScopeFilter> logContextScopeFilter() {
      FilterRegistrationBean<LogContextScopeFilter> registrationBean = new FilterRegistrationBean<>();
      registrationBean.setFilter(new LogContextScopeFilter());
      registrationBean.setOrder(2);
      registrationBean.addUrlPatterns("/*");
      return registrationBean;
   }

   @Bean
   public HttpMetricsFilter httpMetricsFilter(MetricCollector metricCollector) {
      return new HttpMetricsFilter(metricCollector);
   }

   @Bean
   public FilterRegistrationBean<HttpMetricsFilter> httpMetricsFilterRegistration(HttpMetricsFilter httpMetricsFilter) {
      FilterRegistrationBean<HttpMetricsFilter> registrationBean = new FilterRegistrationBean<>();
      registrationBean.setFilter(httpMetricsFilter);
      registrationBean.setOrder(3);
      registrationBean.addUrlPatterns("/*");
      return registrationBean;
   }
}

```
* `TraceIdFilter` : 요청 단위로 Trace ID를 자동으로 설정하는 필터.
* `LogContextScopeFilter`: HTTP 요청 단위로 LogContextScope를 관리하는 필터.
* `HttpMetricsFilter`: HTTP 요청 메트릭을 Prometheus로 전송하는 필터.

---


5. `InterceptorAutoConfiguration`

HTTP 인터셉터를 자동으로 등록하는 설정 클래스.

```java
package com.monikit.starter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.monikit.starter.interceptor.HttpLoggingInterceptor;

@Configuration
public class InterceptorAutoConfiguration implements WebMvcConfigurer {

   @Bean
   public HttpLoggingInterceptor httpLoggingInterceptor() {
      return new HttpLoggingInterceptor();
   }

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(httpLoggingInterceptor())
              .addPathPatterns("/**");
   }
}

```

* `HttpLoggingInterceptor`: HTTP 요청 및 응답을 자동으로 로깅하는 인터셉터.



---


6.  `ExecutorBeanPostProcessor`
`ExecutorBeanPostProcessor` 클래스는 Spring의 `Executor` 빈을 감싸서 스레드 컨텍스트를 자동으로 적용합니다.
Spring에서 관리되는 `Executor` 빈을 감지하고, 이를 실행할 때마다 `ThreadContextPropagator.runWithContext()`를 호출하여 스레드 컨텍스트를 자동으로 복사합니다.

```java
package com.monikit.starter;

import java.util.concurrent.Executor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.monikit.core.ThreadContextPropagator;

/**
 * 모든 Executor 빈을 감싸서 ThreadContextPropagator를 자동 적용하는 PostProcessor.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExecutorBeanPostProcessor implements BeanPostProcessor {

   @Override
   public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
      if (bean instanceof Executor executor) {
         return wrapExecutor(executor);
      }
      return bean;
   }

   private Executor wrapExecutor(Executor executor) {
      return task -> {
         try {
            ThreadContextPropagator.runWithContext(() -> {
               executor.execute(task);
               return null;
            });
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      };
   }
}

```
**동작 설명**
* `ExecutorBeanPostProcessor` 는 Spring에서 등록되는 모든 Executor 빈을 감지하고, 실행될 때마다 **ThreadContextPropagator.runWithContext()**를 호출하여 스레드 컨텍스트를 유지합니다.
* `Executor` 빈이 실행될 때마다 부모 스레드의 컨텍스트가 자식 스레드로 전파되어 작업이 실행됩니다.

---

7. `LogNotifierAutoConfiguration`

이 클래스는 로깅과 관련된 알림 처리 방식(`LogNotifier`)을 자동으로 설정합니다.

### 주요 기능:
- **`LogNotifier` 빈 자동 등록**:
    - `LogNotifier`를 구현한 빈이 **없을 경우** 기본적으로 `LogbackLogNotifier`나 `DefaultLogNotifier`가 자동으로 등록됩니다.
    - `LogbackLogNotifier`는 Logback을 사용하는 환경에서 적합하며, Logback 클래스를 찾을 수 없으면 `DefaultLogNotifier`가 사용됩니다.

- **다중 `LogNotifier` 빈 처리**:
    - Spring 애플리케이션에서 `LogNotifier`의 구현체가 **여러 개** 존재하는 경우, 이 설정은 경고 로그를 남기고 어떤 구현체가 실제로 사용되고 있는지 정보를 제공합니다.

- **로깅 컨텍스트 설정**:
    - 로깅 시스템에서 `LogNotifier`의 설정을 초기화한 후, 이를 `LogEntryContextManager`에 설정하여 애플리케이션의 로깅 흐름을 관리합니다.

### 사용 예시
- Logback이 포함된 환경에서는 `LogbackLogNotifier`가 자동으로 적용됩니다.
- Logback 의존성이 없는 환경에서는 `DefaultLogNotifier`가 기본 구현체로 사용됩니다.

---

8. `PrometheusMetricCollector`

이 클래스는 Prometheus 기반의 메트릭 수집기입니다. 애플리케이션의 HTTP 요청 및 SQL 쿼리 실행 메트릭을 Prometheus로 수집하여, 시스템 성능과 리소스 사용량을 모니터링하는 데 사용됩니다.

### 주요 기능:
- **HTTP 요청 메트릭 수집**:
    - HTTP 요청의 메서드, URI, 상태 코드, 실행 시간 등을 Prometheus로 수집하여 시스템 상태를 모니터링합니다.

- **SQL 쿼리 메트릭 수집**:
    - SQL 쿼리 실행 횟수와 실행 시간을 기록하여, 데이터베이스 쿼리 성능을 모니터링합니다.

- **`MeterRegistry`와의 통합**:
    - Prometheus 메트릭을 수집하는 데 사용되는 `MeterRegistry`와 통합되어, 메트릭을 쉽게 Prometheus에 전송할 수 있도록 설정합니다.

### 사용 예시
- HTTP 요청과 SQL 쿼리의 실행 시간을 수집하고 Prometheus에서 시스템 성능을 모니터링할 수 있습니다.

---


9.  `ExecutionLoggingAspect` – 서비스 및 레포지토리 메서드 실행 시간 로깅

`ExecutionLoggingAspect`는 Spring의 AOP(Aspect-Oriented Programming)를 사용하여, `@Service` 및 `@Repository` 어노테이션이 붙은 모든 메서드의 실행 시간을 측정하고, 메서드 실행에 관련된 정보를 로깅하는 역할을 합니다.

- **포인트컷**: `@Service` 및 `@Repository` 어노테이션이 붙은 클래스의 모든 메서드에 대해 실행 시간을 측정합니다.
- **로깅 내용**:
    - 메서드 실행 시간
    - 입력 파라미터
    - 출력 값
- **예외 처리**: 예외가 발생하면 예외 정보를 로그에 기록하며, 실행 시간이 여전히 로깅됩니다.
- **상세 로깅 여부**: `detailedLogging` 값에 따라, 실행 시간 외에도 입력 파라미터와 출력 값을 상세히 로깅할지 결정합니다.

--- 


10.  `FeignLogger` – Feign 클라이언트 요청 및 응답 로깅

`FeignLogger`는 Feign 클라이언트를 사용하여 외부 API와의 HTTP 요청 및 응답을 자동으로 로깅하는 클래스입니다.

- **요청 로깅**:
    - HTTP 메서드
    - 요청 URL
    - 요청 헤더 및 바디
    - 요청 시작 시간을 포함한 `HttpOutboundRequestLog` 객체로 로깅
- **응답 로깅**:
    - HTTP 응답 상태 코드
    - 응답 헤더 및 바디
    - 응답에 대한 상태 코드 및 응답 시간을 포함한 `HttpOutboundResponseLog` 객체로 로깅
- **LogEntryContextManager**: 로깅된 요청 및 응답 정보를 `LogEntryContextManager`에 저장하여, 후속 로깅 및 추적이 용이하도록 합니다.

`FeignLogger`는 기본적으로 **Feign의 로거**를 확장하여 사용하며, HTTP 요청과 응답을 특정 로그 엔트리 객체로 변환하여 관리합니다. 로깅된 정보는 `LogEntryContextManager`를 통해 중앙화된 로깅 시스템에 기록됩니다.

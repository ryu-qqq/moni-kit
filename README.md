# `moni-kit`: 장애 감지 및 대응을 위한 로그 관리 라이브러리

### "내 서버는 과연 장애를 빠르게 감지하고, 신속히 대응할 수 있도록 설계되었는가?" 🤔

이 질문을 던졌을 때, 제가 만든 서버는 사실 그다지 효율적이지 않았습니다. 단순히 **슬랙**으로 오류 메시지를 보내고, **키바나**에서 로그를 수동으로 검색해야만 장애를 추적할 수 있었습니다. 그게 얼마나 비효율적인지, 그때까지는 전혀 깨닫지 못했죠.

그런데 **토스 러너스하이 세션**을 듣고 나서, **장애를 빠르게 감지하고 신속히 대응할 수 있는 설계가 무엇인지** 진지하게 고민하게 되었습니다.

### 로깅에 관한 문제

장애를 빠르게 감지하고 대응하기 위해 로깅 시스템을 구축하는 과정에서, 예상보다 로깅 작업이 훨씬 커지게 되었다는 사실을 깨달았습니다.

특히 서버가 늘어날수록 각 서버의 로그 포맷을 일일이 맞추는 작업이 필요해졌고, 기존 서버의 로그 포맷을 다시 확인하는 과정이 비효율적이라는 문제도 발생했습니다.

또한, 새로운 팀원이 들어올 때마다 로그 포맷을 매번 설명해야 하는 상황이 반복될것이고, 로그 포맷을 수동으로 복사하고 붙여넣는 과정에서 휴먼 에러가 발생할 확률도 높아질 것 입니다.

### 해결책: `moni-kit` 라이브러리

이 문제를 해결하고자 **`moni-kit`** 라이브러리를 만들었습니다. 그 과정에서 **"소규모 팀에서도 카프카와 같은 복잡한 시스템 없이 효율적인 로그 관리를 할 수는 없을까?"**라는 고민이 있었습니다.

**`moni-kit`**는 모든 서버에서 일관된 로그 포맷을 자동으로 적용하며, 장애 발생 시 **빠르고 효율적으로 대응할 수 있도록** 설계되었습니다. 이를 통해 개발자들이 로그 포맷을 일일이 설정하거나 관리할 필요 없이, **자동으로** 장애를 추적하고 대응할 수 있게 됐습니다.

### 앞으로의 비전

**토스 러너스하이 세션**에서의 중요한 질문, **"장애를 빠르게 감지하고 신속히 대응할 수 있도록 설계되었는가?"**에 대한 답을 찾으면서 저는 **`moni-kit`**을 더 많은 개발자들에게 유용하게 만들기 위해 꾸준히 발전시켜 나갈 것입니다.

### 결과: 더 효율적이고 신속한 장애 대응

이제, 개발자들은 **로그 설정에 대한 걱정 없이** 장애를 빠르게 파악하고 대응할 수 있습니다. **이게 바로 `moni-kit`의 장점이라 생각합니다. ✨

---

**`moni-kit`**는 장애를 빠르게 감지하고, 신속히 대응할 수 있도록 설계된 로그 관리 라이브러리로, 소규모 팀에서도 쉽게 사용할 수 있도록 만들어졌습니다.


---

## MoniKit 프로젝트 구조

---
# 프로젝트 구조

```
root/                       
│── monitoring-core/          # 로깅에 필요한 필수 순수 자바 코드      
│── monitoring-starter/       # 실제 로그를 출력하고 메트릭을 수집하는 구현체가 제공
```
---


### 1. `monitoring-core`
`monitoring-core`는 순수 자바 객체로 구성되어 있으며, 핵심 인터페이스인 `LogEntry`와 `LogNotifier`가 포함됩니다.


#### `LogEntry` 인터페이스
모든 로그 엔트리는 `LogEntry` 인터페이스를 구현해야 합니다. 이를 통해 ELK와 Prometheus와 연동할 수 있는 구조화된 로그 데이터를 생성할 수 있습니다.


```java
package com.monikit.core;

import java.time.Instant;

public interface LogEntry {
    Instant getTimestamp();
    String getTraceId();
    LogType getLogType();
    LogLevel getLogLevel();
    String toString();
}

```

- getTimestamp(): 로그 생성 시각 (UTC 기준)
- getTraceId(): 트랜잭션을 추적하기 위한 고유 ID
- getLogType(): 로그 유형 (예: EXECUTION_TIME, BUSINESS_EVENT, EXCEPTION 등)
- getLogLevel(): 로그 레벨 (TRACE, DEBUG, INFO, WARN, ERROR)



#### `LogNotifier` 인터페이스
로그를 출력하는 역할을 합니다. **monitoring-starter** 에서 구현하며, **monitoring-core** 는 의존성을 갖지 않습니다.

```java
package com.monikit.core;

public interface LogNotifier {
        void notify(LogLevel logLevel, String message);
        void notify(LogEntry logEntry);
}
```

#### `MetricCollector` 인터페이스
HTTP 요청 및 SQL 쿼리 메트릭을 수집하는 공통 인터페이스입니다.

```java
package com.monikit.core;

public interface MetricCollector {
   void recordHttpRequest(String method, String uri, int statusCode, long duration);
   void recordQueryMetrics(String sql, long executionTime, String dataSourceName);
}

```


#### `LogEntryContext` 인터페이스
스레드로컬을 이용해 로그 엔트리 큐를 관리합니다. 로그가 요청 단위로 저장되고 관리됩니다.

```java
package com.monikit.core;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogEntryContext {
   private static final InheritableThreadLocal<Queue<LogEntry>> logThreadLocal =
           new InheritableThreadLocal<>() {
              @Override
              protected Queue<LogEntry> initialValue() {
                 return new ConcurrentLinkedQueue<>();
              }
           };

   private static final InheritableThreadLocal<Boolean> hasError =
           new InheritableThreadLocal<>() {
              @Override
              protected Boolean initialValue() {
                 return false;
              }
           };

   // 로그 추가
   static void addLog(LogEntry logEntry) {
      logThreadLocal.get().add(logEntry);
   }

   // 로그 반환
   public static Queue<LogEntry> getLogs() {
      return new ConcurrentLinkedQueue<>(logThreadLocal.get());
   }

   // 요청에서 예외가 발생했는지 여부 확인
   public static boolean hasError() {
      return hasError.get();
   }

   // 예외 발생 여부 설정
   static void setErrorOccurred(boolean errorOccurred) {
      hasError.set(errorOccurred);
   }

   // 자식 스레드로 컨텍스트 전파
   static Runnable propagateToChildThread(Runnable task) {
      Queue<LogEntry> parentLogs = new ConcurrentLinkedQueue<>(logThreadLocal.get());
      Boolean parentHasError = hasError.get();
      return () -> {
         logThreadLocal.set(new ConcurrentLinkedQueue<>(parentLogs));
         hasError.set(parentHasError);
         task.run();
      };
   }

   static <T> Callable<T> propagateToChildThread(Callable<T> task) {
      Queue<LogEntry> parentLogs = new ConcurrentLinkedQueue<>(logThreadLocal.get());
      Boolean parentHasError = hasError.get();
      return () -> {
         logThreadLocal.set(new ConcurrentLinkedQueue<>(parentLogs));
         hasError.set(parentHasError);
         return task.call();
      };
   }
}


```

#### `LogEntryContextManager` 인터페이스
**LogEntryContext**를 관리하는 매니저 클래스입니다. 로그를 추가하고, 관리하는 역할을 수행합니다.


```java
package com.monikit.core;

import java.util.concurrent.Callable;

public class LogEntryContextManager {
   private static final int MAX_LOG_SIZE = 1000;
   private static LogNotifier logNotifier;

   // LogNotifier 설정
   public static void setLogNotifier(LogNotifier notifier) {
      logNotifier = notifier;
   }

   // 로그 추가
   public static void addLog(LogEntry logEntry) {
      if (LogEntryContext.size() >= MAX_LOG_SIZE) {
         logNotifier.notify(LogLevel.WARN, "LogEntryContext cleared due to size limit");
         flush();
         LogEntryContext.clear();
      }
      LogEntryContext.addLog(logEntry);
   }

   // 모든 로그 출력 및 컨텍스트 초기화
   public static void flush() {
      for (LogEntry log : LogEntryContext.getLogs()) {
         logNotifier.notify(log);
      }
      LogEntryContext.clear();
      LogEntryContext.setErrorOccurred(false);
   }

   // 부모 스레드의 컨텍스트를 자식 스레드로 전달하는 Runnable 생성
   public static Runnable propagateToChildThread(Runnable task) {
      return LogEntryContext.propagateToChildThread(task);
   }

   public static <T> Callable<T> propagateToChildThread(Callable<T> task) {
      return LogEntryContext.propagateToChildThread(task);
   }

   // 예외 로깅
   public static void logException(String traceId, Throwable exception) {
      if (LogEntryContext.hasError()) {
         return;
      }
      LogEntryContext.addLog(ExceptionLog.create(traceId, exception));
      LogEntryContext.setErrorOccurred(true);
   }
}

```



### 2. `monitoring-starter`
`monitoring-starter`는 **monitoring-core** 모듈에 의존성을 주입하여 로그를 관리하는 역할을 합니다. 
이 부분에서는 실제 로그를 출력하고 메트릭을 수집하는 구현체가 제공됩니다.


#### `MoniKitLoggingProperties`

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


#### `MoniKitMetricsProperties`

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

#### `DataSourceLoggingConfig`

LoggingDataSource를 자동으로 감싸서 적용하는 설정 클래스.

- 메트릭 수집 활성화 여부 (enabled)

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


#### `FilterAutoConfiguration`

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


#### `TraceIdFilter`

요청 단위로 Trace ID를 자동으로 설정하는 필터.

```java
package com.monikit.starter.filter;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TraceIdFilter extends OncePerRequestFilter {

   private static final String TRACE_ID_HEADER = "X-Trace-Id";
   private static final String MDC_TRACE_ID_KEY = "traceId";

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
      String traceId = request.getHeader(TRACE_ID_HEADER);

      if (traceId == null || traceId.isEmpty()) {
         traceId = UUID.randomUUID().toString();
      }

      MDC.put(MDC_TRACE_ID_KEY, traceId);
      response.setHeader(TRACE_ID_HEADER, traceId);

      try {
         filterChain.doFilter(request, response);
      } finally {
         MDC.clear();
      }
   }
}

```



#### `LogContextScopeFilter`

HTTP 요청 단위로 LogContextScope를 관리하는 필터.

```java
package com.monikit.starter.filter;

import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import com.monikit.core.LogContextScope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LogContextScopeFilter extends OncePerRequestFilter {

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

      try (LogContextScope scope = new LogContextScope()) {
         RequestWrapper requestWrapper = new RequestWrapper(request);
         ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
         filterChain.doFilter(requestWrapper, wrappedResponse);
      }
   }
}


```



#### `HttpMetricsFilter`

HTTP 요청 메트릭을 Prometheus로 전송하는 필터.

```java
package com.monikit.starter.filter;

import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import com.monikit.core.MetricCollector;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpMetricsFilter extends OncePerRequestFilter {

   private final MetricCollector metricCollector;

   public HttpMetricsFilter(MetricCollector metricCollector) {
      this.metricCollector = metricCollector;
   }

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
           throws ServletException, IOException {
      long startTime = System.currentTimeMillis();
      try {
         filterChain.doFilter(request, response);
      } finally {
         long duration = System.currentTimeMillis() - startTime;
         metricCollector.recordHttpRequest(request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
      }
   }
}
```



#### `InterceptorAutoConfiguration`

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

#### `HttpLoggingInterceptor`

HTTP 요청 및 응답을 자동으로 로깅하는 인터셉터.

```java
package com.monikit.starter.interceptor;

import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;
import com.monikit.core.HttpInboundRequestLog;
import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.filter.RequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

   private static final ThreadLocal<Instant> requestStartTime = new ThreadLocal<>();

   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      String traceId = TraceIdProvider.currentTraceId();
      requestStartTime.set(Instant.now());

      LogEntryContextManager.addLog(HttpInboundRequestLog.create(
              traceId,
              request.getMethod(),
              request.getRequestURI(),
              request.getQueryString(),
              extractHeaders(request),
              extractRequestBody(request),
              request.getRemoteAddr(),
              request.getHeader("User-Agent"),
              LogLevel.INFO
      ));

      return true;
   }

   @Override
   public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws
           IOException {
      String traceId = TraceIdProvider.currentTraceId();
      Instant startTime = requestStartTime.get();
      long executionTime = startTime != null ? Instant.now().toEpochMilli() - startTime.toEpochMilli() : 0;

      LogEntryContextManager.addLog(HttpInboundResponseLog.create(
              traceId,
              request.getMethod(),
              request.getRequestURI(),
              response.getStatus(),
              extractHeaders(response),
              extractResponseBody(response),
              executionTime,
              LogLevel.INFO
      ));

      requestStartTime.remove();
   }

   private String extractHeaders(HttpServletRequest request) {
      StringBuilder headers = new StringBuilder();
      Enumeration<String> headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
         String headerName = headerNames.nextElement();
         headers.append(headerName).append(": ").append(request.getHeader(headerName)).append("; ");
      }
      return headers.toString();
   }

   private String extractHeaders(HttpServletResponse response) {
      StringBuilder headers = new StringBuilder();
      for (String headerName : response.getHeaderNames()) {
         headers.append(headerName).append(": ").append(response.getHeader(headerName)).append("; ");
      }
      return headers.toString();
   }

   private String extractRequestBody(HttpServletRequest request) {
      if (request instanceof RequestWrapper) {
         byte[] requestBody = ((RequestWrapper) request).getContentAsByteArray();
         return new String(requestBody);
      }
      return "RequestBody Can't read";
   }

   private String extractResponseBody(HttpServletResponse response) throws IOException {
      if (response instanceof ContentCachingResponseWrapper wrappedResponse) {
         byte[] contentAsByteArray = wrappedResponse.getContentAsByteArray();
         wrappedResponse.copyBodyToResponse();
         return new String(contentAsByteArray);
      }
      return "ResponseBody Can't read";
   }

}

```

## MoniKit 서버 모니터링 및 로그 관리 시스템

### 기능 설명

1. **설정 관리 클래스**  
   `MoniKitLoggingProperties`와 `MoniKitMetricsProperties`를 사용하여 로깅 및 메트릭 수집의 활성화 여부와 세부 설정을 관리합니다.
   - 로깅 설정: `detailedLogging`, `slowQueryThresholdMs`, `criticalQueryThresholdMs`, `datasourceLoggingEnabled` 등
   - 메트릭 수집 설정: `enabled` 설정으로 메트릭 수집 활성화

2. **데이터 소스 로깅**
   - `LoggingDataSource`를 통해 기본 데이터소스를 프록시 객체로 감싸 SQL 쿼리 성능 및 데이터베이스 로깅을 지원합니다.

3. **자동 필터 등록**
   - 요청 처리 시 `TraceIdFilter`, `LogContextScopeFilter`, `HttpMetricsFilter`가 자동으로 등록되어 로깅 및 메트릭 수집을 처리합니다.
   - **TraceIdFilter**: 요청마다 고유한 `TraceId`를 자동 생성 및 관리
   - **LogContextScopeFilter**: 요청별로 `LogContextScope`를 관리하고 메모리 누수를 방지
   - **HttpMetricsFilter**: HTTP 요청에 대한 메트릭을 수집하여 Prometheus와 같은 시스템에 전송

4. **인터셉터 등록**
   - `HttpLoggingInterceptor`를 통해 HTTP 요청 및 응답을 로깅하고, `TraceId`를 자동으로 관리합니다.

### 향후 개발 계획

지속적으로 개발을 진행하여, ELK 스택과 Prometheus 또는 다른 라이브러리를 활용하여 효율적으로 로그와 메트릭을 수집하고, 수집된 데이터를 데이터화하여 서버의 고도화 작업에 집중할 수 있을 것입니다.

### PR 기여

이 프로젝트는 오픈 소스로 PR 기여를 환영합니다. 감사합니다~~


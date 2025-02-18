# `moni-kit`: 장애 감지 및 대응을 위한 로그 관리 라이브러리

### "내 서버는 과연 장애를 빠르게 감지하고, 신속히 대응할 수 있도록 설계되었는가?" 🤔

이 질문을 던졌을 때, 제가 만든 서버는 사실 그다지 효율적이지 않았습니다. 단순히 **슬랙**으로 오류 메시지를 보내고, **키바나**에서 로그를 수동으로 검색해야만 장애를 추적할 수 있었습니다. 그게 얼마나 비효율적인지, 그때까지는 전혀 깨닫지 못했죠.

그런데 **토스 러너스하이 세션**을 듣고 나서, **장애를 빠르게 감지하고 신속히 대응할 수 있는 설계가 무엇인지** 진지하게 고민하게 되었습니다.

### 로깅에 관한 문제

장애를 빠르게 감지하고 대응하기 위해 로깅 시스템을 구축하는 과정에서, 예상보다 로깅 작업이 훨씬 커지게 되었다는 사실을 깨달았습니다.

특히 서버가 늘어날수록 각 서버의 로그 포맷을 일일이 맞추는 작업이 필요해졌고, 기존 서버의 로그 포맷을 다시 확인하는 과정이 비효율적이라는 문제도 발생했습니다.

또한, 새로운 팀원이 들어올 때마다 로그 포맷을 매번 설명해야 하는 상황이 반복될것이고, 로그 포맷을 수동으로 복사하고 붙여넣는 과정에서 휴먼 에러가 발생할 확률도 높아질 것 입니다.

### 해결책: `moni-kit` 라이브러리

이 문제를 해결하고자 **`moni-kit`** 라이브러리를 만들었습니다. 그 과정에서 **"소규모 팀에서도 카프카를 사용한 로깅 전용 서버 같은  복잡한 시스템 없이 효율적인 로그 관리를 할 수는 없을까?"**라는 고민이 있었습니다.

**`moni-kit`**는 모든 서버에서 일관된 로그 포맷을 자동으로 적용하며, 장애 발생 시 **빠르고 효율적으로 대응할 수 있도록** 설계되었습니다. 이를 통해 개발자들이 로그 포맷을 일일이 설정하거나 관리할 필요 없이, **자동으로** 장애를 추적하고 대응할 수 있게 됐습니다.

### 앞으로의 비전

**토스 러너스하이 세션**에서의 중요한 교훈, **"장애를 빠르게 감지하고 신속히 대응할 수 있도록 설계되었는가?"**에 대한 답을 찾으면서 저는 **`moni-kit`**을 더 많은 개발자들에게 유용하게 만들기 위해 꾸준히 발전시켜 나갈 것입니다.

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

1. [monitoring-core](monitoring-core/README.md)  패키지 상세 설명
2. [monitoring-starter](monitoring-starter/README.md) 패키지 상세 설명

### 사용법 

**monikit**을 프로젝트에 추가하려면, build.gradle 파일에 아래 의존성을 추가합니다.

```gradle
	repositories {
		maven { url 'https://jitpack.io' }
	}
	
	implementation "com.github.ryu-qqq.moni-kit:monikit-core:${moniKitVersion}"
	implementation "com.github.ryu-qqq.moni-kit:monikit-starter:${moniKitVersion}"
	
	// moniKitVersion 1.0.0 이 최신 릴리즈 버전
	
```

그 후, **application.yml** 파일에서 **monikit** 설정을 구성합니다. 아래는 설정 예시입니다.

```yml
monikit:
   logging:
      detailedLogging: true  # 상세 로깅 여부 (기본값: false)
      slowQueryThresholdMs: 1000  # WARN 로그 기준 (1초 이상)
      criticalQueryThresholdMs: 5000  # ERROR 로그 기준 (5초 이상)
      datasourceLoggingEnabled: true  # 데이터베이스 로깅 활성화 여부
   metrics:
      enabled: true
```

**변수 설정 설명**

1. **detailedLogging**
   - true로 설정하면, 애플리케이션 내에서 상세한 로깅을 활성화합니다. 이 값은 디버깅이나 시스템 추적에 유용하지만, 성능에 영향을 줄 수 있으므로 필요에 따라 설정하세요. 기본값은 false입니다.
2. **slowQueryThresholdMs**
   - 이 값은 데이터베이스 쿼리 실행 시간이 1초를 초과할 경우 WARN 로그로 기록되도록 설정합니다. 이 임계값을 설정하여 성능 문제를 조기에 감지할 수 있습니다.
3. **criticalQueryThresholdMs**
   - 이 값은 데이터베이스 쿼리 실행 시간이 5초 이상인 경우 ERROR 로그로 기록되도록 설정합니다. 이 임계값을 설정하여 심각한 성능 문제를 빠르게 식별할 수 있습니다.
4. **datasourceLoggingEnabled**
   - 데이터베이스 로깅을 활성화할지 여부를 결정합니다. true로 설정하면, 데이터베이스와 관련된 쿼리 및 성능 데이터를 로깅할 수 있습니다. 이 설정은 성능 분석 및 장애 감지에 유용합니다.
5. **metrics.enabled**
   - true로 설정하면, 애플리케이션의 메트릭 데이터를 Prometheus와 같은 모니터링 시스템에 전달하여 성능을 추적할 수 있습니다. 이 설정은 시스템의 건강 상태를 점검하는 데 유용합니다.

   
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

## 🛠 지원 및 피드백
- 이 프로젝트에 기여하거나 버그를 제보하려면 GitHub Issues를 이용해주세요.


---
## 📌 v1.0.1 업데이트 내용
- **1.0.1 업데이트 내용 반영**
   - `ExecutorService` 자동 래핑 추가
      - `LogEntryContext` 개선 (`ThreadLocal` 유지)  
        ⚠️ **주의:** 현재 `ScopedValue`는 Java 21에서 여전히 Preview 기능이므로 적용되지 않았습니다.  
        버츄얼 스레드를 사용할 경우, `ThreadLocal`이 스레드별로 개별 저장되기 때문에 **많은 버츄얼 스레드가 생성되면 메모리 사용량이 증가할 수 있습니다.**  
        버츄얼 스레드를 적극적으로 활용하는 환경에서는 **스레드 컨텍스트 관리 전략을 신중히 고려해야 합니다.**
    - `DataSourceProvider` 예외 처리 개선


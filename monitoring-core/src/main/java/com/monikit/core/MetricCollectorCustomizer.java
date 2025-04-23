package com.monikit.core;

import java.util.List;


/**
 * {@link MetricCollector}를 동적으로 확장하거나 변경할 수 있도록 지원하는 커스터마이저 인터페이스.
 * <p>
 * 이 인터페이스를 구현한 빈이 존재할 경우, MoniKit의 메트릭 수집 초기화 시점에 호출되어
 * {@link MetricCollector} 리스트를 사용자 정의 방식으로 조작할 수 있다.
 * </p>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>{@code
 * @Component
 * public class CustomCollectorCustomizer implements MetricCollectorCustomizer {
 *     @Override
 *     public void customize(List<MetricCollector<? extends LogEntry>> collectors) {
 *         collectors.add(new CustomHttpMetricCollector());
 *     }
 * }
 * }</pre>
 *
 * <p>
 * 주로 다음과 같은 용도로 활용된다:
 * <ul>
 *     <li>기본 수집기에 사용자 정의 수집기를 추가</li>
 *     <li>특정 조건에 따라 수집기 교체 또는 필터링</li>
 * </ul>
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.1
 */

public interface MetricCollectorCustomizer {
    void customize(List<MetricCollector<? extends LogEntry>> collectors);
}

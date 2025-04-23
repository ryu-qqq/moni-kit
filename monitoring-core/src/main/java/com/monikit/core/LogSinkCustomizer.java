package com.monikit.core;

import java.util.List;

/**
 * {@link LogSink} 리스트를 동적으로 조작할 수 있도록 지원하는 커스터마이저 인터페이스.
 * <p>
 * 이 인터페이스를 구현한 빈이 존재할 경우, MoniKit의 {@link LogNotifier} 초기화 시점에 호출되어
 * {@link LogSink} 목록에 사용자 정의 Sink를 추가하거나 변경할 수 있습니다.
 * </p>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>{@code
 * @Component
 * public class SlackSinkCustomizer implements LogSinkCustomizer {
 *     @Override
 *     public void customize(List<LogSink> sinks) {
 *         sinks.add(new SlackSink());
 *     }
 * }
 * }</pre>
 *
 * <p>
 * 주로 다음과 같은 용도로 활용됩니다:
 * <ul>
 *     <li>Slack, S3, Kafka 등 외부 연동 Sink 추가</li>
 *     <li>테스트 환경에서는 ConsoleSink만 남기고 필터링</li>
 *     <li>특정 {@link LogType}에 특화된 Sink 우선순위 조정</li>
 * </ul>
 * </p>
 *
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
public interface LogSinkCustomizer {
    void customize(List<LogSink> sinks);
}

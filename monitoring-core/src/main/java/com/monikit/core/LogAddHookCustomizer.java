package com.monikit.core;

import java.util.List;


/**
 * {@link LogAddHook} 리스트를 동적으로 확장하거나 수정할 수 있도록 지원하는 커스터마이저 인터페이스.
 * <p>
 * 이 인터페이스를 구현한 빈이 존재할 경우, {@link com.monikit.core.LogEntryContextManager} 초기화 시점에 호출되어
 * 애플리케이션 전역에 적용할 {@link LogAddHook} 리스트를 사용자 정의 방식으로 조작할 수 있습니다.
 * </p>
 *
 * <p><b>참고:</b> {@link MetricCollectorLogAddHook}는 내부적으로 별도로 등록되며,
 * 이 Customizer는 그 외의 모든 추가적인 {@link LogAddHook} 확장 시에 사용됩니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */

public interface LogAddHookCustomizer {
    void customize(List<LogAddHook> hooks);
}

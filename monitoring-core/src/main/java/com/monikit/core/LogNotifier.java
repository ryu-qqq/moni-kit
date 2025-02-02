package com.monikit.core;

/**
 * 로그를 출력하는 인터페이스.
 * <p>
 * 구현체는 Spring Starter에서 제공하며, 코어 모듈은 의존성을 갖지 않는다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */

public interface LogNotifier {

    void notify(LogLevel logLevel, String message);
}

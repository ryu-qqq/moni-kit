package com.monikit.core;

import java.util.concurrent.Callable;

/**
 * 스레드 컨텍스트 전파를 담당하는 인터페이스.
 * <p>
 * - 부모 스레드의 로그 컨텍스트를 자식 스레드로 전달하는 기능을 제공한다.
 * - 기본 구현체는 {@link DefaultThreadContextHandler}이며, 필요하면 커스텀 가능.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1
 */

public interface ThreadContextHandler {

    /**
     * 부모 스레드의 로그 컨텍스트를 자식 스레드로 전달하는 Runnable을 생성한다.
     * <p>
     * - 멀티스레드 환경에서 부모 스레드의 로그 컨텍스트를 유지하면서 자식 스레드에서 실행할 수 있도록 지원한다.
     * </p>
     *
     * @param task 실행할 {@link Runnable}
     * @return 부모 스레드의 컨텍스트가 복사된 새로운 {@link Runnable}
     */
    Runnable propagateToChildThread(Runnable task);

    /**
     * 부모 스레드의 로그 컨텍스트를 자식 스레드로 전달하는 Callable을 생성한다.
     * <p>
     * - 멀티스레드 환경에서 부모 스레드의 로그 컨텍스트를 유지하면서 자식 스레드에서 실행할 수 있도록 지원한다.
     * </p>
     *
     * @param <T> 반환 타입
     * @param task 실행할 {@link Callable}
     * @return 부모 스레드의 컨텍스트가 복사된 새로운 {@link Callable}
     */
    <T> Callable<T> propagateToChildThread(Callable<T> task);

    /**
     * 부모 스레드의 로그 컨텍스트를 자식 스레드로 전달하는 {@link ThrowingCallable}.
     * <p>
     * - `Throwable`을 던질 수 있는 작업을 처리할 수 있도록 확장된 버전.
     * </p>
     *
     * @param <T> 반환 타입
     * @param task 실행할 {@link ThrowingCallable}
     * @return 부모 스레드의 컨텍스트가 복사된 새로운 {@link ThrowingCallable}
     */
    <T> ThrowingCallable<T> propagateToChildThreadThrowable(ThrowingCallable<T> task);

    /**
     * 예외 발생 시 예외 정보를 로그 컨텍스트에 추가한다.
     *
     * @param traceId   현재 요청의 트레이스 ID
     * @param exception 발생한 예외
     * @param errorCategory 예외의 카테고리
     */
    void logException(String traceId, Throwable exception, ErrorCategory errorCategory);

    default void propagateAndRun(Runnable task) {
        propagateToChildThread(task).run();
    }

    default <T> T propagateAndCall(Callable<T> task) throws Exception {
        return propagateToChildThread(task).call();
    }

    default <T> T propagateAndCallThrowable(ThrowingCallable<T> task) throws Throwable {
        return propagateToChildThreadThrowable(task).call();
    }

    @FunctionalInterface
    interface ThrowingCallable<T> {
        T call() throws Throwable;
    }

}

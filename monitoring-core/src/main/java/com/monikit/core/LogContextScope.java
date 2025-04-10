package com.monikit.core;
/**
 * 요청 단위로 {@link LogEntryContextManager}를 사용하여 로그 컨텍스트를 자동으로 관리하는 클래스.
 * <p>
 * - {@link LogEntryContextManager}는 요청에 대한 로그 컨텍스트를 관리하고, 요청 종료 시 로그를 플러시하여 처리한다.
 * - {@link LogContextScope}는 {@code try-with-resources} 구문을 활용하여 자동으로 로그 플러시를 호출한다.
 * - 요청이 시작될 때 {@code clear()} 메서드를 호출하여 이전의 로그 컨텍스트를 초기화하고,
 *   요청이 끝날 때 {@code flush()} 메서드를 호출하여 로그를 저장한다.
 * </p>
 *
 * 사용 예시:
 * <pre>{@code
 * try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
 *     // 요청 처리 중 로그 컨텍스트가 자동으로 관리된다.
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 * @see LogEntryContextManager
 */
public class LogContextScope implements AutoCloseable {

    private final LogEntryContextManager logEntryContextManager;

    /**
     * 새로운 {@link LogContextScope} 인스턴스를 생성한다.
     * <p>
     * - 요청이 시작될 때 {@link LogEntryContextManager#clear()}를 호출하여 로그 컨텍스트를 초기화한다.
     * </p>
     *
     * @param logEntryContextManager {@link LogEntryContextManager} 인스턴스
     */
    public LogContextScope(LogEntryContextManager logEntryContextManager) {
        this.logEntryContextManager = logEntryContextManager;
        logEntryContextManager.clear();
    }

    /**
     * {@link LogContextScope}가 종료될 때 호출되며, 로그를 플러시하여 요청에 대한 로그를 저장한다.
     * <p>
     * - {@link LogEntryContextManager#flush()}를 호출하여 로그를 저장한다.
     * - flush() 실패 시 예외를 삼켜서 애플리케이션 흐름에 영향을 주지 않도록 안전하게 처리한다.
     * </p>
     */
    @Override
    public void close() {
        try {
            logEntryContextManager.flush();
        } catch (Exception e) {
            System.err.println("[monikit] logContext flush failed: " + e.getMessage());
        }
    }

}

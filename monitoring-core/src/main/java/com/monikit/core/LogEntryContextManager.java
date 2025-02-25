package com.monikit.core;

/**
 * 로그 컨텍스트를 관리하는 핸들러 인터페이스.
 * <p>
 * - 로그를 추가, 조회, 삭제하는 기능을 제공하며, 요청 단위의 로깅을 관리한다.
 * - 기본 구현체는 {@link DefaultLogEntryContextManager}이며, 필요하면 커스텀 구현체를 등록하여 확장 가능하다.
 * - 멀티스레드 환경에서도 요청 컨텍스트를 유지할 수 있도록 {@code propagateToChildThread()} 메서드를 제공한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public interface LogEntryContextManager {

    /**
     * 현재 요청의 로그 컨텍스트에 로그를 추가한다.
     *
     * @param logEntry 추가할 로그 객체
     */
    void addLog(LogEntry logEntry);

    /**
     * 현재 요청에서 수집된 모든 로그를 출력하고 컨텍스트를 초기화한다.
     * <p>
     * - 이 메서드는 요청이 종료될 때 호출되어야 하며, 로그를 저장소 또는 모니터링 시스템으로 전송하는 역할을 한다.
     * </p>
     */
    void flush();

    /**
     * 현재 요청의 로그 컨텍스트를 초기화한다.
     * <p>
     * - 컨텍스트를 초기화하면 해당 요청에서 기록된 모든 로그가 삭제된다.
     * - 요청 종료 시 불필요한 메모리 사용을 방지하기 위해 반드시 호출해야 한다.
     * </p>
     */
    void clear();

}
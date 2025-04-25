package com.monikit.jdbc.proxy;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 실행 시 사용되는 바인딩 파라미터를 저장하고 제공하는 클래스.
 *
 * <p>
 * 이 클래스는 JDBC 쿼리 실행 시 바인딩된 파라미터들을 {@code ThreadLocal}을 통해 저장하며,
 * SQL 로그 출력 시 {@code ?} 바인딩 값을 실제 값으로 변환하는 데 사용된다.
 * </p>
 *
 * <p>
 * {@code try-with-resources} 패턴을 사용하여 파라미터 저장소를 자동 초기화할 수 있으며,
 * 멀티스레드 환경에서도 안전하게 동작하도록 설계되어 있다.
 * </p>
 *
 * <pre>{@code
 * try (SqlParameterHolder holder = new SqlParameterHolder()) {
 *     holder.addParameter("john");
 *     holder.addParameter(100);
 *     String params = holder.getCurrentParameters(); // ["john", 100]
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class SqlParameterHolder implements AutoCloseable {

    /**
     * 각 스레드별로 SQL 바인딩 파라미터를 저장하는 {@code ThreadLocal}
     */
    private final ThreadLocal<List<Object>> parametersHolder = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 생성 시 내부 파라미터 목록을 초기화함.
     * <p>
     * 일반적으로 {@code try-with-resources}와 함께 사용되어 SQL 실행 전후로 자원을 안전하게 정리함.
     * </p>
     */

    public SqlParameterHolder() {
        parametersHolder.get().clear();
    }

    /**
     * SQL 실행 시 사용된 파라미터를 추가한다.
     *
     * @param parameter 바인딩된 파라미터 값
     */

    public void addParameter(Object parameter) {
        parametersHolder.get().add(parameter);
    }

    /**
     * 현재 스레드에 저장된 파라미터 목록을 문자열로 반환한다.
     *
     * @return 파라미터 목록 문자열 (예: {@code [1, "foo", null]})
     */

    public String getCurrentParameters() {
        return parametersHolder.get().toString();
    }

    /**
     * SQL 실행이 끝난 후 {@code ThreadLocal} 저장소를 정리한다.
     * <p>
     * 메모리 누수를 방지하기 위해 반드시 호출되어야 하며,
     * {@code try-with-resources}를 사용하는 것이 권장된다.
     * </p>
     */

    @Override
    public void close() {
        parametersHolder.get().clear();
    }

}

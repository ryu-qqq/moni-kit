package com.monikit.core;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 실행 시 사용되는 바인딩된 파라미터를 저장하고 제공하는 클래스.
 * <p>
 * - `ThreadLocal`을 사용하여 멀티스레드 환경에서도 안전하게 관리.
 * - `try-with-resources` 패턴을 적용하여 자동 초기화.
 * - SQL 로그 출력 시 `?`를 실제 바인딩된 값으로 변환 가능하도록 개선.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1
 */
public class SqlParameterHolder implements AutoCloseable {

    /**
     * 각 스레드별로 SQL 바인딩 파라미터를 저장하는 `ThreadLocal`
     */
    private static final ThreadLocal<List<Object>> parametersHolder = ThreadLocal.withInitial(ArrayList::new);

    /**
     * SQL 실행 시 `try-with-resources` 패턴을 활용하여 자동 초기화.
     */
    public SqlParameterHolder() {
        parametersHolder.get().clear(); // 초기화
    }

    /**
     * SQL 실행 시 사용된 파라미터를 추가.
     *
     * @param parameter 바인딩된 값
     */
    public static void addParameter(Object parameter) {
        parametersHolder.get().add(parameter);
    }

    /**
     * 현재 스레드의 SQL 실행에 사용된 모든 파라미터를 문자열로 반환.
     *
     * @return SQL 바인딩된 파라미터 목록 (예: `id=1 AND name='John'`)
     */
    public static String getFormattedParameters(String sql) {
        List<Object> parameters = parametersHolder.get();
        for (Object param : parameters) {
            sql = sql.replaceFirst("\\?", formatValue(param));
        }
        return sql;
    }

    /**
     * SQL 실행이 끝나면 `ThreadLocal`을 정리.
     */
    @Override
    public void close() {
        parametersHolder.get().clear();
    }

    /**
     * 파라미터 값을 SQL 로그 포맷에 맞게 변환.
     * - String: `'값'` (작은따옴표 추가)
     * - Number: 그대로 사용
     * - Null: `NULL` 문자열로 변환
     *
     * @param value 변환할 값
     * @return 변환된 SQL 값
     */
    private static String formatValue(Object value) {
        if (value == null) return "NULL";
        if (value instanceof String) return "'" + value + "'";
        return value.toString();
    }
}

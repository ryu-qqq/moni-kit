package com.monikit.core;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 실행 시 사용되는 바인딩된 파라미터를 저장하고 제공하는 클래스.
 * <p>
 * - `ThreadLocal`을 사용하여 멀티스레드 환경에서도 안전하게 관리.
 * - 쿼리 실행이 끝나면 자동으로 초기화해야 함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class SqlParameterHolder {

    /**
     * 각 스레드별로 SQL 바인딩 파라미터를 저장하는 `ThreadLocal`
     */
    private static final ThreadLocal<List<Object>> parametersHolder = ThreadLocal.withInitial(ArrayList::new);

    private SqlParameterHolder() {
    }

    /**
     * SQL 실행 시 사용된 파라미터를 추가.
     *
     * @param parameter 바인딩된 값
     */
    static void addParameter(Object parameter) {
        parametersHolder.get().add(parameter);
    }

    /**
     * 현재 스레드의 SQL 실행에 사용된 모든 파라미터를 문자열로 반환.
     *
     * @return 파라미터 목록 문자열
     */
    static String getCurrentParameters() {
        return parametersHolder.get().toString();
    }

    /**
     * SQL 실행이 끝나면 `ThreadLocal`을 정리.
     */
    static void clear() {
        parametersHolder.remove();
    }
}

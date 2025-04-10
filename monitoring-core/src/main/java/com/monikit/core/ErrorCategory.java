package com.monikit.core;


/**
 * 에러 로그 감지를 위한 범주형 분류 Enum.
 * <p>
 * 예외를 의미적으로 분류하기 위한 Enum으로 사용되었으나,
 * 1.1.0 이후 {@link ExceptionLog}에서 예외 클래스명을 기반으로 처리하도록 변경되었으며,
 * 이 Enum은 더 이상 사용되지 않습니다.
 * </p>
 *
 * @deprecated 1.1.0부터 {@link ExceptionLog}로 대체됨. 예외는 exceptionType 필드로 표현되며,
 * 외부 라이브러리 의존성을 줄이기 위해 의미적 분류 대신 예외 타입 문자열 기반으로 전환됨.
 */

@Deprecated
public enum ErrorCategory {

    APPLICATION_ERROR,
    DATABASE_PERMANENT_ERROR,
    DATABASE_TRANSIENT_ERROR,
    INBOUND_NETWORK_ERROR,
    OUTBOUND_NETWORK_ERROR,
    UNKNOWN_ERROR,

}

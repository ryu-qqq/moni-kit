package com.monikit.starter;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;



import com.monikit.core.ErrorCategory;


/**
 * 예외를 {@link ErrorCategory}로 분류하는 유틸리티 클래스.
 * <p>
 * 1.1.0 이후로는 더 이상 사용되지 않으며, {@link com.monikit.core.ExceptionLog}에서
 * 예외 타입명 기반으로 로그가 분류되므로 이 클래스는 deprecated 처리되었습니다.
 * </p>
 *
 * @deprecated {@link com.monikit.core.ExceptionLog} 사용 권장.
 * 예외 클래스명 기반 필터링이 가능해지면서 명시적 분류 Enum은 더 이상 필요하지 않음.
 */

@Deprecated
public class ErrorCategoryClassifier {

    /**
     * 주어진 예외를 적절한 {@link ErrorCategory}로 분류한다.
     * 더 이상 사용되지 않으며, 예외 클래스명을 그대로 사용하는 방식으로 전환되었다.
     *
     * @param exception 발생한 예외
     * @return 매핑된 ErrorCategory
     */

    public static ErrorCategory categorize(Throwable exception) {

        if (exception instanceof SocketTimeoutException ||
            exception instanceof UnknownHostException ||
            exception instanceof ConnectException) {
            return ErrorCategory.INBOUND_NETWORK_ERROR;
        }


        else if (exception instanceof RuntimeException) {
            return ErrorCategory.APPLICATION_ERROR;
        }

        return ErrorCategory.UNKNOWN_ERROR;
    }
}

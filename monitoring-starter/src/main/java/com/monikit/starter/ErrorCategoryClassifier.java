package com.monikit.starter;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.springframework.boot.web.server.WebServerException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.validation.BindException;

import com.monikit.core.ErrorCategory;

import feign.FeignException;
import jakarta.servlet.ServletException;

/**
 * 예외를 적절한 {@link ErrorCategory }로 분류하는 클래스.
 * <p>
 * 서비스, 저장소, 네트워크 등에서 발생한 예외를 분석하여 적절한 ErrorCategory를 반환한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class ErrorCategoryClassifier {

    /**
     * 주어진 예외를 적절한 {@link ErrorCategory}로 분류한다.
     *
     * @param exception 발생한 예외
     * @return 매핑된 ErrorCategory
     */
    public static ErrorCategory categorize(Throwable exception) {

        if (exception instanceof TransientDataAccessException) {
            return ErrorCategory.DATABASE_TRANSIENT_ERROR;
        }
        else if (exception instanceof NonTransientDataAccessException) {
            return ErrorCategory.DATABASE_PERMANENT_ERROR;
        }

        else if (exception instanceof SocketTimeoutException ||
            exception instanceof UnknownHostException ||
            exception instanceof ConnectException ||
            exception instanceof BindException ||
            exception instanceof WebServerException ||
            exception instanceof ServletException) {
            return ErrorCategory.INBOUND_NETWORK_ERROR;
        }

        // ✅ OUTBOUND NETWORK ERROR (우리 서버가 외부 API 호출할 때)
        else if (exception instanceof FeignException) {
            return ErrorCategory.OUTBOUND_NETWORK_ERROR;
        }

        // ✅ 구체적인 예외가 아니라면, 최종적으로 RuntimeException을 APPLICATION_ERROR로 처리
        else if (exception instanceof RuntimeException) {
            return ErrorCategory.APPLICATION_ERROR;
        }

        // ✅ 마지막까지 어떤 카테고리에도 해당되지 않으면 UNKNOWN_ERROR 처리
        return ErrorCategory.UNKNOWN_ERROR;
    }
}

package com.hnv.elearning.common.exception;

import com.hnv.elearning.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return buildResponse(errorCode);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.error("Authentication exception", ex);
        return buildResponse(ErrorCode.UNAUTHENTICATED);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode) {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }
}

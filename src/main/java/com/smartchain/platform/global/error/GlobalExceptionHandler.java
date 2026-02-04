package com.smartchain.platform.global.error;

import com.smartchain.platform.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = e.getData() != null
                ? ErrorResponse.of(errorCode, e.getData())
                : ErrorResponse.of(errorCode);
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("ValidationException: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
    }

    // Binding 예외 처리
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.error("BindException: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
    }

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
    }
}
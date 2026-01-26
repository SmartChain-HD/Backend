package com.smartchain.platform.dto.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    // v3.0: 에러 관련 필드 추가
    private String errorCode;
    private List<FieldErrorDto> fieldErrors;
    private String traceId;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // v3.0: 에러 코드 포함 응답
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // v3.0: 필드 에러 포함 응답
    public static <T> ApiResponse<T> validationError(String message, List<FieldErrorDto> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode("VAL_001")
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

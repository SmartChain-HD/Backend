package com.smartchain.platform.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "성공", data, LocalDateTime.now());
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> BaseResponse<T> success(String message) {
        return new BaseResponse<>(true, message, null, LocalDateTime.now());
    }

    public static <T> BaseResponse<T> fail(String message) {
        return new BaseResponse<>(false, message, null, LocalDateTime.now());
    }
}

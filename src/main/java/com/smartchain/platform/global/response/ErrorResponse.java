package com.smartchain.platform.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartchain.platform.global.error.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String code;
    private String message;
    private Object data;

    public ErrorResponse(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public ErrorResponse(int status, String code, String message, Object data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, Object data) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                data
        );
    }
}

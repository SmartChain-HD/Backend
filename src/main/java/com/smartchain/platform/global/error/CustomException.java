package com.smartchain.platform.global.error;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object data;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
    }

    public CustomException(ErrorCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
    }
}
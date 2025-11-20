package com.assist.common.common;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    private ErrorCode errorCode;
    private String message;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.message = errorCode.name();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}


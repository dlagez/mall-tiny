package com.roc.malltiny.common.exception;

import com.roc.malltiny.common.api.IErrorCode;

/**
 * 其实就是抛出RunTimeException
 */
public class ApiException extends RuntimeException{
    private IErrorCode errorCode;

    // 这些都是构造方法
    public ApiException(IErrorCode code) {
        super(code.getMessage());
        this.errorCode = code;
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public IErrorCode getErrorCode() {
        return errorCode;
    }
}

package com.sssukho.common.exception;


import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String errorMessageForResponse;

    public CustomException(ErrorCode errorCode, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
        this.errorMessageForResponse = errorCode.getDefaultErrorMessage();
    }

    public CustomException(ErrorCode errorCode, String errorMessageForResponse) {
        super(errorMessageForResponse);
        this.errorCode = errorCode;
        this.errorMessageForResponse = errorMessageForResponse;
    }

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getDefaultErrorMessage());
        this.errorCode = errorCode;
        this.errorMessageForResponse = errorCode.getDefaultErrorMessage();
    }
}

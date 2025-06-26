package com.sssukho.common.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public record ErrorResponseMessage(
    @JsonIgnore
    HttpStatus httpStatus,
    @JsonInclude(Include.NON_NULL)
    String errorCode,
    @JsonInclude(Include.NON_NULL)
    String errorMessage) {

    public static ErrorResponseMessage create(ErrorCode errorCode) {
        return new ErrorResponseMessage(errorCode.getHttpStatus(), errorCode.getErrorCode(),
            errorCode.getDefaultErrorMessage());
    }

    public static ErrorResponseMessage createCustom(ErrorCode errorCode,
        String customerErrorMessage) {
        return new ErrorResponseMessage(errorCode.getHttpStatus(), errorCode.getErrorCode(),
            customerErrorMessage);
    }

    @Getter
    public enum ErrorCode {
        INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "요청 파라미터가 유효하지 않습니다."),
        INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 본문이 유효하지 않습니다."),

        NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "NOT_FOUND_ROOM", "방을 찾을 수 없습니다."),
        NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "NOT_FOUND_MEMBER", "존재하지 않는 회원입니다."),
        NOT_FOUND_DEAL(HttpStatus.NOT_FOUND, "NOT_FOUND_DEAL", "거래를 찾을 수 없습니다."),

        NOT_FOUND_API(HttpStatus.NOT_FOUND, "NOT_FOUND_API", "요청하신 API는 존재하지 않습니다."),

        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 정보가 존재하지 않습니다."),

        FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),

        INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.")
        ;

        private final HttpStatus httpStatus;
        private final String errorCode;
        private final String defaultErrorMessage;

        ErrorCode(HttpStatus httpStatus, String errorCode, String defaultErrorMessage) {
            this.httpStatus = httpStatus;
            this.errorCode = errorCode;
            this.defaultErrorMessage = defaultErrorMessage;
        }
    }
}

package com.sssukho.api.handler;

import com.sssukho.common.dto.common.ErrorResponseMessage;
import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.exception.CustomException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MSG_FORMAT_PARAMETER_INVALID = "파라미터 '%s' 의 값이 유효하지 않습니다.";

    @ExceptionHandler(CustomException.class)
    private ResponseEntity<ErrorResponseMessage> handleCustomException(CustomException exception) {
        ErrorResponseMessage errorResponseMessage = ErrorResponseMessage.createCustom(exception.getErrorCode(),
            exception.getErrorMessageForResponse());

        return createResponseEntity(exception.getErrorCode(), errorResponseMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ErrorResponseMessage> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception) {

        log.debug("validation failed", exception);
        ErrorResponseMessage errorResponseMessage = ErrorResponseMessage.createCustom(
            ErrorCode.INVALID_PARAMETER,
            String.format(MSG_FORMAT_PARAMETER_INVALID, enumerateFields(exception)));

        return createResponseEntity(ErrorCode.INVALID_PARAMETER, errorResponseMessage);
    }

    private static String enumerateFields(MethodArgumentNotValidException exception) {
        StringBuilder fields = new StringBuilder();
        BindingResult bindingResult = exception.getBindingResult();
        bindingResult.getFieldErrors()
            .forEach(fieldError -> fields.append(fieldError.getField()).append(","));
        fields.deleteCharAt(fields.length() - 1);
        return fields.toString();
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class,
        HandlerMethodValidationException.class
    })
    private ResponseEntity<ErrorResponseMessage> handleInvalidParameterExceptions(
        Throwable throwable) {

        log.debug("invalid parameters", throwable);
        ErrorCode errorCodeForResponse = ErrorCode.INVALID_PARAMETER;
        return createResponseEntity(errorCodeForResponse,
            ErrorResponseMessage.create(errorCodeForResponse));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<ErrorResponseMessage> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException exception) {

        if (exception.getRootCause() instanceof CustomException) {
            return handleCustomException((CustomException) exception.getRootCause());
        }

        log.debug("Failed to read request", exception);

        ErrorCode errorCodeForResponse = ErrorCode.INVALID_REQUEST;

        return createResponseEntity(errorCodeForResponse,
            ErrorResponseMessage.create(errorCodeForResponse));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    private ResponseEntity<ErrorResponseMessage> handleNoResourceFoundException(
        NoResourceFoundException exception) {

        log.debug("Failed to find resource", exception);

        ErrorCode errorCodeForResponse = ErrorCode.NOT_FOUND_API;

        return createResponseEntity(errorCodeForResponse,
            ErrorResponseMessage.create(errorCodeForResponse));
    }

    @ExceptionHandler({RuntimeException.class, Exception.class})
    private ResponseEntity<ErrorResponseMessage> handleRuntimeException(
        Exception exception) {

        log.debug("Failed to process request", exception);
        ErrorResponseMessage errorResponseMessage = ErrorResponseMessage.create(ErrorCode.INTERNAL_SERVER_ERROR);
        return createResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR, errorResponseMessage);
    }

    private static ResponseEntity<ErrorResponseMessage> createResponseEntity(ErrorCode errorCode,
        ErrorResponseMessage errorResponseMessage) {
        return new ResponseEntity<>(errorResponseMessage, errorCode.getHttpStatus());
    }
}

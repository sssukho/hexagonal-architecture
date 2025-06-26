package com.sssukho.common.dto.common;

public record ResponseMessage<T>(
    T data
){

    public static <T> ResponseMessage<T> create(final T responseBody) {
        return new ResponseMessage<>(responseBody);
    }

}

package com.sssukho.common.dto.room;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.exception.CustomException;

public enum DealTypeDto {
    MONTHLY_RENT, // 월세
    YEAR_RENT; // 전세

    private static final String MSG_FORMAT_NOT_SUPPORTED = "%s 입력값 '%s' 은 지원하지 않습니다.";

    @JsonCreator
    public static DealTypeDto from(String input) {
        try {
            return valueOf(input.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER,
                String.format(MSG_FORMAT_NOT_SUPPORTED, "거래 유형", input));
        }
    }
}

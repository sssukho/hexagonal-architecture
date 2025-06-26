package com.sssukho.common.dto.room;

import java.math.BigDecimal;
import java.util.List;

public record RoomSearchRequest(
    List<RoomTypeDto> roomTypes,
    List<DealTypeDto> dealTypes,
    BigDecimal minDeposit,
    BigDecimal maxDeposit,
    BigDecimal minMonthlyRent,
    BigDecimal maxMonthlyRent,
    int page,
    int size
    ) {

}

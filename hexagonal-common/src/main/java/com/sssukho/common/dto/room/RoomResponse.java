package com.sssukho.common.dto.room;

import java.math.BigDecimal;
import java.util.List;

public record RoomResponse(
    Long id,
    String title,
    String description,
    String address,
    Double area,
    RoomTypeDto roomType,
    List<DealResponse> deals
    ) {

    public record DealResponse(
        DealTypeDto dealType,
        BigDecimal deposit,
        BigDecimal monthlyRent
    ) {

    }
}

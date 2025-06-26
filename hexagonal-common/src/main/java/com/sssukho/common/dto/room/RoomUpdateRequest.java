package com.sssukho.common.dto.room;

import java.math.BigDecimal;
import java.util.List;

public record RoomUpdateRequest(
    String title,
    String description,
    String address,
    Double area,
    RoomTypeDto roomType,
    List<DealUpdateRequest> deals
    ) {

    public record DealUpdateRequest(
        DealTypeDto dealType,
        BigDecimal deposit,
        BigDecimal monthlyRent
    ) {}


    public boolean isEmpty() {
        return title == null && description == null && address == null && area == null
            && roomType == null && (deals == null || deals.isEmpty());
    }
}

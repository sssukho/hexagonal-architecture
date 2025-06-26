package com.sssukho.common.dto.room;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record RoomRegistrationRequest (
    @NotBlank
    String title,
    String description,
    @NotBlank
    String address,
    Double area,
    @NotNull
    RoomTypeDto roomType,
    @Valid
    List<DealRegistrationRequest> deals
) {

    public record DealRegistrationRequest(
        @NotNull
        DealTypeDto dealType,
        BigDecimal deposit,
        BigDecimal monthlyRent
    ) {
    }
}

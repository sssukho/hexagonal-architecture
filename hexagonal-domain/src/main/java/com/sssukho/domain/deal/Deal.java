package com.sssukho.domain.deal;

import com.sssukho.common.dto.room.DealTypeDto;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Deal {
    private Long id;
    private DealTypeDto dealTypeDto;
    private BigDecimal deposit;
    private BigDecimal monthlyRent;
    private Long roomId;

    private Deal(DealTypeDto dealTypeDto, BigDecimal deposit, BigDecimal monthlyRent, Long roomId) {
        this.dealTypeDto = dealTypeDto;
        this.deposit = deposit;
        this.monthlyRent = monthlyRent;
        this.roomId = roomId;
    }

    private Deal(Long id, DealTypeDto dealTypeDto, BigDecimal deposit, BigDecimal monthlyRent,
        Long roomId) {
        this.id = id;
        this.dealTypeDto = dealTypeDto;
        this.deposit = deposit;
        this.monthlyRent = monthlyRent;
        this.roomId = roomId;
    }

    public static Deal of(DealTypeDto dealTypeDto, BigDecimal deposit, BigDecimal monthlyRent,
        Long roomId) {
        return new Deal(dealTypeDto, deposit, monthlyRent, roomId);
    }

    public static Deal of(Long id, DealTypeDto dealTypeDto, BigDecimal deposit,
        BigDecimal monthlyRent, Long roomId) {
        return new Deal(id, dealTypeDto, deposit, monthlyRent, roomId);
    }
}


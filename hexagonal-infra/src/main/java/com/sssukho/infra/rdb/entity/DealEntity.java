package com.sssukho.infra.rdb.entity;

import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DealEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealType dealType;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal deposit; // 보증금

    @Column(precision = 12, scale = 0)
    private BigDecimal monthlyRent; // 월세 (전세의 경우 null)

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    private DealEntity(DealType dealType, BigDecimal deposit, BigDecimal monthlyRent, Long roomId) {
        this.dealType = dealType;
        this.deposit = deposit;
        this.monthlyRent = monthlyRent;
        this.roomId = roomId;
    }

    public static DealEntity of(DealType dealType, BigDecimal deposit, BigDecimal monthlyRent,
        Long roomId) {
        return new DealEntity(dealType, deposit, monthlyRent, roomId);
    }

    public enum DealType {
        MONTHLY_RENT, YEAR_RENT;

        public static DealType from(String input) {
            try {
                return valueOf(input.toUpperCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new CustomException(ErrorCode.INVALID_PARAMETER,
                    "거래 유형 입력값 '" + input + "' 은 지원하지 않습니다.");
            }
        }
    }
}

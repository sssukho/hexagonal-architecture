package com.sssukho.infra.rdb.entity;


import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.exception.CustomException;
import com.sssukho.domain.room.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String address;
    private Double area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    // 연관관계 제거 - owner_id를 직접 관리
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private RoomEntity(String title, String description, String address, Double area, RoomType roomType,
        Long ownerId) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.area = area;
        this.roomType = roomType;
        this.ownerId = ownerId;
    }

    public static RoomEntity of(String title, String description, String address, Double area,
        RoomType roomType, Long ownerId) {
        return new RoomEntity(title, description, address, area, roomType, ownerId);
    }

    public void update(Room roomDomainToUpdate) {
        if (roomDomainToUpdate.getTitle() != null) {
            this.title = roomDomainToUpdate.getTitle();
        }

        if (roomDomainToUpdate.getDescription() != null) {
            this.description = roomDomainToUpdate.getDescription();
        }

        if (roomDomainToUpdate.getAddress() != null) {
            this.address = roomDomainToUpdate.getAddress();
        }

        if (roomDomainToUpdate.getArea() != null) {
            this.area = roomDomainToUpdate.getArea();
        }

        if (roomDomainToUpdate.getRoomTypeDto() != null) {
            this.roomType = RoomType.from(roomDomainToUpdate.getRoomTypeDto().name());
        }
    }

    public enum RoomType {
        ONE_ROOM, TWO_ROOM, THREE_ROOM;

        public static RoomType from(String input) {
            try {
                return valueOf(input.toUpperCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new CustomException(ErrorCode.INVALID_PARAMETER,
                    "방 유형 입력값 '" + input + "' 은 지원하지 않습니다.");
            }
        }
    }
}

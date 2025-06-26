package com.sssukho.domain.room;

import com.sssukho.common.dto.room.RoomTypeDto;
import com.sssukho.common.dto.room.RoomUpdateRequest;
import com.sssukho.domain.deal.Deal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Room {

    private Long id;
    private String title;
    private String description;
    private String address;
    private Double area;
    private RoomTypeDto roomTypeDto;
    private Long ownerId;
    private List<Deal> deals;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Room(Long id, String title, String description, String address, Double area,
        RoomTypeDto roomTypeDto, Long ownerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.address = address;
        this.area = area;
        this.roomTypeDto = roomTypeDto;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Room of(Long id, String title, String description, String address, Double area,
        RoomTypeDto roomTypeDto, Long ownerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Room(id, title, description, address, area, roomTypeDto, ownerId, createdAt,
            updatedAt);
    }

    public void setDeals(List<Deal> deals) {
        this.deals = new ArrayList<>(deals);
    }

    public void change(RoomUpdateRequest request) {
        if (request.title() != null) {
            this.title = request.title();
        }
        if (request.description() != null) {
            this.description = request.description();
        }
        if (request.address() != null) {
            this.address = request.address();
        }
        if (request.area() != null) {
            this.area = request.area();
        }
        if (request.roomType() != null) {
            this.roomTypeDto = request.roomType();
        }
    }
}

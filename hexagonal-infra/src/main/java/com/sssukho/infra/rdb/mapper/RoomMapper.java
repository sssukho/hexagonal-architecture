package com.sssukho.infra.rdb.mapper;


import com.sssukho.common.dto.room.RoomRegistrationRequest;
import com.sssukho.common.dto.room.RoomTypeDto;
import com.sssukho.domain.room.Room;
import com.sssukho.infra.rdb.entity.RoomEntity;
import com.sssukho.infra.rdb.entity.RoomEntity.RoomType;
import java.util.List;

public class RoomMapper {

    public static RoomEntity toEntity(Room domain) {
        return RoomEntity.of(domain.getTitle(), domain.getDescription(), domain.getAddress(),
            domain.getArea(), RoomType.from(domain.getRoomTypeDto().name()), domain.getOwnerId());
    }

    public static Room toDomain(RoomEntity entity) {
        return Room.of(entity.getId(), entity.getTitle(), entity.getDescription(),
            entity.getAddress(), entity.getArea(), RoomTypeDto.from(entity.getRoomType().name()),
            entity.getOwnerId(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public static Room toDomain(RoomRegistrationRequest request, Long ownerId) {
        return Room.of(null, request.title(), request.description(), request.address(),
            request.area(), request.roomType(), ownerId, null, null);
    }

    public static List<Room> toDomains(List<RoomEntity> foundRoomEntities) {
        return foundRoomEntities.stream().map(RoomMapper::toDomain).toList();
    }

    public static List<Room> toDomainsFromNativeQueryList(List<Object[]> dataQueryResults) {
        return dataQueryResults.stream().map(RoomMapper::toDomainFromRow).toList();
    }

    private static Room toDomainFromRow(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String title = (String) row[1];
        String description = (String) row[2];
        String address = (String) row[3];
        Double area = row[4] != null ? ((Number) row[4]).doubleValue() : null;
        String roomTypeStr = (String) row[5];
        RoomTypeDto roomTypeDto = RoomTypeDto.valueOf(roomTypeStr);
        Long ownerId = ((Number) row[0]).longValue();

        return Room.of(id, title, description, address, area, roomTypeDto, ownerId, null, null);
    }


}

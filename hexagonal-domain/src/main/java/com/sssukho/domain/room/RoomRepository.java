package com.sssukho.domain.room;

import com.sssukho.common.dto.room.RoomSearchRequest;
import java.util.List;

public interface RoomRepository {

    Room save(Room roomToSave);

    Room findById(Long roomId);

    void deleteById(Long roomId);

    Room update(Room roomToUpdate);

    List<Room> findAllByOwnerId(Long ownerId);

    List<Room> searchRooms(RoomSearchRequest roomSearchRequest);
}


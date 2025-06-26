package com.sssukho.infra.rdb.repository;


import com.sssukho.infra.rdb.entity.RoomEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomJpaRepository extends JpaRepository<RoomEntity, Long> {

    List<RoomEntity> findAllByOwnerId(long ownerId);
}

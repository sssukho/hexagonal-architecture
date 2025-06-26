package com.sssukho.infra.rdb.repository;


import com.sssukho.infra.rdb.entity.DealEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealJpaRepository extends JpaRepository<DealEntity, Long> {

    @Modifying
    @Query(value = "DELETE FROM deal WHERE room_id = :roomIdToDelete", nativeQuery = true)
    void deleteAllByRoomId(@Param("roomIdToDelete") Long roomIdToDelete);

    List<DealEntity> findByRoomId(Long roomIdToFind);

    List<DealEntity> findByRoomIdIn(List<Long> roomIdsToFind);
}

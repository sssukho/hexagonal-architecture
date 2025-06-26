package com.sssukho.infra.rdb.repository;


import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.dto.room.DealTypeDto;
import com.sssukho.common.dto.room.RoomSearchRequest;
import com.sssukho.common.dto.room.RoomTypeDto;
import com.sssukho.common.exception.CustomException;
import com.sssukho.domain.room.Room;
import com.sssukho.domain.room.RoomRepository;
import com.sssukho.infra.rdb.entity.RoomEntity;
import com.sssukho.infra.rdb.mapper.RoomMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepository {

    private final RoomJpaRepository roomJpaRepository;
    private final EntityManager entityManager;

    @Override
    public Room save(Room roomDomainToSave) {
        RoomEntity entityToSave = RoomMapper.toEntity(roomDomainToSave);
        RoomEntity savedRoomEntity = roomJpaRepository.save(entityToSave);
        return RoomMapper.toDomain(savedRoomEntity);
    }

    @Override
    public Room findById(Long roomId) {
        RoomEntity foundRoomEntity = roomJpaRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROOM));
        return RoomMapper.toDomain(foundRoomEntity);
    }

    @Override
    public void deleteById(Long roomId) {
        roomJpaRepository.deleteById(roomId);
    }

    @Override
    public Room update(Room roomToUpdate) {
        RoomEntity roomEntity = roomJpaRepository.findById(roomToUpdate.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROOM));
        roomEntity.update(roomToUpdate);
        return RoomMapper.toDomain(roomEntity);
    }

    @Override
    public List<Room> findAllByOwnerId(Long ownerId) {
        List<RoomEntity> foundRoomEntities = roomJpaRepository.findAllByOwnerId(ownerId);
        return RoomMapper.toDomains(foundRoomEntities);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Room> searchRooms(RoomSearchRequest roomSearchRequest) {
        // 동적 쿼리 조건 구성
        Map<String, Object> parameters = new HashMap<>();
        String whereClause = createWhereClause(roomSearchRequest, parameters);

        // 데이터 조회 쿼리
        String dataQuery = """
            SELECT DISTINCT
                r.id as room_id,
                r.title,
                r.description,
                r.address,
                r.area,
                r.room_type,
                r.owner_id,
                r.created_at
            FROM room r
            INNER JOIN deal d ON r.id = d.room_id
            """ + whereClause + """
            ORDER BY r.created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        // 데이터 조회 실행
        Query dataQueryExecution = entityManager.createNativeQuery(dataQuery);
        parameters.forEach(dataQueryExecution::setParameter);
        dataQueryExecution.setParameter("limit", roomSearchRequest.size());
        dataQueryExecution.setParameter("offset", roomSearchRequest.page() * roomSearchRequest.size());

        return RoomMapper.toDomainsFromNativeQueryList(dataQueryExecution.getResultList());
    }

    private static String createWhereClause(RoomSearchRequest request, Map<String, Object> parameters) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        // 방 유형 IN 조건
        if (request.roomTypes() != null && !request.roomTypes().isEmpty()) {
            whereClause.append(" AND r.room_type IN (:roomTypes) ");
            parameters.put("roomTypes",
                request.roomTypes().stream().map(RoomTypeDto::name).toList());
        }

        // 거래 유형 IN 조건
        if (request.dealTypes() != null && !request.dealTypes().isEmpty()) {
            whereClause.append(" AND d.deal_type IN (:dealTypes) ");
            parameters.put("dealTypes",
                request.dealTypes().stream().map(DealTypeDto::name).toList());
        }

        // 보증금 최소값 조건
        if (request.minDeposit() != null) {
            whereClause.append(" AND d.deposit >= :minDeposit ");
            parameters.put("minDeposit", request.minDeposit());
        }

        // 보증금 최대값 조건
        if (request.maxDeposit() != null) {
            whereClause.append(" AND d.deposit <= :maxDeposit ");
            parameters.put("maxDeposit", request.maxDeposit());
        }

        // 월세 최소값 조건
        if (request.minMonthlyRent() != null) {
            whereClause.append(" AND d.monthly_rent >= :minMonthlyRent ");
            parameters.put("minMonthlyRent", request.minMonthlyRent());
        }

        // 월세 최대값 조건
        if (request.maxMonthlyRent() != null) {
            whereClause.append(" AND d.monthly_rent <= :maxMonthlyRent ");
            parameters.put("maxMonthlyRent", request.maxMonthlyRent());
        }
        return whereClause.toString();
    }
}

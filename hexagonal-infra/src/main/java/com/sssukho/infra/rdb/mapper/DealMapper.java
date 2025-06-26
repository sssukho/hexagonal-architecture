package com.sssukho.infra.rdb.mapper;


import com.sssukho.common.dto.room.DealTypeDto;
import com.sssukho.common.dto.room.RoomRegistrationRequest.DealRegistrationRequest;
import com.sssukho.common.dto.room.RoomUpdateRequest.DealUpdateRequest;
import com.sssukho.domain.deal.Deal;
import com.sssukho.infra.rdb.entity.DealEntity;
import com.sssukho.infra.rdb.entity.DealEntity.DealType;
import java.util.List;

public class DealMapper {

    public static DealEntity toEntity(Deal domain) {
        DealType dealTypeInEntity = DealType.valueOf(domain.getDealTypeDto().name());
        return DealEntity.of(
            dealTypeInEntity,
            domain.getDeposit(),
            domain.getMonthlyRent(),
            domain.getRoomId());
    }

    public static List<DealEntity> toEntities(List<Deal> domains) {
        return domains.stream().map(DealMapper::toEntity).toList();
    }

    public static Deal toDomain(DealEntity entity) {
        DealTypeDto dealTypeDto = DealTypeDto.from(entity.getDealType().name());
        return Deal.of(
            entity.getId(),
            dealTypeDto,
            entity.getDeposit(),
            entity.getMonthlyRent(),
            entity.getRoomId());
    }

    public static List<Deal> toDomains(List<DealEntity> entities) {
        return entities.stream().map(DealMapper::toDomain).toList();
    }

    private static Deal toDomain(DealRegistrationRequest request, Long roomId) {
        return Deal.of(request.dealType(), request.deposit(), request.monthlyRent(), roomId);
    }

    public static List<Deal> toDomainsFromRegistrationRequests(List<DealRegistrationRequest> requests, Long roomId) {
        return requests.stream().map(request -> toDomain(request, roomId)).toList();
    }

    private static Deal toDomain(DealUpdateRequest request, Long roomId) {
        return Deal.of(request.dealType(), request.deposit(), request.monthlyRent(), roomId);
    }

    public static List<Deal> toDomainsFromUpdateRequests(List<DealUpdateRequest> requests, Long roomId) {
        return requests.stream().map(request -> toDomain(request, roomId)).toList();
    }



}

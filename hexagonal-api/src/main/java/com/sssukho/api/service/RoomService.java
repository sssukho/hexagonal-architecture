package com.sssukho.api.service;

import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.dto.room.RoomRegistrationRequest;
import com.sssukho.common.dto.room.RoomResponse;
import com.sssukho.common.dto.room.RoomResponse.DealResponse;
import com.sssukho.common.dto.room.RoomSearchRequest;
import com.sssukho.common.dto.room.RoomUpdateRequest;
import com.sssukho.common.exception.CustomException;
import com.sssukho.domain.deal.Deal;
import com.sssukho.domain.member.Member;
import com.sssukho.domain.room.Room;
import com.sssukho.domain.room.RoomRepository;
import com.sssukho.infra.rdb.mapper.DealMapper;
import com.sssukho.infra.rdb.mapper.RoomMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final MemberService memberService;
    private final DealService dealService;

    @Transactional(rollbackFor = Exception.class)
    public RoomResponse register(RoomRegistrationRequest request) {
        Member currentMember = extractCurrentMemberFromSecurityContext();

        Room roomToRegister = RoomMapper.toDomain(request, currentMember.getId());
        Room registeredRoom = roomRepository.save(roomToRegister);

        List<Deal> deals = DealMapper.toDomainsFromRegistrationRequests(request.deals(),
            registeredRoom.getId());

        dealService.registerAll(deals);
        registeredRoom.setDeals(deals);

        return toRoomResponse(registeredRoom);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMyRoom(Long roomIdToDelete) {
        // room 에 속한 deal 도 모두 삭제해야함
        Member currentMember = extractCurrentMemberFromSecurityContext();
        Room foundRoom = roomRepository.findById(roomIdToDelete);

        validateOwnerOrThrow(currentMember, foundRoom);

        dealService.deleteByRoom(foundRoom);
        roomRepository.deleteById(foundRoom.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public RoomResponse updateMyRoom(Long roomIdToUpdate, RoomUpdateRequest request) {
        if (request.isEmpty()) {
            log.debug("All members are null in RoomUpdateRequest");
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Member currentMember = extractCurrentMemberFromSecurityContext();
        Room foundRoom = roomRepository.findById(roomIdToUpdate);

        validateOwnerOrThrow(currentMember, foundRoom);
        foundRoom.change(request);

        Room updatedRoom = roomRepository.update(foundRoom);

        // deal 변경 필요 여부
        if (!request.deals().isEmpty()) {
            // 기존 deal 삭제
            dealService.deleteByRoom(foundRoom);
            // 새로운 deal 저장
            List<Deal> dealsToChange = DealMapper.toDomainsFromUpdateRequests(request.deals(),
                foundRoom.getId());
            dealService.registerAll(dealsToChange);
            updatedRoom.setDeals(dealsToChange);
        }

        return toRoomResponse(updatedRoom);
    }

    public RoomResponse findMyRoom(Long roomIdToFind) {
        Member currentMember = extractCurrentMemberFromSecurityContext();
        Room room = roomRepository.findById(roomIdToFind);
        if (!room.getOwnerId().equals(currentMember.getId())) {
            throw new CustomException(ErrorCode.NOT_FOUND_ROOM);
        }

        // room 에 속한 deal
        List<Deal> dealsInRoom = dealService.findAllDealsByRoomId(room.getId());
        room.setDeals(dealsInRoom);

        return toRoomResponse(room);
    }

    public List<RoomResponse> findMyRooms() {
        Member currentMember = extractCurrentMemberFromSecurityContext();

        List<Room> foundMyRooms = roomRepository.findAllByOwnerId(currentMember.getId());
        List<Long> roomIds = foundMyRooms.stream().map(Room::getId).toList();

        List<Deal> foundDeals = dealService.findAllDealsByRoomIds(roomIds);

        attachDealsToRooms(foundDeals, foundMyRooms);

        return foundMyRooms.stream().map(this::toRoomResponse).toList();
    }

    public List<RoomResponse> search(RoomSearchRequest request) {
        List<Room> foundRooms = roomRepository.searchRooms(request);
        List<Long> roomIds = foundRooms.stream().map(Room::getId).toList();

        List<Deal> foundDeals = dealService.findAllDealsByRoomIds(roomIds);

        attachDealsToRooms(foundDeals, foundRooms);

        return foundRooms.stream().map(this::toRoomResponse).toList();
    }

    private Member extractCurrentMemberFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        try {
            return memberService.findByEmail(email);
        } catch (CustomException e) {
            log.warn("토큰에 포함된 사용자 정보가 DB에 존재하지 않습니다");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateOwnerOrThrow(Member currentMember, Room foundRoom) {
        if (!currentMember.getId().equals(foundRoom.getOwnerId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "방을 삭제할 권한이 없습니다.");
        }
    }

    private RoomResponse toRoomResponse(Room room) {
        List<DealResponse> dealResponses = room.getDeals().stream().map(
            dealDomain -> new DealResponse(dealDomain.getDealTypeDto(), dealDomain.getDeposit(),
                dealDomain.getMonthlyRent())).toList();

        return new RoomResponse(room.getId(), room.getTitle(), room.getDescription(),
            room.getAddress(), room.getArea(), room.getRoomTypeDto(), dealResponses);
    }

    private void attachDealsToRooms(List<Deal> foundDeals, List<Room> foundRooms) {
        Map<Long, List<Deal>> dealsByRoomId = foundDeals.stream()
            .collect(Collectors.groupingBy(Deal::getRoomId));

        // room 에 속한 deal
        for (Room foundRoom : foundRooms) {
            List<Deal> dealsInRoom = dealsByRoomId.get(foundRoom.getId());
            foundRoom.setDeals(dealsInRoom);
        }
    }
}

package com.sssukho.api.controller;

import com.sssukho.api.service.RoomService;
import com.sssukho.common.dto.common.ResponseMessage;
import com.sssukho.common.dto.room.DealTypeDto;
import com.sssukho.common.dto.room.RoomRegistrationRequest;
import com.sssukho.common.dto.room.RoomResponse;
import com.sssukho.common.dto.room.RoomSearchRequest;
import com.sssukho.common.dto.room.RoomTypeDto;
import com.sssukho.common.dto.room.RoomUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * 내방 등록
     */
    @PostMapping
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseMessage<RoomResponse> registerRoom(
        @Valid @RequestBody RoomRegistrationRequest request) {

        RoomResponse result = roomService.register(request);
        return ResponseMessage.create(result);
    }

    /**
     * 내방 삭제
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable("id") @NotNull Long id) {
        roomService.deleteMyRoom(id);
    }

    /**
     * 내방 수정 (PATCH)
     * - 변경이 핊요한 데이터만 있음
     */
    @PatchMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseMessage<RoomResponse> updateRoom(
        @PathVariable("id") @NotNull Long id,
        @Valid @RequestBody RoomUpdateRequest request) {

        RoomResponse result = roomService.updateMyRoom(id, request);
        return ResponseMessage.create(result);
    }

    /**
     * 내방 단건 조회
     */
    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseMessage<RoomResponse> findMyRoom(@PathVariable("id") @NotNull Long id) {
        RoomResponse result = roomService.findMyRoom(id);
        return ResponseMessage.create(result);
    }

    /**
     * 내방 목록 조회
     */
    @GetMapping("/my")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseMessage<List<RoomResponse>> findMyRooms() {
        List<RoomResponse> result = roomService.findMyRooms();
        return ResponseMessage.create(result);
    }

    /**
     * 전체방 목록 조회
     */
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseMessage<List<RoomResponse>> searchRooms(
        @RequestParam(required = false, name = "roomTypes") List<String> roomTypes,
        @RequestParam(required = false, name = "dealTypes") List<String> dealTypes,
        @RequestParam(required = false, name = "minDeposit") @DecimalMin(value = "0") BigDecimal minDeposit,
        @RequestParam(required = false, name = "maxDeposit") @DecimalMin(value = "0") BigDecimal maxDeposit,
        @RequestParam(required = false, name = "minMonthlyRent") @DecimalMin(value = "0") BigDecimal minMonthlyRent,
        @RequestParam(required = false, name = "maxMonthlyRent") @DecimalMin(value = "0") BigDecimal maxMonthlyRent,
        @RequestParam(defaultValue = "0", name = "page") @Min(0) int page,
        @RequestParam(defaultValue = "20", name = "size") @Min(0) @Max(100) int size) {

        List<RoomTypeDto> roomTypeDtos = null;
        if (roomTypes != null && !roomTypes.isEmpty()) {
            roomTypeDtos = roomTypes.stream().map(RoomTypeDto::from).toList();
        }

        List<DealTypeDto> dealTypeDtos = null;
        if (dealTypes != null && !dealTypes.isEmpty()) {
            dealTypeDtos = dealTypes.stream().map(DealTypeDto::from).toList();
        }

        RoomSearchRequest request = new RoomSearchRequest(roomTypeDtos, dealTypeDtos, minDeposit,
            maxDeposit, minMonthlyRent, maxMonthlyRent, page, size);

        List<RoomResponse> result = roomService.search(request);
        return ResponseMessage.create(result);
    }
}


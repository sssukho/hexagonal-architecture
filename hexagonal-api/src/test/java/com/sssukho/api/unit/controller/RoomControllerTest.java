package com.sssukho.api.unit.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sssukho.api.controller.RoomController;
import com.sssukho.api.security.JwtAuthenticationFilter;
import com.sssukho.api.service.RoomService;
import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.dto.room.DealTypeDto;
import com.sssukho.common.dto.room.RoomRegistrationRequest;
import com.sssukho.common.dto.room.RoomResponse;
import com.sssukho.common.dto.room.RoomResponse.DealResponse;
import com.sssukho.common.dto.room.RoomSearchRequest;
import com.sssukho.common.dto.room.RoomTypeDto;
import com.sssukho.common.dto.room.RoomUpdateRequest;
import com.sssukho.common.dto.room.RoomUpdateRequest.DealUpdateRequest;
import com.sssukho.common.exception.CustomException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(controllers = RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("내방 등록 요청 성공 테스트")
    @Test
    void testRegisterRoomSucceed() throws Exception {
        // given
        RoomRegistrationRequest.DealRegistrationRequest fakeDealRequest =
            new RoomRegistrationRequest.DealRegistrationRequest(
                DealTypeDto.MONTHLY_RENT,
                new BigDecimal("10000000"),
                new BigDecimal("500000")
            );

        RoomRegistrationRequest fakeRoomRegistrationRequest = new RoomRegistrationRequest(
            "타이틀 등록 요청",
            "설명 등록 요청",
            "주소 등록 요청",
            20.5,
            RoomTypeDto.ONE_ROOM,
            List.of(fakeDealRequest)
        );

        DealResponse fakeDealResponse = new DealResponse(DealTypeDto.MONTHLY_RENT,
            BigDecimal.valueOf(fakeDealRequest.deposit().longValue()),
            BigDecimal.valueOf(fakeDealRequest.monthlyRent().longValue()));

        RoomResponse fakeRoomResponse = new RoomResponse(1L, fakeRoomRegistrationRequest.title(),
            fakeRoomRegistrationRequest.description(), fakeRoomRegistrationRequest.address(),
            fakeRoomRegistrationRequest.area(), fakeRoomRegistrationRequest.roomType(),
            List.of(fakeDealResponse));

        when(roomService.register(fakeRoomRegistrationRequest))
            .thenReturn(fakeRoomResponse);

        // when
        ResultActions result = mockMvc.perform(post("/rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(fakeRoomRegistrationRequest)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(fakeRoomResponse.id()))
            .andExpect(jsonPath("$.data.title").value(fakeRoomResponse.title()))
            .andExpect(jsonPath("$.data.description").value(fakeRoomResponse.description()))
            .andExpect(jsonPath("$.data.address").value(fakeRoomResponse.address()))
            .andExpect(jsonPath("$.data.area").value(fakeRoomResponse.area()))
            .andExpect(jsonPath("$.data.deals").isArray());
    }

    @DisplayName("내방 등록 요청 실패 테스트")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRegisterRoomRequestProvider")
    void testRegisterRoomFailed(String description, RoomRegistrationRequest request) throws Exception {
        // when
        ResultActions result = mockMvc.perform(
            post("/rooms").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_PARAMETER.getErrorCode()));
    }

    static Stream<Arguments> invalidRegisterRoomRequestProvider() {
        RoomRegistrationRequest.DealRegistrationRequest validDeal =
            new RoomRegistrationRequest.DealRegistrationRequest(
                DealTypeDto.MONTHLY_RENT,
                new BigDecimal("10000000"),
                new BigDecimal("500000")
            );

        return Stream.of(
            Arguments.of("제목이 공백인 경우",
                new RoomRegistrationRequest("", "설명", "주소", 20.5, RoomTypeDto.ONE_ROOM, List.of(validDeal))),
            Arguments.of("제목이 null인 경우",
                new RoomRegistrationRequest(null, "설명", "주소", 20.5, RoomTypeDto.ONE_ROOM, List.of(validDeal))),
            Arguments.of("주소가 공백인 경우",
                new RoomRegistrationRequest("제목", "설명", "", 20.5, RoomTypeDto.ONE_ROOM, List.of(validDeal))),
            Arguments.of("주소가 null인 경우",
                new RoomRegistrationRequest("제목", "설명", null, 20.5, RoomTypeDto.ONE_ROOM, List.of(validDeal))),
            Arguments.of("방 타입이 null인 경우",
                new RoomRegistrationRequest("제목", "설명", "주소", 20.5, null, List.of(validDeal))),
            Arguments.of("거래 정보의 타입이 null인 경우",
                new RoomRegistrationRequest("제목", "설명", "주소", 20.5, RoomTypeDto.ONE_ROOM,
                    List.of(new RoomRegistrationRequest.DealRegistrationRequest(null, new BigDecimal("10000000"), new BigDecimal("500000")))))
        );
    }

    @DisplayName("내방 삭제 요청 성공 테스트")
    @Test
    void testDeleteRoomSucceed() throws Exception {
        // given
        long fakeRoomIdToDelete = 1L;

        doNothing().when(roomService).deleteMyRoom(fakeRoomIdToDelete);

        // when
        ResultActions result = mockMvc.perform(delete("/rooms/" + fakeRoomIdToDelete));

        // then
        result.andExpect(status().isNoContent());
    }

    @DisplayName("내방 삭제 요청 실패 테스트 - 내방이 아닐 때")
    @Test
    void testDeleteRoomFailedWhenRoomIsNotMine() throws Exception {
        // given
        long fakeRoomIdToDelete = 1L;

        doThrow(new CustomException(ErrorCode.FORBIDDEN)).when(roomService)
            .deleteMyRoom(fakeRoomIdToDelete);

        // when
        ResultActions result = mockMvc.perform(delete("/rooms/" + fakeRoomIdToDelete));

        // then
        result.andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.FORBIDDEN.getErrorCode()));
    }

    @DisplayName("내방 삭제 요청 실패 테스트 - 삭제 대상 room 의 id 가 파라미터에 없을 때")
    @Test
    void testDeleteRoomFailedWhenRoomIdParameterIsNull() throws Exception {
        // when
        ResultActions result = mockMvc.perform(delete("/rooms/"));

        // then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_API.getErrorCode()));
    }

    @DisplayName("내방 수정 요청 성공 테스트")
    @Test
    void testUpdateRoomSucceed() throws Exception {
        // given
        final long fakeRoomId = 1L;
        DealUpdateRequest fakeDealRequest = new DealUpdateRequest(DealTypeDto.YEAR_RENT,
            BigDecimal.valueOf(1000000000), BigDecimal.valueOf(20));
        RoomUpdateRequest fakeUpdateRequest = new RoomUpdateRequest(
            "수정 요청 타이틀",
            "수정 요청 설명",
            "수정 요청 주소",
            45.5,
            RoomTypeDto.THREE_ROOM,
            List.of(fakeDealRequest)
        );

        DealResponse fakeDealResponse = new DealResponse(fakeDealRequest.dealType(),
            fakeDealRequest.deposit(), fakeDealRequest.monthlyRent());

        RoomResponse fakeResponse = new RoomResponse(fakeRoomId, fakeUpdateRequest.title(),
            fakeUpdateRequest.description(), fakeUpdateRequest.address(),
            fakeUpdateRequest.area(), fakeUpdateRequest.roomType(), List.of(fakeDealResponse));

        when(roomService.updateMyRoom(fakeRoomId, fakeUpdateRequest)).thenReturn(fakeResponse);

        // when
        ResultActions result = mockMvc.perform(
            patch("/rooms/" + fakeRoomId).contentType(MediaType.APPLICATION_JSON).content(
                objectMapper.writeValueAsBytes(fakeUpdateRequest)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(fakeRoomId))
            .andExpect(jsonPath("$.data.title").value(fakeUpdateRequest.title()))
            .andExpect(jsonPath("$.data.description").value(fakeUpdateRequest.description()))
            .andExpect(jsonPath("$.data.address").value(fakeUpdateRequest.address()))
            .andExpect(jsonPath("$.data.area").value(fakeUpdateRequest.area()))
            .andExpect(jsonPath("$.data.deals").isArray())
        ;
    }

    @DisplayName("내방 수정 요청 실패 테스트")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateRoomRequestProvider")
    void testUpdateRoomFailed(String description, RoomUpdateRequest request) throws Exception {
        // given
        long fakeRoomId = 1L;

        // when
        ResultActions result = mockMvc.perform(
            patch("/rooms/" + fakeRoomId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_REQUEST.getErrorCode()));

    }

    static Stream<Arguments> invalidUpdateRoomRequestProvider() {
        return Stream.of(
            Arguments.of("request body 데이터가 null 인 경우", null)
        );
    }

    @DisplayName("내방 단건 조회 성공 테스트")
    @Test
    void testFindMyRoomSucceed() throws Exception{
        // given
        long fakeRoomId = 1L;

        DealResponse fakeDealResponse = new DealResponse(DealTypeDto.YEAR_RENT,
            BigDecimal.valueOf(1000000000), BigDecimal.valueOf(100000));

        RoomResponse fakeResponse = new RoomResponse(fakeRoomId, "내방 타이틀", "내방 설명", "내방 주소", 45.45,
            RoomTypeDto.THREE_ROOM, List.of(fakeDealResponse));

        when(roomService.findMyRoom(fakeRoomId)).thenReturn(fakeResponse);

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + fakeRoomId));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(fakeRoomId))
            .andExpect(jsonPath("$.data.title").value(fakeResponse.title()))
            .andExpect(jsonPath("$.data.description").value(fakeResponse.description()))
            .andExpect(jsonPath("$.data.address").value(fakeResponse.address()))
            .andExpect(jsonPath("$.data.area").value(fakeResponse.area()))
            .andExpect(jsonPath("$.data.deals").isArray());
    }

    @DisplayName("내방 단건 조회 실패 테스트")
    @Test
    void testFindMyRoomFailed() throws Exception {
        // given
        long fakeRoomId = 1L;

        doThrow(new CustomException(ErrorCode.FORBIDDEN)).when(roomService)
            .findMyRoom(fakeRoomId);

        // when
        ResultActions result = mockMvc.perform(get("/rooms/" + fakeRoomId));

        // then
        result.andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.FORBIDDEN.getErrorCode()));

    }

    @DisplayName("전체방 목록 조회 성공 테스트")
    @Test
    void testSearchRoomsSucceed() throws Exception {
        // given
        List<RoomResponse> fakeResponses = List.of(
            new RoomResponse(1L, "타이틀1", "설명1", "주소1", 45.5, RoomTypeDto.THREE_ROOM,
                List.of(new DealResponse(DealTypeDto.YEAR_RENT, BigDecimal.valueOf(1000000000), BigDecimal.valueOf(100000)))),
            new RoomResponse(2L, "타이틀2", "설명2", "집주소2", 35.5, RoomTypeDto.TWO_ROOM,
                List.of(new DealResponse(DealTypeDto.MONTHLY_RENT, BigDecimal.valueOf(200000000), BigDecimal.valueOf(500000))))
        );

        when(roomService.search(any(RoomSearchRequest.class))).thenReturn(fakeResponses);

        // when
        ResultActions result = mockMvc.perform(get("/rooms")
            .param("roomType", "one_room")
            .param("dealTypes", "MONTHLY_RENT")
            .param("minDeposit", "5000000")
            .param("maxDeposit", "20000000")
            .param("minMonthlyRent", "300000")
            .param("maxMonthlyRent", "1000000")
            .param("page", "0")
            .param("size", "10"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(fakeResponses.get(0).id()))
            .andExpect(jsonPath("$.data[1].id").value(fakeResponses.get(1).id()));
    }

    @DisplayName("전체방 목록 조회 실패 테스트")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRoomSearchRequestProvider")
    void testSearchRoomsFailed(String description, RoomSearchRequest request) throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = get("/rooms");
        if (request.roomTypes() != null) {
            request.roomTypes()
                .forEach(roomType -> requestBuilder.param("roomTypes", roomType.name()));
        }
        if (request.dealTypes() != null) {
            request.dealTypes()
                .forEach(dealType -> requestBuilder.param("dealTypes", dealType.name()));
        }
        if (request.minDeposit() != null) {
            requestBuilder.param("minDeposit", request.minDeposit().toPlainString());
        }

        if (request.maxDeposit() != null) {
            requestBuilder.param("maxDeposit", request.maxDeposit().toPlainString());
        }

        if (request.minMonthlyRent() != null) {
            requestBuilder.param("minMonthlyRent", request.minMonthlyRent().toPlainString());
        }

        if (request.maxMonthlyRent() != null) {
            requestBuilder.param("maxMonthlyRent", request.maxMonthlyRent().toPlainString());
        }

        requestBuilder.param("page", String.valueOf(request.page()));
        requestBuilder.param("size", String.valueOf(request.size()));

        // when
        ResultActions result = mockMvc.perform(requestBuilder);

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_PARAMETER.getErrorCode()));
    }

    static Stream<Arguments> invalidRoomSearchRequestProvider() {

        // minDeposit 이 0 미만
        RoomSearchRequest req1 = new RoomSearchRequest(null, null, BigDecimal.valueOf(-1), null,
            null, null, 0, 20);
        // minMonthlyRent 가 0 미만
        RoomSearchRequest req2 = new RoomSearchRequest(null, null, null, null,
            BigDecimal.valueOf(-100), null, 0, 20);
        // page 가 음수
        RoomSearchRequest req3 = new RoomSearchRequest(null, null, null, null, null, null, -2, 10);
        // size 가 음수
        RoomSearchRequest req4 = new RoomSearchRequest(null, null, null, null, null, null, 0, -5);

        return Stream.of(
            Arguments.of("minDeposit 이 0 미만", req1),
            Arguments.of("minMonthlyRent 가 0 미만", req2),
            Arguments.of("page 가 음수", req3),
            Arguments.of("size 가 음수", req4));
    }
}

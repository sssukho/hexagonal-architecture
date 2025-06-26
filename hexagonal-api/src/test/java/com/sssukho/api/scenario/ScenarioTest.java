package com.sssukho.api.scenario;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sssukho.api.config.properties.SecurityConfigurationProperties;
import com.sssukho.api.security.JwtTokenProvider;
import com.sssukho.common.dto.auth.RefreshTokenRequest;
import com.sssukho.common.dto.auth.SignInRequest;
import com.sssukho.common.dto.auth.SignInResponse;
import com.sssukho.common.dto.auth.SignUpRequest;
import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.dto.common.ResponseMessage;
import com.sssukho.common.dto.room.DealTypeDto;
import com.sssukho.common.dto.room.RoomRegistrationRequest;
import com.sssukho.common.dto.room.RoomRegistrationRequest.DealRegistrationRequest;
import com.sssukho.common.dto.room.RoomResponse;
import com.sssukho.common.dto.room.RoomTypeDto;
import com.sssukho.common.dto.room.RoomUpdateRequest;
import com.sssukho.common.dto.room.RoomUpdateRequest.DealUpdateRequest;
import com.sssukho.domain.deal.Deal;
import com.sssukho.domain.room.Room;
import com.sssukho.infra.rdb.repository.DealRepositoryImpl;
import com.sssukho.infra.rdb.repository.RoomRepositoryImpl;
import com.sssukho.testcontainer.mysql.MySQLContainerBaseTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.StringUtils;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration-test")
public class ScenarioTest implements MySQLContainerBaseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepositoryImpl roomRepositoryImpl;

    @Autowired
    private DealRepositoryImpl dealRepositoryImpl;

    @Autowired
    private SecurityConfigurationProperties securityConfigurationProperties;

    @SpyBean
    private JwtTokenProvider jwtTokenProvider;

    private static String USER1_ACCESS_TOKEN;
    private static RoomResponse USER1_REGISTERED_ROOM;

    private static String USER2_ACCESS_TOKEN;
    private static RoomResponse USER2_REGISTERED_ROOM;

    @DisplayName("시나리오#1. 사용자가 회원가입을 하고, 로그인 한 뒤 JWT 토큰을 받는다.")
    @Test
    @Order(1)
    void testScenario1() throws Exception {
        // 1. 회원 가입
        SignUpRequest signUpRequest = new SignUpRequest("dev.sssukho@gmail.com", "passwordpassword",
            "임석호");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(signUpRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.email").value(signUpRequest.email()))
            .andExpect(jsonPath("$.data.name").value(signUpRequest.name()));

        // 2. 로그인
        SignInRequest signInRequest = new SignInRequest(signUpRequest.email(),
            signUpRequest.password());

        ResultActions signInResult = mockMvc.perform(
            post("/auth/signin").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(signInRequest)));

        ResponseMessage<SignInResponse> responseMessage = objectMapper.readValue(
            signInResult.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
            new TypeReference<ResponseMessage<SignInResponse>>() {});

        SignInResponse signInResponse = responseMessage.data();

        assertNotNull(signInResponse);
        assertTrue(StringUtils.hasText(signInResponse.accessToken()));
        assertEquals("Bearer", signInResponse.grantType());

        USER1_ACCESS_TOKEN = signInResponse.accessToken();
    }

    @DisplayName("시나리오#2. JWT 로 인증된 사용자가 내방을 등록하면서 거래 정보도 함께 등록한다.")
    @Test
    @Order(2)
    void testScenario2() throws Exception {
        // 내방 등록(거래 정보도 함께 등록됨)
        // given
        // 월세 / 2000 / 50
        DealRegistrationRequest dealRegistrationRequest1 = new DealRegistrationRequest(
            DealTypeDto.MONTHLY_RENT, new BigDecimal("20000000"),
            new BigDecimal("500000"));

        // 전세 / 1억5천 / 0
        DealRegistrationRequest dealRegistrationRequest2 = new DealRegistrationRequest(
            DealTypeDto.YEAR_RENT, new BigDecimal("150000000"), new BigDecimal("0"));

        RoomRegistrationRequest roomRegistrationRequest = new RoomRegistrationRequest("저렴한 방",
            "2000 에 50 방입니다.", "서울시 강동구", 33.5, RoomTypeDto.ONE_ROOM,
            List.of(dealRegistrationRequest1, dealRegistrationRequest2));

        // when
        ResultActions result = mockMvc.perform(post("/rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
            .content(objectMapper.writeValueAsString(roomRegistrationRequest)));

        ResponseMessage<RoomResponse> responseMessage = objectMapper.readValue(
            result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
            new TypeReference<ResponseMessage<RoomResponse>>() {});

        RoomResponse registeredResponse = responseMessage.data();

        // then
        Room registeredRoom = roomRepositoryImpl.findById(registeredResponse.id());
        assertEquals(roomRegistrationRequest.title(), registeredRoom.getTitle());

        List<Deal> foundDealsInRegisteredRoom = dealRepositoryImpl.findByRoomId(
            registeredRoom.getId());

        assertEquals(dealRegistrationRequest1.deposit(),
            foundDealsInRegisteredRoom.get(0).getDeposit());

        USER1_REGISTERED_ROOM = registeredResponse;
    }

    @DisplayName("시나리오#3. JWT 로 인증된 사용자가 등록한 방을 단건 조회한다.")
    @Test
    @Order(3)
    void testScenario3() throws Exception {
        // when
        ResultActions result = mockMvc.perform(
            get("/rooms/" + USER1_REGISTERED_ROOM.id())
                .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(USER1_REGISTERED_ROOM.id()))
            .andExpect(jsonPath("$.data.title").value(USER1_REGISTERED_ROOM.title()))
            .andExpect(jsonPath("$.data.description").value(USER1_REGISTERED_ROOM.description()))
            .andExpect(jsonPath("$.data.address").value(USER1_REGISTERED_ROOM.address()))
            .andExpect(jsonPath("$.data.area").value(USER1_REGISTERED_ROOM.area()))
            .andExpect(jsonPath("$.data.deals").isArray())
            .andExpect(jsonPath("$.data.deals[0].deposit").value(
                USER1_REGISTERED_ROOM.deals().get(0).deposit()))
            .andExpect(jsonPath("$.data.deals[1].deposit").value(
                USER1_REGISTERED_ROOM.deals().get(1).deposit()));
    }

    @DisplayName("시나리오#4. JWT 로 인증된 사용자가 등록한 모든 방을 조회한다.")
    @Test
    @Order(4)
    void testScenario4() throws Exception {
        // given
        // 월세 / 3000 / 80
        DealRegistrationRequest dealRegistrationRequest1 = new DealRegistrationRequest(
            DealTypeDto.MONTHLY_RENT, new BigDecimal("10000000"),
            new BigDecimal("800000"));

        // 전세 / 4억 / 20
        DealRegistrationRequest dealRegistrationRequest2 = new DealRegistrationRequest(
            DealTypeDto.YEAR_RENT, new BigDecimal("400000000"), new BigDecimal("200000"));

        RoomRegistrationRequest roomRegistrationRequest = new RoomRegistrationRequest("새로 등록된 방",
            "3000 에 80 방입니다.", "서울시 강남구", 33.5, RoomTypeDto.ONE_ROOM,
            List.of(dealRegistrationRequest1, dealRegistrationRequest2));

        ResultActions newRegisteredResult = mockMvc.perform(post("/rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
            .content(objectMapper.writeValueAsString(roomRegistrationRequest)));

        ResponseMessage<RoomResponse> responseMessage = objectMapper.readValue(
            newRegisteredResult.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
            new TypeReference<ResponseMessage<RoomResponse>>() {});

        RoomResponse newRegisteredRoom = responseMessage.data();

        // when
        ResultActions result = mockMvc.perform(get("/rooms/my")
            .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(USER1_REGISTERED_ROOM.id()))
            .andExpect(jsonPath("$.data[0].title").value(USER1_REGISTERED_ROOM.title()))
            .andExpect(jsonPath("$.data[1].id").value(newRegisteredRoom.id()))
            .andExpect(jsonPath("$.data[1].title").value(newRegisteredRoom.title()));
    }

    @DisplayName("시나리오#5. 다른 사용자로 로그인 및 방 등록 후 원래 사용자로 내방 목록을 조회한다.")
    @Test
    @Order(5)
    void testScenario5() throws Exception {
        // given
        // 새로운 회원가입 & 로그인
        SignUpRequest signUpRequest = new SignUpRequest("user2@gmail.com", "passwordpassword",
            "user2");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(signUpRequest)))
            .andExpect(status().isOk());

        SignInRequest signInRequest = new SignInRequest(signUpRequest.email(),
            signUpRequest.password());

        ResultActions signInResult = mockMvc.perform(
            post("/auth/signin").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(signInRequest)));

        ResponseMessage<SignInResponse> responseMessage = objectMapper.readValue(
            signInResult.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
            new TypeReference<ResponseMessage<SignInResponse>>() {});

        SignInResponse signInResponse = responseMessage.data();

        USER2_ACCESS_TOKEN = signInResponse.accessToken();

        // 새로운 회원으로 방 등록
        // 월세 / 500 / 200
        DealRegistrationRequest dealRegistrationRequest = new DealRegistrationRequest(
            DealTypeDto.MONTHLY_RENT, new BigDecimal("5000000"), new BigDecimal("2000000"));

        RoomRegistrationRequest roomRegistrationRequest = new RoomRegistrationRequest(
            "다른 사용자가 등록한 방 타이틀",
            "다른 사용자가 등록한 방 설명 ",
            "서울시 송파구",
            19.5,
            RoomTypeDto.TWO_ROOM,
            List.of(dealRegistrationRequest));

        ResultActions result = mockMvc.perform(post("/rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + USER2_ACCESS_TOKEN)
            .content(objectMapper.writeValueAsString(roomRegistrationRequest)))
            .andExpect(status().isOk());

        ResponseMessage<RoomResponse> registeredRoomResponse = objectMapper.readValue(
            result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
            new TypeReference<ResponseMessage<RoomResponse>>() {});

        USER2_REGISTERED_ROOM = registeredRoomResponse.data();

        // when & then
        // 기존 회원으로 내방 목록 조회시 2개만 나온다.
        mockMvc.perform(get("/rooms/my")
                .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @DisplayName("시나리오#6. JWT 로 인증된 사용자가 조건 없이 전체 방 목록을 조회한다. ")
    @Test
    @Order(6)
    void testScenario6() throws Exception {
        ResultActions result = mockMvc.perform(get("/rooms")
            .param("page", "0")
            .param("size", "10")
            .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
        );

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(3));
    }

    @DisplayName("시나리오#7. JWT 로 인증된 사용자가 보증금 조건으로만 방 목록을 조회한다.")
    @Test
    @Order(7)
    void testScenario7() throws Exception {
        // 월세 / 2000 / 50 => room1
        // 전세 / 1억5천 / 0 => room1
        // 월세 / 3000 / 80 => room2
        // 전세 / 4억 / 20  => room2
        // 월세 / 500 / 200 => room3

        // case 1. 보증금 1천만원 이상 => room1 , room2
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minDeposit", "10000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));

        // case 2. 보증금 1억 이상 => room1, room2
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minDeposit", "100000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));

        // case 3. 보증금 100만원 이상 500만원 이하  => room3
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minDeposit", "1000000")
                    .param("maxDeposit", "5000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1));


        // case 4. 보증금 1천만원 이상 2억 이하 => room 1, room 2
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minDeposit", "10000000")
                    .param("maxDeposit", "200000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @DisplayName("시나리오#8. JWT 로 인증된 사용자가 월세 비용 조건으로만 방 목록을 조회한다.")
    @Test
    @Order(8)
    void testScenario8() throws Exception {
        // 월세 / 2000 / 50 => room1
        // 전세 / 1억5천 / 0 => room1
        // 월세 / 3000 / 80 => room2
        // 전세 / 4억 / 20  => room2
        // 월세 / 500 / 200 => room3

        // case1. 월세 0만원 이상 => room1, room2, room3
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minMonthlyRent", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(3));

        // case2. 월세 20만원 이상 => room1, room2, room3
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minMonthlyRent", "200000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(3));

        // case3. 월세 80만원 이상 => room2, room3
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minMonthlyRent", "800000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));

        // case4. 월세 100만원 이상 200만원 이하 => room3
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minMonthlyRent", "1000000")
                    .param("maxMonthlyREnt", "2000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1));

        // case5. 월세 250만원 이상 => 결과 없음(empty array)
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("minMonthlyRent", "2500000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));

        // case6. 월세 50만원 이하 => room1, room2
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("maxMonthlyRent", "500000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2)).andDo(print());
    }

    @DisplayName("시나리오#9. JWT 로 인증된 사용자가 방 유형(원룸/투룸/쓰리룸)과 월세 조건으로만 조회한다.")
    @Test
    @Order(9)
    void testScenario9() throws Exception {
        // 월세 / 2000 / 50 / ONE_ROOM => room1
        // 전세 / 1억5천 / 0 / ONE_ROOM => room1
        // 월세 / 3000 / 80 / ONE_ROOM => room2
        // 전세 / 4억 / 20  / ONE_ROOM => room2
        // 월세 / 500 / 200 / TWO_ROOM=> room3

        // case1. 원룸 + 월세 50만원 이상 => room1, room2
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("roomTypes","one_room")
                    .param("minMonthlyRent", "500000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));

        // case2. 원룸 + 월세 80만원 이상 => room2
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("roomTypes","one_room")
                    .param("minMonthlyRent", "800000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1));


        // case3. 투룸 + 월세 100만원 이상 => room3
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("roomTypes","two_room")
                    .param("minMonthlyRent", "1000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1));

        // case4. 쓰리룸 => 결과 없음(empty array)
        mockMvc.perform(
                get("/rooms")
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN)
                    .param("page", "0")
                    .param("size", "10")
                    .param("roomTypes","three_room"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));
    }

    @DisplayName("시나리오#10. JWT 로 인증된 사용자가 내방을 삭제한다.")
    @Test
    @Order(10)
    void testScenario10() throws Exception {
        // user1 내방 삭제
        mockMvc.perform(
                delete("/rooms/" + USER1_REGISTERED_ROOM.id())
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN))
            .andExpect(status().isNoContent());

        // user2 로 전체방 조회시 2개
        mockMvc.perform(
            get("/rooms")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + USER2_ACCESS_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2));

        // user1 로 user2 의 방 삭제시 FORBIDDEN
        mockMvc.perform(
                delete("/rooms/" + USER2_REGISTERED_ROOM.id())
                    .header("Authorization", "Bearer " + USER1_ACCESS_TOKEN))
            .andExpect(status().isForbidden());
    }

    @DisplayName("시나리오#11. JWT 로 인증된 사용자가 내방을 수정한다.")
    @Test
    @Order(11)
    void testScenario11() throws Exception {

        // 월세 / 500 / 200 / TWO_ROOM => 전세 / 100000000 / 10 / TWO_ROOM 으로 수정
        DealUpdateRequest dealUpdateRequest = new DealUpdateRequest(
            DealTypeDto.YEAR_RENT,
            BigDecimal.valueOf(100000000),
            BigDecimal.valueOf(10));

        RoomUpdateRequest roomUpdateRequest = new RoomUpdateRequest(
            "방수정타이틀",
            "방수정설명",
            "경기도 성남시 수정구",
            18.34,
            RoomTypeDto.TWO_ROOM,
            List.of(dealUpdateRequest));

        // when
        ResultActions result = mockMvc.perform(
            patch("/rooms/" + USER2_REGISTERED_ROOM.id())
                .header("Authorization", "Bearer " + USER2_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(roomUpdateRequest)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(USER2_REGISTERED_ROOM.id()))
            .andExpect(jsonPath("$.data.title").value(roomUpdateRequest.title()))
            .andExpect(jsonPath("$.data.description").value(roomUpdateRequest.description()))
            .andExpect(jsonPath("$.data.area").value(roomUpdateRequest.area()))
            .andExpect(jsonPath("$.data.roomType").value(roomUpdateRequest.roomType().name()))
            .andExpect(jsonPath("$.data.deals[0].dealType").value(dealUpdateRequest.dealType().name()))
            .andExpect(jsonPath("$.data.deals[0].deposit").value(dealUpdateRequest.deposit()))
            .andExpect(jsonPath("$.data.deals[0].monthlyRent").value(dealUpdateRequest.monthlyRent()));
    }

    @DisplayName("시나리오#12. access token 이 만료된 경우, refresh token api 를 통해 갱신된 access token 을 받는다.")
    @Test
    @Order(12)
    void testScenario12() throws Exception {
        // 1. 회원 가입
        SignUpRequest signUpRequest = new SignUpRequest("userForRefreshToken@gmail.com",
            "passwordpassword", "리프레쉬토큰사용자테스트");

         mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(signUpRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(signUpRequest.email()))
            .andExpect(jsonPath("$.data.name").value(signUpRequest.name()));

        // 2. 로그인
        SignInRequest signInRequest = new SignInRequest(signUpRequest.email(),
            signUpRequest.password());

        // 로그인 시 이미 만료된 액세스 토큰 응답
        when(jwtTokenProvider.generateAccessToken(signInRequest.email())).thenReturn(
            generateInvalidAccessToken(signInRequest.email()));

        ResultActions signInResult = mockMvc.perform(
            post("/auth/signin").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(signInRequest)));

        ResponseMessage<SignInResponse> responseMessage = objectMapper.readValue(
            signInResult.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
            new TypeReference<ResponseMessage<SignInResponse>>() {});

        SignInResponse signInResponse = responseMessage.data();
        String expiredAccessToken = signInResponse.accessToken();
        String refreshToken = signInResponse.refreshToken();

        // 3. 이미 만료된 액세스 토큰으로 내방 조회 API 접근시 401 에러
        mockMvc.perform(get("/rooms/my")
                .header("Authorization", "Bearer " + expiredAccessToken))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode")
                .value(ErrorCode.UNAUTHORIZED.getErrorCode()));

        // 4. 로그인 시에 받았던 refresh token 으로 새 access token 발급
        // 만료된 토큰을 발급하게 한 부분을 원래대로 돌린다.
        doCallRealMethod().when(jwtTokenProvider).generateAccessToken(anyString());

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
        ResultActions refreshTokenResult = mockMvc.perform(post("/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(refreshTokenRequest)));

        ResponseMessage<SignInResponse> refreshTokenResponse = objectMapper.readValue(
            refreshTokenResult.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
            new TypeReference<ResponseMessage<SignInResponse>>() {});

        String newAccessToken = refreshTokenResponse.data().accessToken();

        // 5. 새 access token 으로 요청시에는 성공
        mockMvc.perform(get("/rooms/my")
                .header("Authorization", "Bearer " + newAccessToken))
            .andExpect(status().isOk());
    }

    private String generateInvalidAccessToken(String email) {
        Date expiryDate = new Date(System.currentTimeMillis());
        SecretKey key = Keys.hmacShaKeyFor(
            securityConfigurationProperties.jwt().secret().getBytes());
        return Jwts.builder()
            .subject(email)
            .issuedAt(new Date())
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }
}

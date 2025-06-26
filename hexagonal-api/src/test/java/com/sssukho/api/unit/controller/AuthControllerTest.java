package com.sssukho.api.unit.controller;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sssukho.api.controller.AuthController;
import com.sssukho.api.security.JwtAuthenticationFilter;
import com.sssukho.api.service.AuthService;
import com.sssukho.common.dto.auth.SignInRequest;
import com.sssukho.common.dto.auth.SignInResponse;
import com.sssukho.common.dto.auth.SignUpRequest;
import com.sssukho.common.dto.auth.SignupResponse;
import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
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

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("회원 등록 요청 성공 테스트")
    @Test
    void testSignUpSucceed() throws Exception {
        // given
        SignUpRequest fakeRequest = new SignUpRequest("dev.sssukho@gmail.com", "plainPassword",
            "임석호");

        final Long fakeMemberId = 1L;
        SignupResponse fakeResponse = new SignupResponse(fakeMemberId, fakeRequest.email(),
            fakeRequest.name());

        when(authService.signUp(fakeRequest)).thenReturn(fakeResponse);

        // when
        ResultActions result = mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fakeRequest)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.email").value(fakeRequest.email()))
            .andExpect(jsonPath("$.data.name").value(fakeRequest.name()));
    }

    @DisplayName("회원 등록 요청 실패 테스트")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSignUpRequestProvider")
    void testSignUpFailed(String description, SignUpRequest request) throws Exception {
        // when
        ResultActions result = mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_PARAMETER.getErrorCode()));
    }

    static Stream<Arguments> invalidSignUpRequestProvider() {
        return Stream.of(
            Arguments.of("이메일이 null", new SignUpRequest(null, "password123", "홍길동")),
            Arguments.of("이메일이 빈 문자열", new SignUpRequest("", "password123", "홍길동")),
            Arguments.of("이메일이 공백", new SignUpRequest("   ", "password123", "홍길동")),
            Arguments.of("이메일 형식이 아님", new SignUpRequest("invalid-email", "password123", "홍길동")),
            Arguments.of("비밀번호가 null", new SignUpRequest("user@example.com", null, "홍길동")),
            Arguments.of("비밀번호가 빈 문자열", new SignUpRequest("user@example.com", "", "홍길동")),
            Arguments.of("비밀번호가 공백", new SignUpRequest("user@example.com", "   ", "홍길동")),
            Arguments.of("비밀번호가 6자 미만", new SignUpRequest("user@example.com", "123", "홍길동")),
            Arguments.of("이름이 null", new SignUpRequest("user@example.com", "password123", null)),
            Arguments.of("이름이 빈 문자열", new SignUpRequest("user@example.com", "password123", "")),
            Arguments.of("이름이 공백", new SignUpRequest("user@example.com", "password123", "   "))
        );
    }

    @DisplayName("로그인 요청 성공 테스트")
    @Test
    void testSignInSucceed() throws Exception {
        // given
        SignInRequest fakeRequest = new SignInRequest("dev.sssukho@gmail.com", "plainPassword");

        SignInResponse fakeResponse = new SignInResponse("accessToken", "refreshToken", "Bearer");

        when(authService.signIn(fakeRequest)).thenReturn(fakeResponse);

        // when
        ResultActions result = mockMvc.perform(
            post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fakeRequest)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value(fakeResponse.accessToken()))
            .andExpect(jsonPath("$.data.refreshToken").value(fakeResponse.refreshToken()))
            .andExpect(jsonPath("$.data.grantType").value(fakeResponse.grantType()));

    }

    @DisplayName("로그인 요청 실패 테스트")
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSignInRequestProvider")
    void testSignInFailed(String description, SignInRequest request) throws Exception {
        // when
        ResultActions result = mockMvc.perform(
            post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_PARAMETER.getErrorCode()));
    }

    static Stream<Arguments> invalidSignInRequestProvider() {
        return Stream.of(
            Arguments.of("이메일이 null", new SignInRequest(null, "password123")),
            Arguments.of("이메일이 빈 문자열", new SignInRequest("", "password123")),
            Arguments.of("이메일이 공백", new SignInRequest("   ", "password123")),
            Arguments.of("이메일 형식이 아님", new SignInRequest("invalid-email", "password123")),
            Arguments.of("비밀번호가 null", new SignInRequest("user@example.com", null)),
            Arguments.of("비밀번호가 빈 문자열", new SignInRequest("user@example.com", "")),
            Arguments.of("비밀번호가 공백", new SignInRequest("user@example.com", "   "))
        );
    }
}

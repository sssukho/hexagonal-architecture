package com.sssukho.api.unit.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sssukho.api.security.JwtTokenProvider;
import com.sssukho.api.service.AuthService;
import com.sssukho.api.service.MemberService;
import com.sssukho.common.dto.auth.SignInRequest;
import com.sssukho.common.dto.auth.SignInResponse;
import com.sssukho.common.dto.auth.SignUpRequest;
import com.sssukho.common.dto.auth.SignupResponse;
import com.sssukho.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberService memberService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @DisplayName("회원가입 성공 테스트")
    @Test
    void testSignUpSucceed() {
        // given
        SignUpRequest fakeRequest = new SignUpRequest("dev.sssukho@gmail.com", "passwordpassword",
            "임석호");
        Long fakeRegisteredMemberId = 1L;

        Member mockedRegisteredMember = mock(Member.class);
        when(mockedRegisteredMember.getId()).thenReturn(fakeRegisteredMemberId);
        when(mockedRegisteredMember.getEmail()).thenReturn(fakeRequest.email());
        when(mockedRegisteredMember.getName()).thenReturn(fakeRequest.name());

        when(memberService.register(any(Member.class))).thenReturn(mockedRegisteredMember);

        // when
        SignupResponse result = authService.signUp(fakeRequest);

        // then
        assertEquals(fakeRegisteredMemberId, result.id());
        assertEquals(fakeRequest.email(), result.email());
        assertEquals(fakeRequest.name(), result.name());

        verify(memberService, times(1)).register(any(Member.class));
    }

    @DisplayName("로그인 성공 테스트")
    @Test
    void testSignInSucceed() {
        // given
        SignInRequest fakeRequest = new SignInRequest("dev.sssukho@gmail.com", "fakePassword");

        Member fakeFoundMember = mock(Member.class);
        when(fakeFoundMember.getHashedPassword()).thenReturn(fakeRequest.password());

        when(memberService.findByEmail(fakeRequest.email())).thenReturn(fakeFoundMember);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(tokenProvider.generateAccessToken(any())).thenReturn("accessToken");

        // when
        SignInResponse result = authService.signIn(fakeRequest);

        // then
        assertEquals("accessToken", result.accessToken());
        assertEquals("Bearer", result.grantType());

        verify(tokenProvider, times(1)).generateAccessToken(any());
    }
}

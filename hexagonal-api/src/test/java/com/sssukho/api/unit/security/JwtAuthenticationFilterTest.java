package com.sssukho.api.unit.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sssukho.api.security.JwtAuthenticationFilter;
import com.sssukho.api.security.JwtTokenProvider;
import com.sssukho.api.service.MemberService;
import jakarta.servlet.FilterChain;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private MemberService memberService;

    @Mock
    private FilterChain mockedFilterChain;

    private MockHttpServletRequest mockedRequest;
    private MockHttpServletResponse mockedResponse;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockedRequest = new MockHttpServletRequest();
        mockedResponse = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();

        userDetails = User.builder()
            .username("testuser")
            .password("password")
            .authorities(List.of(new SimpleGrantedAuthority("MEMBER")))
            .build();
    }

    @DisplayName("JWT 토큰이 유효할 경우 인증에 성공한다.")
    @Test
    void testAuthenticationSucceed() throws Exception {
        // given
        String validJwt = "validToken";
        String memberName = "testMember";

        mockedRequest.addHeader("Authorization", "Bearer " + validJwt);

        when(tokenProvider.validateToken(validJwt)).thenReturn(true);
        when(tokenProvider.extractMemberNameFromToken(validJwt)).thenReturn(memberName);
        when(memberService.loadUserByUsername(memberName)).thenReturn(userDetails);

        // when
        jwtAuthenticationFilter.doFilterInternal(mockedRequest, mockedResponse, mockedFilterChain);

        // then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(result);
        assertEquals(userDetails, result.getPrincipal());
        assertEquals(userDetails.getAuthorities().size(), userDetails.getAuthorities().size());

        verify(tokenProvider).validateToken(validJwt);
        verify(tokenProvider).extractMemberNameFromToken(validJwt);
        verify(memberService).loadUserByUsername(memberName);
        verify(mockedFilterChain).doFilter(mockedRequest, mockedResponse);
    }

    @DisplayName("헤더에 Authorization 키가 없으면 인증에 실패한다.")
    @Test
    void testAuthenticationFailedWithoutAuthorizationHeader() throws Exception {
        // when
        jwtAuthenticationFilter.doFilterInternal(mockedRequest, mockedResponse, mockedFilterChain);

        // then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertNull(result);

        verify(tokenProvider, never()).validateToken(any());
        verify(tokenProvider, never()).extractMemberNameFromToken(any());
        verify(memberService, never()).loadUserByUsername(any());
        verify(mockedFilterChain).doFilter(mockedRequest, mockedResponse);
    }

    @DisplayName("헤더에 Bearer 가 없는 Authorization 헤더일 때 인증에 실패한다.")
    @Test
    void testAuthenticationFailedWithoutBearerKeyword() throws Exception {
        // given
        mockedRequest.addHeader("Authorization", "weired token");

        // when
        jwtAuthenticationFilter.doFilterInternal(mockedRequest, mockedResponse, mockedFilterChain);

        // then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertNull(result);

        verify(tokenProvider, never()).validateToken(any());
        verify(mockedFilterChain).doFilter(mockedRequest, mockedResponse);
    }

    @DisplayName("유효하지 않은 JWT 일 때 인증에 실패한다.")
    @Test
    void testAuthenticationFailedWithInvalidJwt() throws Exception {
        // given
        String invalidJwt = "invalidToken";
        mockedRequest.addHeader("Authorization", "Bearer " + invalidJwt);

        when(tokenProvider.validateToken(invalidJwt)).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(mockedRequest, mockedResponse, mockedFilterChain);

        // then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertNull(result);

        verify(tokenProvider).validateToken(invalidJwt);
        verify(tokenProvider, never()).extractMemberNameFromToken(any());
        verify(memberService, never()).loadUserByUsername(any());
        verify(mockedFilterChain).doFilter(mockedRequest, mockedResponse);
    }

    @DisplayName("JWT 는 유효하지만 멤버를 찾을 수 없을 때 예외가 발생한다.")
    @Test
    void testAuthenticationThrowExceptionWithValidJwt() throws Exception {
        // given
        String validJwt = "validJwt";
        String memberName = "invalidMemberName";

        mockedRequest.addHeader("Authorization", "Bearer " + validJwt);

        when(tokenProvider.validateToken(validJwt)).thenReturn(true);
        when(tokenProvider.extractMemberNameFromToken(validJwt)).thenReturn(memberName);
        when(memberService.loadUserByUsername(memberName)).thenThrow(new RuntimeException());

        // when & then
        assertThrows(RuntimeException.class,
            () -> jwtAuthenticationFilter.doFilterInternal(mockedRequest, mockedResponse,
                mockedFilterChain));

        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertNull(result);

        verify(tokenProvider).validateToken(validJwt);
        verify(tokenProvider).extractMemberNameFromToken(validJwt);
        verify(memberService).loadUserByUsername(memberName);
    }

}

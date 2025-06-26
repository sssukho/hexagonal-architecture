package com.sssukho.api.service;

import com.sssukho.api.security.JwtTokenProvider;
import com.sssukho.common.dto.auth.RefreshTokenRequest;
import com.sssukho.common.dto.auth.SignInRequest;
import com.sssukho.common.dto.auth.SignInResponse;
import com.sssukho.common.dto.auth.SignUpRequest;
import com.sssukho.common.dto.auth.SignupResponse;
import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.exception.CustomException;
import com.sssukho.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String JWT_GRANT_TYPE = "Bearer";
    private final MemberService memberService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder 방식

    /**
     * 회원 가입
     */
    public SignupResponse signUp(SignUpRequest request) {
        String hashedPassword = passwordEncoder.encode(request.password());
        Member memberToRegister = Member.createMemberToRegisterWithoutId(request.email(), hashedPassword,
            request.name());
        Member registeredMember = memberService.register(memberToRegister);
        return new SignupResponse(registeredMember.getId(), registeredMember.getEmail(),
            registeredMember.getName());
    }

    /**
     * 로그인
     */
    public SignInResponse signIn(SignInRequest request) {
        Member member = memberService.findByEmail(request.email());
        if (!passwordEncoder.matches(request.password(), member.getHashedPassword())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        String accessToken = tokenProvider.generateAccessToken(request.email());
        String refreshToken = tokenProvider.generateRefreshToken(request.email());
        memberService.updateRefreshToken(request.email(), refreshToken);
        return new SignInResponse(accessToken, refreshToken, JWT_GRANT_TYPE);
    }

    /**
     * 토큰 갱신
     */
    public SignInResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        String userEmail = tokenProvider.extractMemberNameFromToken(refreshToken);
        UserDetails userDetails = memberService.loadUserByUsername(userEmail);

        if (!memberService.validateRefreshToken(userEmail, refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String newAccessToken = tokenProvider.generateAccessToken(userDetails.getUsername());
        return new SignInResponse(newAccessToken, refreshToken, JWT_GRANT_TYPE);
    }
}

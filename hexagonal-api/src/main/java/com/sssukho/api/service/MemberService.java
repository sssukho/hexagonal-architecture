package com.sssukho.api.service;

import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.exception.CustomException;
import com.sssukho.domain.member.Member;
import com.sssukho.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member;
        try {
            member = memberRepository.findByEmail(email);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        UserBuilder builder = User.withUsername(email);
        builder.password(member.getHashedPassword());
        builder.roles("MEMBER");

        return builder.build();
    }

    @Transactional(rollbackFor = Exception.class)
    public Member register(Member memberToRegister) {
        if (memberRepository.existsByEmail(memberToRegister.getEmail())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 존재하는 회원(이메일) 입니다.");
        }
        return memberRepository.save(memberToRegister);
    }

    @Transactional
    public void updateRefreshToken(String email, String refreshToken) {
        memberRepository.updateRefreshToken(email, refreshToken);
    }

    public boolean validateRefreshToken(String email, String refreshTokenToValidate) {
        Member foundMember = memberRepository.findByEmail(email);
        return foundMember.getRefreshToken().equals(refreshTokenToValidate);
    }

}

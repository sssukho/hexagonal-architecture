package com.sssukho.domain.member;

public interface MemberRepository {

    Member findByEmail(String email);

    Member save(Member member);

    boolean existsByEmail(String email);

    Member updateRefreshToken(String email, String refreshToken);

}

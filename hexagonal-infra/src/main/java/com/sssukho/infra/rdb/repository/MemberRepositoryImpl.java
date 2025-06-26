package com.sssukho.infra.rdb.repository;


import com.sssukho.common.dto.common.ErrorResponseMessage.ErrorCode;
import com.sssukho.common.exception.CustomException;
import com.sssukho.domain.member.Member;
import com.sssukho.domain.member.MemberRepository;
import com.sssukho.infra.rdb.entity.MemberEntity;
import com.sssukho.infra.rdb.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository jpaRepository;

    @Override
    public Member findByEmail(String email) {
        MemberEntity foundMemberEntity = jpaRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        return MemberMapper.toDomain(foundMemberEntity);
    }

    @Override
    public Member save(Member member) {
        MemberEntity memberEntityToSave = MemberMapper.toEntity(member);
        MemberEntity savedMemberEntity = jpaRepository.save(memberEntityToSave);
        return MemberMapper.toDomain(savedMemberEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Member updateRefreshToken(String email, String refreshToken) {
        MemberEntity memberEntity = jpaRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        memberEntity.updateRefreshToken(refreshToken);
        return MemberMapper.toDomain(memberEntity);
    }
}

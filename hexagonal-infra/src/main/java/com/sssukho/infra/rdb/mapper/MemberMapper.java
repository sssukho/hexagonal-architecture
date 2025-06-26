package com.sssukho.infra.rdb.mapper;


import com.sssukho.domain.member.Member;
import com.sssukho.infra.rdb.entity.MemberEntity;

public class MemberMapper {

    public static Member toDomain(MemberEntity entity) {
        return Member.createMemberWithId(entity.getId(), entity.getEmail(), entity.getPassword(), entity.getName(),
            entity.getRefreshToken());
    }

    public static MemberEntity toEntity(Member domain) {
        return MemberEntity.of(domain.getEmail(), domain.getHashedPassword(), domain.getName());
    }

}

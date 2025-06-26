package com.sssukho.domain.member;

import lombok.Getter;

@Getter
public class Member {

    private Long id;
    private String email;
    private String hashedPassword;
    private String name;
    private String refreshToken;

    private Member(Long id, String email, String hashedPassword, String name, String refreshToken) {
        this.id = id;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.name = name;
        this.refreshToken = refreshToken;
    }

    private Member(String email, String hashedPassword, String name) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.name = name;
    }

    public static Member createMemberWithId(Long id, String email, String password, String name, String refreshToken) {
        return new Member(id, email, password, name, refreshToken);
    }

    public static Member createMemberToRegisterWithoutId(String email, String hashedPassword, String name) {
        return new Member(email, hashedPassword, name);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

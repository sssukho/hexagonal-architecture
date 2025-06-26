package com.sssukho.common.dto.auth;

public record SignInResponse(
    String accessToken,
    String refreshToken,
    String grantType
){

}

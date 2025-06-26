package com.sssukho.api.controller;

import com.sssukho.api.service.AuthService;
import com.sssukho.common.dto.auth.RefreshTokenRequest;
import com.sssukho.common.dto.auth.SignInRequest;
import com.sssukho.common.dto.auth.SignInResponse;
import com.sssukho.common.dto.auth.SignUpRequest;
import com.sssukho.common.dto.auth.SignupResponse;
import com.sssukho.common.dto.common.ResponseMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage<SignupResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignupResponse result = authService.signUp(request);
        return ResponseMessage.create(result);
    }

    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage<SignInResponse> signIn(@Valid @RequestBody SignInRequest request) {
        SignInResponse result = authService.signIn(request);
        return ResponseMessage.create(result);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage<SignInResponse> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request) {

        SignInResponse result = authService.refreshToken(request);
        return ResponseMessage.create(result);
    }

}

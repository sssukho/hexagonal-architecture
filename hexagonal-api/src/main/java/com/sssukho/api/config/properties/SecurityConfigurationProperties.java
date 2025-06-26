package com.sssukho.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "secret")
public record SecurityConfigurationProperties(
    Jwt jwt
) {

    public record Jwt (
        String secret,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs
    ) { }
}

package com.cecena.spotifystats.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class TokenData {
    private String accessToken;
    private String refreshToken;
    private long expirationTimeMillis;

    public TokenData(String accessToken, String refreshToken, long expirationTimeMillis) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTimeMillis = expirationTimeMillis;
    }
}

package com.cecena.spotifystats.utils;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
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

package com.cecena.spotifystats.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenStorageService {
    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();

    public void saveToken(String accessToken, String refreshToken) {
        tokenMap.put(accessToken, refreshToken);
    }

    public String getTokens(String accessToken) {
        return tokenMap.get(accessToken);
    }

    public void removeTokens(String accessToken) {
        tokenMap.remove(accessToken);
    }

    public boolean hasTokens(String accessToken) {
        return tokenMap.containsKey(accessToken);
    }
}

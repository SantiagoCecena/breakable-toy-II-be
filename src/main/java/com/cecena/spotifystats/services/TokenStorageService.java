package com.cecena.spotifystats.services;

import com.cecena.spotifystats.utils.TokenData;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenStorageService {
    private final Map<String, TokenData> tokenMap = new ConcurrentHashMap<>();

    public void saveToken(String userId, TokenData tokenData) {
        tokenMap.put(userId, tokenData);
    }

    public TokenData getTokens(String userId) {
        return tokenMap.get(userId);
    }

    public void removeTokens(String userId) {
        tokenMap.remove(userId);
    }

    public boolean hasTokens(String userId) {
        return tokenMap.containsKey(userId);
    }
}

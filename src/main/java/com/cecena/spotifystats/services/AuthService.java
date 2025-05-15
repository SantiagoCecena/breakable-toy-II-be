package com.cecena.spotifystats.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Value("${app.spotify.clientId}")
    private String clientId;
    @Value("${app.spotify.clientSecret}")
    private String clientSecret;

    private RestTemplate restTemplate;

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String authString = clientId + ":" + clientSecret;
        String base64Auth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + base64Auth);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);

        HttpEntity<?> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://accounts.spotify.com/api/token",
                    request,
                    Map.class
            );
            Map<String, String> result = new HashMap<>();
            result.put("access_token", (String) response.getBody().get("access_token"));
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}

package com.cecena.spotifystats.controllers;

import com.cecena.spotifystats.services.TokenStorageService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenStorageService tokenStorageService;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void login_shouldRedirectToSpotify() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/spotify"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("accounts.spotify.com")));
    }

    @Test
    void callback_shouldReturnAccessToken_whenStateMatchesAndSpotifyReturnsToken() throws Exception {
        String code = "valid-code";
        String state = "test-state";
        String storedState = "test-state";
        String accessToken = "mock-access-token";
        String refreshToken = "mock-refresh-token";
        int expiresIn = 3600;

        ResponseEntity<Map> tokenResponse = new ResponseEntity<>(
                Map.of(
                        "access_token", accessToken,
                        "refresh_token", refreshToken,
                        "expires_in", expiresIn
                ),
                HttpStatus.OK
        );

        Mockito.when(restTemplate.postForEntity(
                eq("https://accounts.spotify.com/api/token"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(tokenResponse);

        ResponseEntity<Map> meResponse = new ResponseEntity<>(
                Map.of("id", "spotify-user-id"),
                HttpStatus.OK
        );

        Mockito.when(restTemplate.exchange(
                eq("https://api.spotify.com/v1/me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(meResponse);

        mockMvc.perform(get("/api/auth/callback")
                        .param("code", code)
                        .param("state", state)
                        .cookie(new Cookie("spotify_auth_state", storedState))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(accessToken));
    }
}

package com.cecena.spotifystats.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authService = new AuthService(restTemplate);
        ReflectionTestUtils.setField(authService, "clientId", "testClientId");
        ReflectionTestUtils.setField(authService, "clientId", "testClientId");
    }

    @Test
    void refreshAccessToken_shouldReturnToken_ifSpotifyReturnsSuccess() {
        // Given
        String refreshToken = "testRefreshToken";
        Map<String, String> mockBody = new HashMap<>();
        mockBody.put("access_token", "mockAccessToken");
        ResponseEntity<Map> mockResponse = new ResponseEntity<>(mockBody, HttpStatus.OK);

        Mockito.when(restTemplate.postForEntity(
                Mockito.eq("https://accounts.spotify.com/api/token"),
                Mockito.any(HttpEntity.class),
                Mockito.eq(Map.class)
        )).thenReturn(mockResponse);

        // When
        String token = authService.refreshAccessToken(refreshToken);

        // Then
        Assertions.assertThat(token).isEqualTo("mockAccessToken");
        Mockito.verify(restTemplate).postForEntity(
                Mockito.eq("https://accounts.spotify.com/api/token"),
                Mockito.any(HttpEntity.class),
                Mockito.eq(Map.class)
        );
    }

    @Test
    void refreshAccessToken_shouldThrowException_ifSpotifyCallFails() {
        Mockito.when(restTemplate.postForEntity(
                Mockito.anyString(), Mockito.any(), Mockito.eq(Map.class)
        )).thenThrow(new RuntimeException("Spotify error"));

        Assertions.assertThatThrownBy(() -> authService.refreshAccessToken("invalid"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Spotify error");
    }
}

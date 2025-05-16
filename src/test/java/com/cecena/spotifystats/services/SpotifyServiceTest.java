package com.cecena.spotifystats.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class SpotifyServiceTest {
    private SpotifyService spotifyService;

    @Mock
    private AuthService authService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TokenStorageService tokenStorageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        spotifyService = new SpotifyService(authService, restTemplate, tokenStorageService);
    }

    @Test
    void makeSpotifyRequest_shouldReturnResponse_whenTokenIsValid() {
        // Given
        String accessToken = "valid-token";
        String url = "https://api.spotify.com/test";
        HttpMethod method = HttpMethod.GET;

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(method),
                Mockito.any(HttpEntity.class),
                Mockito.eq(String.class))
        ).thenReturn(mockResponse);

        // When
        ResponseEntity<String> response = spotifyService.makeSpotifyRequest(accessToken, url, method, String.class);

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isEqualTo("OK");
    }


    @Test
    void makeSpotifyRequest_shouldRetryWithNewToken_whenTokenIsExpiredAndRefreshWorks() {
        // Given
        String expiredToken = "expired-token";
        String newAccessToken = "new-token";
        String refreshToken = "refresh-token";
        String url = "https://api.spotify.com/test";

        Mockito.when(restTemplate.exchange(
                        Mockito.eq(url),
                        Mockito.eq(HttpMethod.GET),
                        Mockito.any(HttpEntity.class),
                        Mockito.eq(String.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED)) // primer intento falla
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK)); // segundo intento con nuevo token

        Mockito.when(tokenStorageService.getTokens(expiredToken)).thenReturn(refreshToken);
        Mockito.when(authService.refreshAccessToken(refreshToken)).thenReturn(newAccessToken);

        // no falla el guardado del nuevo token
        Mockito.doNothing().when(tokenStorageService).removeTokens(expiredToken);
        Mockito.doNothing().when(tokenStorageService).saveToken(newAccessToken, refreshToken);

        // When
        ResponseEntity<String> response = spotifyService.makeSpotifyRequest(expiredToken, url, HttpMethod.GET, String.class);

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isEqualTo("OK");

        // Verifica que se intentó refrescar y guardar
        Mockito.verify(authService).refreshAccessToken(refreshToken);
        Mockito.verify(tokenStorageService).removeTokens(expiredToken);
        Mockito.verify(tokenStorageService).saveToken(newAccessToken, refreshToken);
    }

    @Test
    void makeSpotifyRequest_shouldReturnUnauthorized_whenNoRefreshTokenExists() {
        // Given
        String expiredToken = "expired-token";
        String url = "https://api.spotify.com/test";

        Mockito.when(restTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(HttpEntity.class),
                Mockito.eq(String.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED)); // primer intento falla

        Mockito.when(tokenStorageService.getTokens(expiredToken)).thenReturn(null); // no hay refresh token

        // When
        ResponseEntity<String> response = spotifyService.makeSpotifyRequest(expiredToken, url, HttpMethod.GET, String.class);

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void makeSpotifyRequest_shouldReturnServerError_whenOtherErrorOccurs() {
        // Given
        String token = "any-token";
        String url = "https://api.spotify.com/test";

        Mockito.when(restTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(HttpEntity.class),
                Mockito.eq(String.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)); // 400 o cualquier otro error

        // When
        ResponseEntity<String> response = spotifyService.makeSpotifyRequest(token, url, HttpMethod.GET, String.class);

        // Then
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package com.cecena.spotifystats.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class SpotifyService {

    private RestTemplate restTemplate;
    private TokenStorageService tokenStorageService;
    private AuthService authService;

    public SpotifyService(AuthService authService, RestTemplate restTemplate, TokenStorageService tokenStorageService) {
        this.authService = authService;
        this.restTemplate = restTemplate;
        this.tokenStorageService = tokenStorageService;
    }

    public <T> ResponseEntity<T> makeSpotifyRequest(
            String accessToken,
            String url,
            HttpMethod method,
            Class<T> responseType
    ) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            return restTemplate.exchange(url, method, new HttpEntity<>(headers), responseType);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Expired token
                String refreshToken = tokenStorageService.getTokens(accessToken);

                if (refreshToken == null) {
//                    System.out.println("There's no refresh token for this access token");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }

                String newAccessToken = authService.refreshAccessToken(refreshToken);
//                System.out.println("New access token: " + newAccessToken);
                if (newAccessToken != null) {
                    tokenStorageService.removeTokens(accessToken);
                    tokenStorageService.saveToken(newAccessToken, refreshToken);

                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setBearerAuth(newAccessToken);
//                        System.out.println("Sending new request with new access token");

                        return restTemplate.exchange(url, method, new HttpEntity<>(headers), responseType);

                    } catch (Exception e2) {
                        System.out.println(e2);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

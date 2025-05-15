package com.cecena.spotifystats.controllers;

import com.cecena.spotifystats.services.TokenStorageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${app.spotify.clientId}")
    private String clientId;
    @Value("${app.spotify.clientSecret}")
    private String clientSecret;
    @Value("${app.spotify.redirectUri}")
    private String redirectUri;
    private final String stateKey = "spotify_auth_state";

    private RestTemplate restTemplate;

    private TokenStorageService tokenStorageService;

    public AuthController(RestTemplate restTemplate, TokenStorageService tokenStorageService){
        this.restTemplate = restTemplate;
        this.tokenStorageService = tokenStorageService;

    }

    @GetMapping("/spotify")
    public void login(HttpServletResponse response) throws IOException {
        String state = generateRandomString(16);

        Cookie stateCookie = new Cookie(stateKey, state);
        stateCookie.setHttpOnly(true);
        stateCookie.setPath("/");
        response.addCookie(stateCookie);

        String scope = "user-read-private user-read-email user-library-read user-top-read";
        String spotifyUrl = UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host("accounts.spotify.com")
                .path("/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", scope)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build().toString();
        response.sendRedirect(spotifyUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @CookieValue(name = "spotify_auth_state", required = false) String storedState,
            HttpServletResponse response
    ) {
        if (state == null || !state.equals(storedState)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("State mismatch");
        }
        Cookie cookie = new Cookie(stateKey, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        //Get the access and refresh tokens
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String authString = clientId + ":" + clientSecret;
        String base64Auth = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + base64Auth);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(form, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                tokenRequest,
                Map.class
        );
        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error fetching the token");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");
        String refreshToken = (String) tokenResponse.getBody().get("refresh_token");

        tokenStorageService.saveToken(accessToken, refreshToken);
        String redirectUri = UriComponentsBuilder.fromUriString("http://localhost:3000/")
                .queryParam("access_token", accessToken)
                .build()
                .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUri)
                .build();
    }

    @GetMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(
            @RequestParam("refresh_token") String refreshToken
    ) {
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
            result.put("refresh_token", (String) response.getBody().get("refresh_token"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid refresh token"));
        }
    }

    private String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }
}

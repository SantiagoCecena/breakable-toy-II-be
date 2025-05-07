package com.cecena.spotifystats.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
public class AppController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/me/top/artists")
    public ResponseEntity<?> getUserTopArtist(
            @RequestParam("access_token") String accessToken
    ) {
        String type = "artists";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/me/top/" + type,
                HttpMethod.GET,
                request,
                Map.class
        );
        Map<String, Object> result = new HashMap<>();
        result.put("previous", response.getBody().get("previous"));
        result.put("next", response.getBody().get("next"));
        result.put("items", response.getBody().get("items"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<?> getArtist(
            @PathVariable String id,
            @RequestParam("access_token") String accessToken
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/artists/" + id,
                HttpMethod.GET,
                request,
                Map.class
        );

        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/albums/{id}")
    public ResponseEntity<?> getAlbum(
            @PathVariable String id,
            @RequestParam("access_token") String accessToken
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/albums/" + id,
                HttpMethod.GET,
                request,
                Map.class
        );
        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/search")
    public ResponseEntity<?> Search(
            @RequestHeader("Authorization") String token,
            @RequestParam("q") String query
    ) {
        String accessToken = token.split("\\s")[1];
        String types = "album,artist,track";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/search?q=" + query + "&type=" + types,
                HttpMethod.GET,
                request,
                Map.class
        );

        return ResponseEntity.ok(response.getBody());
    }
}

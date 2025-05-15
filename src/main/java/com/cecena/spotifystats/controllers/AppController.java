package com.cecena.spotifystats.controllers;

import com.cecena.spotifystats.services.SpotifyService;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080", "http://localhost:4173", "http://localhost:3000"})
public class AppController {

    private SpotifyService spotifyService;

    public AppController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/me/top/artists")
    public ResponseEntity<?> getUserTopArtist(
            @RequestHeader("Authorization") String accessToken
    ) {
        String type = "artists";
        ResponseEntity<Map> response = spotifyService.makeSpotifyRequest(
                getAccessTokenFromHeader(accessToken),
                "https://api.spotify.com/v1/me/top/" + type,
                HttpMethod.GET,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            return response;
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("previous", response.getBody().get("previous"));
            result.put("next", response.getBody().get("next"));
            result.put("items", response.getBody().get("items"));
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<?> getArtist(
            @PathVariable String id,
            @RequestHeader("Authorization") String accessToken
    ) {
        ResponseEntity<Map> responseArtist = spotifyService.makeSpotifyRequest(
                getAccessTokenFromHeader(accessToken),
                "https://api.spotify.com/v1/artists/" + id,
                HttpMethod.GET,
                Map.class
        );

        ResponseEntity<Map> responseArtistTopTracks = spotifyService.makeSpotifyRequest(
                getAccessTokenFromHeader(accessToken),
                "https://api.spotify.com/v1/artists/" + id + "/top-tracks",
                HttpMethod.GET,
                Map.class
        );

        ResponseEntity<Map> responseArtistAbums = spotifyService.makeSpotifyRequest(
                getAccessTokenFromHeader(accessToken),
                "https://api.spotify.com/v1/artists/" + id + "/albums",
                HttpMethod.GET,
                Map.class
        );
        if (responseArtist.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return responseArtist;
        } else if (responseArtistTopTracks.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return responseArtistAbums;
        } else if (responseArtistAbums.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return responseArtistAbums;
        } else {
            return ResponseEntity.ok(Map.of(
                    "artist", responseArtist.getBody(),
                    "top_tracks", responseArtistTopTracks.getBody(),
                    "artist_albums", responseArtistAbums.getBody()
            ));
        }

    }

    @GetMapping("/albums/{id}")
    public ResponseEntity<?> getAlbum(
            @PathVariable String id,
            @RequestHeader("Authorization") String accessToken
    ) {
        ResponseEntity<Map> response = spotifyService.makeSpotifyRequest(
                getAccessTokenFromHeader(accessToken),
                "https://api.spotify.com/v1/albums/" + id,
                HttpMethod.GET,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return response;
        } else {
            return ResponseEntity.ok(response.getBody());
        }

    }

    @GetMapping("/search")
    public ResponseEntity<?> Search(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam("q") String query
    ) {
        String types = "album,artist,track";
        ResponseEntity<Map> response = spotifyService.makeSpotifyRequest(
                getAccessTokenFromHeader(accessToken),
                "https://api.spotify.com/v1/search?q=" + query + "&type=" + types,
                HttpMethod.GET,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return response;
        } else {
            return ResponseEntity.ok(response.getBody());
        }
    }

    private String getAccessTokenFromHeader(String header) {
        return header.split("\\s")[1];
    }
}

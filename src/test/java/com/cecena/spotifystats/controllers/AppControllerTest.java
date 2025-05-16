package com.cecena.spotifystats.controllers;


import com.cecena.spotifystats.services.SpotifyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Map;

@WebMvcTest(AppController.class)
public class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpotifyService spotifyService;

    private final String authHeader = "Bearer testToken";

    @Test
    void getUserTopArtist_shouldReturnOk() throws Exception {
        Map<String, Object> fakeResponse = Map.of(
                "next", "",
                "items", List.of()
        );

        Mockito.when(spotifyService.makeSpotifyRequest(
                "testToken",
                "https://api.spotify.com/v1/me/top/artists",
                HttpMethod.GET,
                Map.class
        )).thenReturn(new ResponseEntity<>(fakeResponse, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/spotify/me/top/artists")
                        .header("Authorization", authHeader))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.items").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.previous").doesNotExist());
    }

    @Test
    void getAlbum_shouldReturnUnauthorized() throws Exception {

        Mockito.when(spotifyService.makeSpotifyRequest(
                "testToken",
                "https://api.spotify.com/v1/albums/123",
                HttpMethod.GET,
                Map.class
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/spotify/albums/123")
                        .header("Authorization", authHeader))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void getArtist_shouldReturnOk() throws Exception {
        Map<String, Object> artist = Map.of("name", "Artist Name");
        Map<String, Object> topTracks = Map.of("tracks", List.of());
        Map<String, Object> albums = Map.of("items", List.of());

        Mockito.when(spotifyService.makeSpotifyRequest("testToken", "https://api.spotify.com/v1/artists/abc", HttpMethod.GET, Map.class))
                .thenReturn(new ResponseEntity<>(artist, HttpStatus.OK));

        Mockito.when(spotifyService.makeSpotifyRequest("testToken", "https://api.spotify.com/v1/artists/abc/top-tracks", HttpMethod.GET, Map.class))
                .thenReturn(new ResponseEntity<>(topTracks, HttpStatus.OK));

        Mockito.when(spotifyService.makeSpotifyRequest("testToken", "https://api.spotify.com/v1/artists/abc/albums", HttpMethod.GET, Map.class))
                .thenReturn(new ResponseEntity<>(albums, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/spotify/artists/abc")
                        .header("Authorization", authHeader))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.artist.name").value("Artist Name"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.top_tracks.tracks").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.artist_albums.items").isArray());
    }

    @Test
    void search_shouldReturnResults() throws Exception {
        Map<String, Object> searchResults = Map.of("artists", Map.of("items", List.of()));

        Mockito.when(spotifyService.makeSpotifyRequest(
                "testToken",
                "https://api.spotify.com/v1/search?q=test&type=album,artist,track",
                HttpMethod.GET,
                Map.class
        )).thenReturn(new ResponseEntity<>(searchResults, HttpStatus.OK));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/spotify/search")
                        .header("Authorization", authHeader)
                        .param("q", "test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.artists.items").isArray());
    }

}

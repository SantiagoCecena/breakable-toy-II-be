# 🎧 Spotify Stats App Backend

## Description
This backend project acts as an intermediary between a frontend application and the Spotify Web API. It handles user authentication using Spotify's OAuth 2.0 flow and manages requests to fetch and deliver user-specific data provided by the Spotify API, such as top artists, tracks, albums, and other related information. 

## 🚀 Technologies used
- Java
- Spring boot
- Gradle

## ⚒️ Installation instructions
### Prerequisites
- Java 21
- Gradle (optinal but not required)

### Setup:

1. Clone the repository:
```git clone https://github.com/SantiagoCecena/breakable-toy-II-be.git```
```cd breakable-toy-II-be```  

2. Build the project:  
```./gradlew clean install -x test``` on macOS/Linux
```gradlew.bat clean install -x test``` on Windows

3. Run the project:
```./gradlew bootRun``` on macOS/Linux
```gradlew.bat bootRun``` on Windows
<br>The application will be available at http://127.0.0.0.1:8080 


## 📖 API Documentation
### Endpoints
### ```Base route: /api/spotify```
All endpoints require an Authorization header with a valid Spotify access token.

<hr>

#### ```GET /me/top/artists```
Description: Retrieves the authenticated user's most-listened-to artists.

##### Headers
- ```Authorization``` (string, required): Spotify access token. Format ```Bearer <token>```

##### Successful response (200 OK)

```
{
    "previous": "<previous page URL>",
    "next": "<next page URL>",
    "items": [ ... ]
}
```

##### Status Codes
- ```200 OK```: Top artists retrieved successfully.

- ```401 Unauthorized```: Invalid or expired token.

- ```500 Internal Server Error```: Error while processing the request.

<hr>

#### ```GET /artists/{id}```
Fetches detailed information about a specific artist, including their top tracks and albums.

##### Path Parameters
- ```id``` (string, required): Spotify artist ID.

##### Headers
- ```Authorization``` (string, required): Spotify access token. Format ```Bearer <token>```

##### Successful response (200 OK)

```
{
  "artist": { ... },
  "top_tracks": { ... },
  "artist_albums": { ... }
}

```

##### Status Codes
- ```200 OK```: Artist data retrieved successfully.

- ```401 Unauthorized```: Invalid or expired token.

<hr>

#### ```GET /albums/{id}```
Retrieves information about a specific album on Spotify.

##### Path Parameters
- ```id``` (string, required): Spotify album ID.

##### Headers
- ```Authorization``` (string, required): Spotify access token. Format ```Bearer <token>```

##### Successful response (200 OK)

```
{ ... } // Full album data
```

##### Status Codes
- ```200 OK```: Album data retrieved successfully.

- ```401 Unauthorized```: Invalid or expired token.

<hr>

#### ```GET /search```
Performs a search for albums, artists, and tracks.

##### Path Parameters
- ```q``` (string, required): Search query string.

##### Headers
- ```Authorization``` (string, required): Spotify access token. Format ```Bearer <token>```

##### Successful response (200 OK)

```
{
  "albums": { ... },
  "artists": { ... },
  "tracks": { ... }
}
```

##### Status Codes
- ```200 OK```: Search results retrieved successfully.

- ```401 Unauthorized```: Invalid or expired token.
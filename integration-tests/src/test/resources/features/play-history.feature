@PlayHistory
Feature: Recently played tracks are pulled from Spotify and stored

  Scenario: Play history is not fetched when the user is not authenticated
    # No token has been exchanged, so the job should bail out before calling Spotify at all.
    When the play history job runs
    Then the play table should be empty
    And the URL "/v1/me/player/recently-played?limit=50" should have been called 0 times

  Scenario: Recently played tracks are stored with their track, album and artists
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Given the URL "/v1/me/player/recently-played?limit=50" is set to return the JSON from file "spotify/player/recently-played-two-tracks.json"
    And the URL "/v1/tracks/track-one" is set to return the JSON from file "spotify/tracks/track-one.json"
    And the URL "/v1/tracks/track-two" is set to return the JSON from file "spotify/tracks/track-two.json"
    When the play history job runs
    # The play id is the played_at time in epoch milliseconds.
    Then the play table should contain the following plays:
      | id            | track_id  |
      | 1705312800000 | track-one |
      | 1705316400000 | track-two |
    And the track table should contain the following tracks:
      | id        | name        | song_length | popularity | album_id  | artists    |
      | track-one | First Song  | 180000      | 55         | album-one | artist-one |
      | track-two | Second Song | 210000      | 42         | album-two | artist-two |
    And the artist table should contain the following artists:
      | id         | name         |
      | artist-one | The Band     |
      | artist-two | Another Band |
    And the album table should contain the following albums:
      | id        | type   | name      | artists    |
      | album-one | album  | Debut     | artist-one |
      | album-two | single | Follow Up | artist-two |

  Scenario: Plays that are already stored are not fetched or saved again
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Given the URL "/v1/me/player/recently-played?limit=50" is set to return the JSON from file "spotify/player/recently-played-two-tracks.json"
    And the URL "/v1/tracks/track-one" is set to return the JSON from file "spotify/tracks/track-one.json"
    And the URL "/v1/tracks/track-two" is set to return the JSON from file "spotify/tracks/track-two.json"
    When the play history job runs
    Then the play table should contain the following plays:
      | id            | track_id  |
      | 1705312800000 | track-one |
      | 1705316400000 | track-two |
    # Spotify returns the same two plays again; neither should be looked up or written a second time.
    When the play history job runs
    Then the URL "/v1/me/player/recently-played?limit=50" should have been called 2 times
    And the URL "/v1/tracks/track-one" should have been called 1 times
    And the URL "/v1/tracks/track-two" should have been called 1 times
    And the play table should contain the following plays:
      | id            | track_id  |
      | 1705312800000 | track-one |
      | 1705316400000 | track-two |

  Scenario: Podcast episodes in the play history are ignored
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Given the URL "/v1/me/player/recently-played?limit=50" is set to return the JSON from file "spotify/player/recently-played-with-podcast.json"
    And the URL "/v1/tracks/track-one" is set to return the JSON from file "spotify/tracks/track-one.json"
    When the play history job runs
    Then the play table should contain the following plays:
      | id            | track_id  |
      | 1705312800000 | track-one |
    # The episode is never even looked up, because its type rules it out before the track request.
    And the URL "/v1/tracks/episode-one" should have been called 0 times

  Scenario: A second page of history is fetched using the oldest play time
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Given the URL "/v1/me/player/recently-played?limit=50" is set to return the JSON from file "spotify/player/recently-played-page-one.json"
    # The oldest play on page one is 11:00:00, so the next request asks for anything before 10:59:59.
    And the URL "/v1/me/player/recently-played?limit=50&before=2024-01-15T10%3A59%3A59" is set to return the JSON from file "spotify/player/recently-played-page-two.json"
    And the URL "/v1/tracks/track-one" is set to return the JSON from file "spotify/tracks/track-one.json"
    And the URL "/v1/tracks/track-two" is set to return the JSON from file "spotify/tracks/track-two.json"
    And the URL "/v1/tracks/track-three" is set to return the JSON from file "spotify/tracks/track-three.json"
    When the play history job runs
    Then the play table should contain the following plays:
      | id            | track_id    |
      | 1705312800000 | track-one   |
      | 1705316400000 | track-two   |
      | 1705320000000 | track-three |
    And the URL "/v1/me/player/recently-played?limit=50&before=2024-01-15T10%3A59%3A59" should have been called 1 times

  Scenario: A track lookup failure stops the run without saving the play
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Given the URL "/v1/me/player/recently-played?limit=50" is set to return the JSON from file "spotify/player/recently-played-one-track.json"
    And the URL "/v1/tracks/track-one" is set to return status 500
    When the play history job runs
    Then the play table should be empty
    And the track table should be empty
    And the URL "/v1/tracks/track-one" should have been called 1 times
    # The failure is contained: the application is still serving requests afterwards.
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_STATUS" should be "200"

  Scenario: An album and artist shared by two tracks are only stored once
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Given the URL "/v1/me/player/recently-played?limit=50" is set to return the JSON from file "spotify/player/recently-played-shared-album.json"
    And the URL "/v1/tracks/track-one" is set to return the JSON from file "spotify/tracks/track-one.json"
    And the URL "/v1/tracks/track-three" is set to return the JSON from file "spotify/tracks/track-three.json"
    When the play history job runs
    Then the track table should contain the following tracks:
      | id          | name       | song_length | popularity | album_id  | artists    |
      | track-one   | First Song | 180000      | 55         | album-one | artist-one |
      | track-three | Third Song | 240000      | 30         | album-one | artist-one |
    And the artist table should contain the following artists:
      | id         | name     |
      | artist-one | The Band |
    And the album table should contain the following albums:
      | id        | type  | name  | artists    |
      | album-one | album | Debut | artist-one |

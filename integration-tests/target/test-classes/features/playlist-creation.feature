@Playlist
Feature: Playlists are created from the most played tracks

  Background:
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Then "RESPONSE_STATUS" should be "200"
    Given the URL "/v1/me" is set to return the JSON from file "spotify/me.json"
    And the POST URL "/v1/users/user-one/playlists" is set to return the JSON from file "spotify/playlists/created-playlist.json"

  Scenario: A playlist is created from the most played tracks in the range
    # track-one was played three times, track-two twice and track-three once, so asking for the top
    # two should leave track-three out.
    Given the following plays exist:
      | id            | track_id    |
      | 1705312800000 | track-one   |
      | 1705312800001 | track-one   |
      | 1705312800002 | track-one   |
      | 1705316400000 | track-two   |
      | 1705316400001 | track-two   |
      | 1705320000000 | track-three |
    And the POST URL "/v1/playlists/playlist-one/tracks?uris=spotify%3Atrack%3Atrack-one%2Cspotify%3Atrack%3Atrack-two" is set to return the JSON from file "spotify/playlists/snapshot.json"
    When I send an HTTP POST request to "/playlist/create/20240101/20240131/2?name=Top%20Tracks"
    Then "RESPONSE_STATUS" should be "200"
    And "RESPONSE_BODY" should be "Done"
    And the URL "/v1/me" should have been called 1 times
    And the POST URL "/v1/users/user-one/playlists" should have been called with the JSON from file "spotify/playlists/create-playlist-request.json"
    # The tracks are sent as a query parameter, most played first.
    And the POST URL "/v1/playlists/playlist-one/tracks?uris=spotify%3Atrack%3Atrack-one%2Cspotify%3Atrack%3Atrack-two" should have been called 1 times

  Scenario: Only plays inside the requested date range are counted
    # track-three is the most played track overall, but every one of its plays is in February and so
    # falls outside the requested January range.
    Given the following plays exist:
      | id            | track_id    |
      | 1705312800000 | track-one   |
      | 1705312800001 | track-one   |
      | 1705312800002 | track-one   |
      | 1705316400000 | track-two   |
      | 1705316400001 | track-two   |
      | 1707955200000 | track-three |
      | 1707955200001 | track-three |
      | 1707955200002 | track-three |
      | 1707955200003 | track-three |
      | 1707955200004 | track-three |
    And the POST URL "/v1/playlists/playlist-one/tracks?uris=spotify%3Atrack%3Atrack-one%2Cspotify%3Atrack%3Atrack-two" is set to return the JSON from file "spotify/playlists/snapshot.json"
    When I send an HTTP POST request to "/playlist/create/20240101/20240131/2?name=Top%20Tracks"
    Then "RESPONSE_STATUS" should be "200"
    And the POST URL "/v1/playlists/playlist-one/tracks?uris=spotify%3Atrack%3Atrack-one%2Cspotify%3Atrack%3Atrack-two" should have been called 1 times

  Scenario: A range with no plays in it is rejected and never reaches Spotify
    Given the following plays exist:
      | id            | track_id  |
      | 1705312800000 | track-one |
    When I send an HTTP POST request to "/playlist/create/20230101/20230131/2?name=Top%20Tracks"
    Then "RESPONSE_STATUS" should be "404"
    And the URL "/v1/me" should have been called 0 times
    And the POST URL "/v1/users/user-one/playlists" should have been called 0 times

  Scenario: A Spotify failure while creating the playlist is swallowed
    # The controller logs and still reports success, so a caller cannot tell the playlist was never
    # created. This pins the current behaviour rather than endorsing it.
    Given the following plays exist:
      | id            | track_id  |
      | 1705312800000 | track-one |
    And the POST URL "/v1/users/user-one/playlists" is set to return status 500
    When I send an HTTP POST request to "/playlist/create/20240101/20240131/2?name=Top%20Tracks"
    Then "RESPONSE_STATUS" should be "200"
    And "RESPONSE_BODY" should be "Done"
    And the POST URL "/v1/users/user-one/playlists" should have been called 1 times

@Auth
Feature: Spotify authentication

  Scenario: Login state returns false when not authenticated
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_STATUS" should be "200"
    And "RESPONSE_BODY" should be "false"

  Scenario: The login URL asks Spotify for the scopes the application needs
    # Built by the library rather than fetched, so this is the one Spotify URL that is not stubbed.
    When I send an HTTP GET request to "/login"
    Then "RESPONSE_STATUS" should be "200"
    And "RESPONSE_BODY" should be "https://accounts.spotify.com:443/authorize?client_id=test-client-id&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A28080%2Fget-user-code&scope=user-read-recently-played%20playlist-modify-public%20playlist-modify-private&show_dialog=true"

  Scenario: The user can exchange an authorisation code for tokens
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Then "RESPONSE_STATUS" should be "200"
    And "RESPONSE_BODY" should be "Auth successful"
    And the POST URL "/api/token" should have been called 1 times
    # The refresh token is persisted so the application can authenticate itself after a restart.
    And the token table should contain the following tokens:
      | id | refresh_token      |
      | 1  | test-refresh-token |
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_BODY" should be "true"

  Scenario: A failed code exchange leaves the user logged out
    # The controller swallows the failure and still reports success, so the login state and the
    # empty token table are the only signals that the exchange did not work.
    Given the POST URL "/api/token" is set to return status 400
    When I send an HTTP GET request to "/get-user-code?code=expired-user-code"
    Then "RESPONSE_STATUS" should be "200"
    And "RESPONSE_BODY" should be "Auth successful"
    And the token table should be empty
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_BODY" should be "false"

  Scenario: The scheduled token refresh gets a new access token
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Then "RESPONSE_STATUS" should be "200"
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/refreshed-token-response.json"
    When the token refresh job runs
    Then the POST URL "/api/token" should have been called 2 times
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_BODY" should be "true"

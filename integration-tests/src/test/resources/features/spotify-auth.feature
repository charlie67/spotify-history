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

  Scenario: A failed code exchange tells the user to try again and leaves them logged out
    Given the POST URL "/api/token" is set to return status 400
    When I send an HTTP GET request to "/get-user-code?code=expired-user-code"
    Then "RESPONSE_STATUS" should be "502"
    And "RESPONSE_BODY" should be "Auth failed, please try signing in again"
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

  Scenario: Nothing is refreshed before the user has ever signed in
    # There is no token to refresh, so the job must not go to Spotify with an empty one and read the
    # rejection as an expired sign in.
    When the token refresh job runs
    Then the POST URL "/api/token" should have been called 0 times
    When I send an HTTP GET request to "/authStatus"
    Then "RESPONSE_STATUS" should be "200"
    And the response body should match the file: "spotify/auth/signed-out-status.json"

  Scenario: An expired refresh token is discarded and the user is asked to sign in again
    # Spotify expires refresh tokens six months after they are issued and answers a refresh with
    # 400 invalid_grant from then on.
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Then "RESPONSE_STATUS" should be "200"
    Given the POST URL "/api/token" is set to return status 400 with the JSON from file "spotify/token/invalid-grant-response.json"
    When the token refresh job runs
    # The rejected token is thrown away rather than kept for another attempt.
    Then the token table should be empty
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_BODY" should be "false"
    When I send an HTTP GET request to "/authStatus"
    Then the response body should match the file: "spotify/auth/reauthorisation-required-status.json"

  Scenario: A discarded refresh token is never sent to Spotify again
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Then "RESPONSE_STATUS" should be "200"
    Given the POST URL "/api/token" is set to return status 400 with the JSON from file "spotify/token/invalid-grant-response.json"
    When the token refresh job runs
    # One call to exchange the code and one refresh that was rejected. Later runs of the job have
    # nothing to send, so the count stays where it is.
    Then the POST URL "/api/token" should have been called 2 times
    When the token refresh job runs
    And the token refresh job runs
    Then the POST URL "/api/token" should have been called 2 times

  Scenario: Signing in again after an expiry clears the reauthorisation state
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Given the POST URL "/api/token" is set to return status 400 with the JSON from file "spotify/token/invalid-grant-response.json"
    When the token refresh job runs
    Then the token table should be empty
    # The user goes back through the sign in flow, exactly as they did the first time.
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=second-user-code"
    Then "RESPONSE_STATUS" should be "200"
    And the token table should contain the following tokens:
      | id | refresh_token      |
      | 1  | test-refresh-token |
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_BODY" should be "true"
    # And the refresh job works again with the new token.
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/refreshed-token-response.json"
    When the token refresh job runs
    Then the token table should contain the following tokens:
      | id | refresh_token      |
      | 1  | test-refresh-token |

  Scenario: A refresh that fails for any other reason keeps the stored token
    # A 500 says nothing about the token, so throwing it away would sign the user out over a blip.
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    Given the POST URL "/api/token" is set to return status 500
    When the token refresh job runs
    Then the token table should contain the following tokens:
      | id | refresh_token      |
      | 1  | test-refresh-token |
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_BODY" should be "true"
    # The next run tries the same token again and gets through.
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/refreshed-token-response.json"
    When the token refresh job runs
    Then the POST URL "/api/token" should have been called 3 times
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_BODY" should be "true"

  Scenario: A refresh token rotated by Spotify replaces the stored one
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/token-response.json"
    When I send an HTTP GET request to "/get-user-code?code=test-user-code"
    # Spotify may hand back a new refresh token on a refresh; the old one stops working when it does.
    Given the POST URL "/api/token" is set to return the JSON from file "spotify/token/rotated-token-response.json"
    When the token refresh job runs
    Then the token table should contain the following tokens:
      | id | refresh_token         |
      | 1  | rotated-refresh-token |
    When I send an HTTP GET request to "/loginState"
    Then "RESPONSE_BODY" should be "true"

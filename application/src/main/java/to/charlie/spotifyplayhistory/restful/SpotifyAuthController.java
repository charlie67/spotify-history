package to.charlie.spotifyplayhistory.restful;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import to.charlie.spotifyplayhistory.domain.service.SpotifyApiService;


@RestController
@RequestMapping("/")
public class SpotifyAuthController
{
  private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyAuthController.class);

  private final SpotifyApiService spotifyApiService;

  @Autowired
  public SpotifyAuthController(SpotifyApiService spotifyApiService)
  {
    this.spotifyApiService = spotifyApiService;
  }

  @GetMapping("/login")
  @ResponseBody
  public String spotifyLogin()
  {
    AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApiService.spotifyApi.authorizationCodeUri()
        .scope("user-read-recently-played playlist-modify-public playlist-modify-private")
        .show_dialog(true)
        .build();

    return authorizationCodeUriRequest.execute().toString();
  }

  @GetMapping("/loginState")
  @ResponseBody
  @CrossOrigin(origins = "*")
  public ResponseEntity<Boolean> getLoginMessage()
  {
    if (spotifyApiService.isLoggedIn())
    {
      return ResponseEntity.ok(true);
    }

    return ResponseEntity.ok(false);
  }

  /**
   * The fuller picture behind {@code /loginState}: whether the stored refresh token was rejected
   * and has been thrown away, and when the one now held runs out.
   */
  @GetMapping("/authStatus")
  @ResponseBody
  @CrossOrigin(origins = "*")
  public ResponseEntity<AuthStatus> getAuthStatus()
  {
    return ResponseEntity.ok(new AuthStatus(spotifyApiService.isLoggedIn(),
        spotifyApiService.isReauthorisationRequired(),
        spotifyApiService.refreshTokenIssuedAt().orElse(null),
        spotifyApiService.refreshTokenExpiresAt().orElse(null)));
  }

  @GetMapping("/get-user-code")
  public ResponseEntity<String> getSpotifyUserCode(@RequestParam("code") String userCode)
  {
    AuthorizationCodeRequest authorizationCodeRequest = spotifyApiService.spotifyApi.authorizationCode(userCode).build();

    try
    {
      final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

      spotifyApiService.onAuthorisationCodeExchanged(authorizationCodeCredentials);

      LOGGER.info("Signed in, access token expires in: {}", authorizationCodeCredentials.getExpiresIn());

      return ResponseEntity.ok("Auth successful");
    }
    catch (ParseException | IOException | SpotifyWebApiException | URISyntaxException e)
    {
      // Saying "Auth successful" here used to leave the user looking at a page that claimed they
      // were signed in while the application was still signed out, which matters far more now that
      // an expired token sends them back through this flow.
      LOGGER.error("Error in authentication", e);

      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Auth failed, please try signing in again");
    }
  }
}

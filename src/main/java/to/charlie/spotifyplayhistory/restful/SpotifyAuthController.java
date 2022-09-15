package to.charlie.spotifyplayhistory.restful;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import to.charlie.spotifyplayhistory.SpotifyApiService;
import to.charlie.spotifyplayhistory.config.SpotifyProperties;
import to.charlie.spotifyplayhistory.domain.entity.Token;
import to.charlie.spotifyplayhistory.domain.repository.TokenRepository;


@RestController
@RequestMapping("/")
public class SpotifyAuthController
{
  private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyAuthController.class);

  private final SpotifyApiService spotifyApiService;

  private final TokenRepository tokenRepository;

  private final SpotifyProperties spotifyProperties;

  @Autowired
  public SpotifyAuthController(SpotifyApiService spotifyApiService,
                               TokenRepository tokenRepository,
                               SpotifyProperties spotifyProperties)
  {
    this.spotifyApiService = spotifyApiService;
    this.tokenRepository = tokenRepository;
    this.spotifyProperties = spotifyProperties;
  }

  @GetMapping("/login")
  @ResponseBody
  public RedirectView spotifyLogin()
  {
    AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApiService.spotifyApi.authorizationCodeUri()
        .scope("user-read-recently-played playlist-modify-public playlist-modify-private")
        .show_dialog(true)
        .build();

    return new RedirectView(authorizationCodeUriRequest.execute().toString());
  }

  @GetMapping("/get-user-code")
  public ResponseEntity<String> getSpotifyUserCode(@RequestParam("code") String userCode)
  {
    AuthorizationCodeRequest authorizationCodeRequest = spotifyApiService.spotifyApi.authorizationCode(userCode).build();

    try
    {
      final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

      String refreshToken = authorizationCodeCredentials.getRefreshToken();

      spotifyApiService.spotifyApi = new SpotifyApi.Builder()
          .setAccessToken(authorizationCodeCredentials.getAccessToken())
          .setRefreshToken(refreshToken)
          .setClientSecret(spotifyProperties.getSpotifyClientSecret())
          .setClientId(spotifyProperties.getSpotifyClientId())
          .setRedirectUri(new URI(spotifyProperties.getSpotifyBaseRedirectUri()))
          .build();

      spotifyApiService.spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());

      Token token = new Token();
      token.setRefreshToken(refreshToken);
      // hard code this for now
      token.setId(1);
      tokenRepository.save(token);

      LOGGER.info("Refresh credentials expire in: {}", authorizationCodeCredentials.getExpiresIn());
    }
    catch (ParseException | IOException | SpotifyWebApiException | URISyntaxException e)
    {
      LOGGER.error("Error in authentication", e);
    }

    return ResponseEntity.ok("Auth successful");
  }
}

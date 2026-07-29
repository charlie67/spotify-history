package to.charlie.spotifyplayhistory.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import se.michaelthelin.spotify.IHttpManager;
import se.michaelthelin.spotify.SpotifyApi;


/**
 * Builds {@link SpotifyApi} instances that all share the same {@link IHttpManager} and client
 * credentials. Every {@link SpotifyApi} in the application must come from here - the http manager is
 * the only hook the library offers for redirecting requests, because the authorisation requests
 * hardcode {@code accounts.spotify.com} inside their builders and ignore the configured host.
 */
@Component
@RequiredArgsConstructor
public class SpotifyApiFactory
{
  private final SpotifyProperties spotifyProperties;

  private final IHttpManager httpManager;

  public SpotifyApi.Builder builder() throws URISyntaxException
  {
    return new SpotifyApi.Builder()
        .setHttpManager(httpManager)
        .setClientId(spotifyProperties.getSpotifyClientId())
        .setClientSecret(spotifyProperties.getSpotifyClientSecret())
        .setRedirectUri(new URI(spotifyProperties.getSpotifyBaseRedirectUri()));
  }
}
